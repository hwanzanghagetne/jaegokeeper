package com.jaegokeeper.schedule;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ScheduleTimesTest {

    @Test
    public void 영문_약어_한글을_모두_영문_대문자_요일로_정규화() {
        assertEquals("MONDAY", ScheduleTimes.normalize("monday"));
        assertEquals("WEDNESDAY", ScheduleTimes.normalize(" Wed "));
        assertEquals("SUNDAY", ScheduleTimes.normalize("일"));
        assertEquals("SATURDAY", ScheduleTimes.normalize("토"));
    }

    @Test
    public void 알수없는_값이나_null은_BAD_REQUEST() {
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> ScheduleTimes.normalize("월요일")).getErrorCode());
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> ScheduleTimes.normalize(null)).getErrorCode());
    }
}
