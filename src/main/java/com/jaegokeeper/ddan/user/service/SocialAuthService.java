package com.jaegokeeper.ddan.user.service;

import com.jaegokeeper.ddan.user.dto.LoginTarget;
import com.jaegokeeper.ddan.user.dto.TicketDTO;
import com.jaegokeeper.ddan.user.dto.UidDTO;
import com.jaegokeeper.ddan.user.dto.UserDTO;
import com.jaegokeeper.ddan.user.mapper.UserAuthMapper;
import com.jaegokeeper.ddan.user.utils.SocialProfile;
import com.jaegokeeper.ddan.user.utils.SocialVerifier;
import com.jaegokeeper.psj.dto.StoreDto;
import com.jaegokeeper.psj.mapper.StoreMapper2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class SocialAuthService {

    private final StoreMapper2 storeMapper;
    private final UserAuthMapper userAuthMapper;

    private final Map<String, SocialVerifier> verifiersByProvider;

    public SocialAuthService(
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
        String key = UUID.randomUUID().toString();
        TicketDTO ticket = new TicketDTO();
        ticket.setTicketKey(key);
        ticket.setUserId(userId);
        ticket.setRedirectUrl(redirectUrl);
        ticket.setExpiresAt(Date.from(Instant.now().plusSeconds(120)));
        userAuthMapper.insertTicket(ticket);

        return key;
    }

    private String defaultName(String displayName, String provider) {
        if (displayName != null && !displayName.trim().isEmpty()) return displayName.trim();
        return provider + "_USER";
    }
}