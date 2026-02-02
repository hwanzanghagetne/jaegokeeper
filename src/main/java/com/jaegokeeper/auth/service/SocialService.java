package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.TicketDTO;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import com.jaegokeeper.common.api.ApiException;
import com.jaegokeeper.psj.dto.StoreDto;
import com.jaegokeeper.psj.mapper.StoreMapper2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class SocialService {

    private final StoreMapper2 storeMapper;
    private final UserAuthMapper userAuthMapper;

    private final Map<String, SocialVerifier> verifiersByProvider;

    public SocialService(
            StoreMapper2 storeMapper,
            UserAuthMapper userAuthMapper,
            List<SocialVerifier> verifiers
    ) {
        this.storeMapper = storeMapper;
        this.userAuthMapper = userAuthMapper;

        Map<String, SocialVerifier> m = new HashMap<>();
        for (SocialVerifier v : verifiers) m.put(v.provider(), v);
        this.verifiersByProvider = Collections.unmodifiableMap(m);
    }

    /** 1) accessToken 검증 후, user 없으면 생성, ticket 발급 */
    @Transactional
    public String completeAndIssueTicket(String provider, String accessToken, String redirectUrl) throws Exception {
        SocialVerifier verifier = verifiersByProvider.get(provider);
        if (verifier == null) throw new IllegalArgumentException("unsupported provider: " + provider);
        if (accessToken == null || accessToken.isEmpty()) throw new IllegalArgumentException("accessToken required");

        SocialProfile profile = verifier.verify(accessToken);
        String providerUid = profile.getProviderUid();

        LoginTarget tgt = userAuthMapper.findByProviderUid(provider, providerUid);

        int userId;
        if (tgt == null) {
            // user 생성
            UserDTO user = new UserDTO();
            user.setUserName(defaultName(profile.getDisplayName(), provider));
            user.setUserMail(profile.getEmail());        // email은 없을 수도 있음(또는 중복 가능성 있음)
            user.setIsActive(true);
            userAuthMapper.insertUser(user);

            // store 생성(요구사항)
            StoreDto store = new StoreDto();
            store.setUserId(user.getUserId());
            store.setStoreName(user.getUserName() + "의 스토어");
            storeMapper.insertStore(store);

            // auth 생성
            UidDTO auth = new UidDTO();
            auth.setUserId(user.getUserId());
            auth.setProvider(provider);
            auth.setProviderUid(providerUid);
            auth.setPassHash(null);
            userAuthMapper.insertAuth(auth);

            userId = user.getUserId();
        } else {
            if (tgt.getIsActive() == false) throw new IllegalStateException("user inactive");
            userId = tgt.getUserId();
        }

        // ticket 생성 (예: 2분 TTL)
        String ticketKey = UUID.randomUUID().toString();

        TicketDTO ticket = new TicketDTO();
            ticket.setTicketKey(ticketKey);
            ticket.setUserId(userId);
            ticket.setRedirectUrl(redirectUrl);
            ticket.setExpiresAt(Date.from(Instant.now().plusSeconds(300)));

        int inserted = userAuthMapper.insertTicket(ticket);
        if (inserted != 1) {
            throw ApiException.badRequest("TICKET_ISSUE_FAILED", "티켓 발급에 실패했습니다.");
        }

        return ticketKey;
    }

    private String defaultName(String displayName, String provider) {
        if (displayName != null && !displayName.trim().isEmpty()) return displayName.trim();
        return provider + "_USER";
    }
}