package com.jaegokeeper.schedule;

import com.jaegokeeper.exception.BusinessException;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.jaegokeeper.exception.ErrorCode.BAD_REQUEST;

public final class ScheduleTimes {

    private static final String[] KOREAN = {"월", "화", "수", "목", "금", "토", "일"};
    private static final Map<String, String> DAYS = new HashMap<>();

    static {
        for (DayOfWeek day : DayOfWeek.values()) {
            String name = day.name();
            DAYS.put(name, name);
            DAYS.put(name.substring(0, 3), name);
            DAYS.put(KOREAN[day.ordinal()], name);
        }
    }

    private ScheduleTimes() {
    }

    public static String normalize(String raw) {
        String day = raw == null ? null : DAYS.get(raw.trim().toUpperCase(Locale.ROOT));
        if (day == null) {
            throw new BusinessException(BAD_REQUEST);
        }
        return day;
    }
}
