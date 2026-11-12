package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginRequest;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class LocalService {

    private final UserAuthMapper userAuthMapper;

    public int login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        UserDTO user = userAuthMapper.findUserByEmail(email);
        if (user == null || !PasswordHasher.matches(req.getPassword(), user.getPassHash())) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessException(USER_NOT_ACTIVE);
        }

        return user.getUserId();
    }

}
