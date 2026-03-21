package com.jaegokeeper.mail;

import com.jaegokeeper.common.mail.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-mail-context.xml")
public class MailServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MailServiceTest.class);

    @Autowired
    private MailService mailService;

    /**
     * 비동기 적용 전/후 응답 반환 시간 비교 테스트
     *
     * [@Async 주석 처리 후 실행 - 동기]
     * → [테스트] 호출 반환 시간: 3.37초  ← 메일 전송 완료될 때까지 블로킹
     *
     * [@Async 복원 후 실행 - 비동기]
     * → [테스트] 호출 반환 시간: 0.003초 ← 스레드풀에 위임 후 즉시 반환
     */
    @Test
    public void 메일_전송_속도_측정() throws InterruptedException {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        mailService.sendSignupCode("dltmdghks543@gmail.com", "123456");
        stopWatch.stop();

        log.info("[테스트] sendSignupCode 호출 반환 시간: {}초", stopWatch.getTotalTimeSeconds());

        // 비동기 작업이 실제로 완료될 때까지 대기
        Thread.sleep(5000);
    }
}