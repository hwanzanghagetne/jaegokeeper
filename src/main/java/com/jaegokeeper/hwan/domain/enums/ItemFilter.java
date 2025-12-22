package com.jaegokeeper.hwan.domain.enums;

public enum ItemFilter {
    FAVORITE,
    LOW_STOCK,
    HAS_REQUEST,
    BAD_STOCK;

    public static ItemFilter from(String value) {
        if (value == null) throw new IllegalArgumentException("filter가 null임");
        try {
            return ItemFilter.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 filter: " + value);
        }
    }
}
