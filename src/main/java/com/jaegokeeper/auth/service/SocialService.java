package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.TicketDTO;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
public class SocialService {

    private final StoreMapper storeMapper;
    private final UserAuthMapper userAuthMapper;
    private final Map<String, SocialVerifier> verifiersByProvider;

    public SocialService(
            StoreMapper storeMapper,
            UserAuthMapper userAuthMapper,
            List<SocialVerifier> verifiers
    ) {
        this.storeMapper = storeMapper;
        this.userAuthMapper = userAuthMapper;

        Map<String, SocialVerifier> m = new HashMap<>();
        for (SocialVerifier v : verifiers) m.put(v.provider(), v);
        this.verifiersByProvider = Collections.unmodifiableMap(m);
    }

    @Transactional
    public String completeAndIssueTicket(String provider, String accessToken, String redirectUrl) {
        SocialVerifier verifier = verifiersByProvider.get(provider);
        if (verifier == null) throw new BusinessException(BAD_REQUEST);
        if (accessToken == null || accessToken.isEmpty()) throw new BusinessException(BAD_REQUEST);

        SocialProfile profile;
        try {
            profile = verifier.verify(accessToken);
        } catch (Exception e) {
            throw new BusinessException(BAD_REQUEST);
        }

        int userId = findOrRegister(provider, profile);

        String ticketKey = UUID.randomUUID().toString();
        TicketDTO ticket = new TicketDTO();
        ticket.setTicketKey(ticketKey);
        ticket.setUserId(userId);
        ticket.setRedirectUrl(redirectUrl);
        ticket.setExpiresAt(Date.from(Instant.now().plusSeconds(300)));

        int inserted = userAuthMapper.insertTicket(ticket);
        if (inserted != 1) {
            throw new BusinessException(TICKET_ISSUE_FAILED);
        }

        return ticketKey;
    }

    private int findOrRegister(String provider, SocialProfile profile) {
        String providerUid = profile.getProviderUid();
        LoginTarget tgt = userAuthMapper.findByProviderUid(provider, providerUid);

        if (tgt != null) {
            if (!tgt.getIsActive()) throw new BusinessException(USER_NOT_ACTIVE);
            return tgt.getUserId();
        }

        // 신규 유저 등록
        UserDTO user = new UserDTO();
        user.setUserName(defaultName(profile.getDisplayName(), provider));
        user.setUserMail(profile.getEmail());
        user.setIsActive(true);
        userAuthMapper.insertUser(user);

        StoreDto store = new StoreDto();
        store.setUserId(user.getUserId());
        store.setStoreName(user.getUserName() + "의 스토어");
        storeMapper.insertStore(store);

        UidDTO auth = new UidDTO();
        auth.setUserId(user.getUserId());
        auth.setProvider(provider);
        auth.setProviderUid(providerUid);
        auth.setPassHash(null);
        userAuthMapper.insertAuth(auth);

        return user.getUserId();
    }

    private String defaultName(String displayName, String provider) {
        if (displayName != null && !displayName.trim().isEmpty()) return displayName.trim();
        return provider + "_USER";
    }
}
