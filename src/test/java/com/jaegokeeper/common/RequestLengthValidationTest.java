package com.jaegokeeper.common;

import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.email.dto.EmailAuthSendRequest;
import com.jaegokeeper.email.dto.EmailAuthVerifyRequest;
import com.jaegokeeper.item.dto.request.ItemPageRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestLengthValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private String email(int totalLength) {
        return "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(totalLength - 193);
    }

    private <T> boolean hasSizeViolation(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        return violations.stream()
                .anyMatch(v -> v.getConstraintDescriptor().getAnnotation().annotationType() == Size.class);
    }

    private BoardCreateRequest boardCreate(String title) {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle(title);
        req.setContent("내용");
        return req;
    }

    private AlbaRegisterRequest albaRegister(String status) {
        AlbaRegisterRequest req = new AlbaRegisterRequest();
        req.setAlbaName("홍길동");
        req.setAlbaPhone("010-1234-5678");
        req.setAlbaStatus(status);
        return req;
    }

    @Test
    public void 게시글_제목은_100자까지만_허용() {
        assertTrue(validator.validate(boardCreate("가".repeat(100))).isEmpty());
        assertEquals(1, validator.validate(boardCreate("가".repeat(101))).size());

        BoardUpdateRequest update = new BoardUpdateRequest();
        update.setTitle("가".repeat(101));
        assertTrue(hasSizeViolation(update));
        update.setTitle("가".repeat(100));
        assertTrue(validator.validate(update).isEmpty());
    }

    @Test
    public void 알바_근로상태는_컬럼_길이인_20자까지만_허용() {
        assertTrue(validator.validate(albaRegister("a".repeat(20))).isEmpty());
        assertTrue(hasSizeViolation(albaRegister("a".repeat(21))));

        AlbaUpdateRequest update = new AlbaUpdateRequest();
        update.setAlbaStatus("a".repeat(21));
        assertTrue(hasSizeViolation(update));
        update.setAlbaStatus("a".repeat(20));
        assertTrue(validator.validate(update).isEmpty());
    }

    @Test
    public void 인증_이메일은_255자까지만_허용() {
        EmailAuthSendRequest send = new EmailAuthSendRequest();
        send.setEmail(email(255));
        assertTrue(validator.validate(send).isEmpty());
        send.setEmail(email(256));
        assertTrue(hasSizeViolation(send));

        EmailAuthVerifyRequest verify = new EmailAuthVerifyRequest();
        verify.setCode("123456");
        verify.setEmail(email(255));
        assertTrue(validator.validate(verify).isEmpty());
        verify.setEmail(email(256));
        assertTrue(hasSizeViolation(verify));
    }

    @Test
    public void 페이지_번호는_100000까지만_허용() {
        ItemPageRequest page = new ItemPageRequest();
        page.setPage(100_000);
        assertTrue(validator.validate(page).isEmpty());

        ReflectionTestUtils.setField(page, "page", 2_147_483_647);
        assertEquals(1, validator.validate(page).size());
    }
}
