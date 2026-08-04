package com.jaegokeeper.common;

import java.time.ZoneId;

public final class DateTimeConstants {

    // 서버(EC2) OS 타임존이 UTC라 LocalDateTime.now()/LocalDate.now()가 서버 로컬시간과
    // 어긋날 수 있어 명시적으로 지정해야 하는 곳에서 공통으로 참조하는 상수
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private DateTimeConstants() {
    }
}
