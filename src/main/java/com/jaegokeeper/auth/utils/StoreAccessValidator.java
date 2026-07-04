package com.jaegokeeper.auth.utils;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;

import org.springframework.stereotype.Component;

import static com.jaegokeeper.exception.ErrorCode.BAD_REQUEST;
import static com.jaegokeeper.exception.ErrorCode.FORBIDDEN;

/**
 * 요청 경로의 storeId가 로그인 세션의 storeId와 일치하는지 검증한다.
 *
 * SessionInterceptor가 URL 패턴({@code /stores/**})과 path variable 이름("storeId")에
 * 의존해 동일한 검사를 먼저 수행하지만, 그 검사는 인터셉터 매핑 설정이나 변수명이
 * 바뀌면 조용히 우회될 수 있다. 이 클래스는 컨트롤러가 실제로 바인딩한 storeId 값을
 * 직접 비교하므로 URL 구조와 무관하게 항상 동작하는 최종 검증 지점이다.
 */
@Component
public class StoreAccessValidator {

    public void validate(LoginContext login, Integer storeId) {
        if (storeId == null) {
            throw new BusinessException(BAD_REQUEST);
        }
        if (login.getStoreId() != storeId.intValue()) {
            throw new BusinessException(FORBIDDEN);
        }
    }
}
