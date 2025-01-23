package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;

import java.math.BigDecimal;

public class PaymentCommand {
    public record Create(Long orderId, BigDecimal totalAmount) {}

    public record Pay(Long userId, Payment payment) {}

}
