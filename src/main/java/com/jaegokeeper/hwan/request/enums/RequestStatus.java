package com.jaegokeeper.hwan.request.enums;

public enum RequestStatus {
    WAIT,      // 대기
    WORKING,   // 확인중
    CONFIRM,   // 승인
    REVERT,    // 거절
    DONE,      // 완료
    CANCEL     // 취소
}