package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.order.Order;

import java.math.BigDecimal;

public record OrderFixture() {

    public static Order order(Long userId, BigDecimal totalAmount){

        return Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .build();
    }



}
