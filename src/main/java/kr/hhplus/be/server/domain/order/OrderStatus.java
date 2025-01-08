package kr.hhplus.be.server.domain.order;

public enum OrderStatus {
    ORDERED,    // 주문 완료
    PAID,       // 결제 완료
    FAILED,     // 결제 실패
    CANCELLED   // 주문 취소
}