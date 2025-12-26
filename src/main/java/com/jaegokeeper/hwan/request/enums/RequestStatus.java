package com.jaegokeeper.hwan.request.enums;

public enum RequestStatus {
    대기,     // 요청 등록됨
    확인중,   // 검토 중
    승인,     // 승인됨
    반려,     // 거절됨
    완료,     // 처리 완료
    취소      // 요청 취소
}