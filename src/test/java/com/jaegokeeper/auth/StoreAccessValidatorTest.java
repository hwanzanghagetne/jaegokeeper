package com.jaegokeeper.auth;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.StoreAccessValidator;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class StoreAccessValidatorTest {

    private final StoreAccessValidator validator = new StoreAccessValidator();

    @Test
    public void storeId가_널이면_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

        BusinessException e = assertThrows(BusinessException.class,
                () -> validator.validate(login, null));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 세션storeId와_다르면_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

        BusinessException e = assertThrows(BusinessException.class,
                () -> validator.validate(login, 99));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 세션storeId와_같으면_통과() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

        validator.validate(login, 1);
        // 예외 없이 통과하면 성공
    }
}
