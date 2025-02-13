package kr.hhplus.be.server.domain.payment;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import kr.hhplus.be.server.domain.order.Order;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // CASH, CARD, POINTS

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, SUCCESS, FAILED, REFUNDED

    private String transactionId;

    public static Payment create(Long orderId, BigDecimal amount, PaymentMethod paymentMethod){
        String tid = paymentMethod.equals(PaymentMethod.POINTS) ? generateTransactionId() : null;
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PENDING)
                .transactionId(tid)
                .build();
        return payment;
    }

    private static String generateTransactionId(){
        String prefix = "POINT_";
        String uniqueSuffix = UUID.randomUUID().toString();
        return prefix + uniqueSuffix;
    }

    public Payment changeStatus(PaymentStatus newStatus) {
        return Payment.builder()
                .id(this.id)
                .orderId(this.orderId)
                .amount(this.amount)
                .paymentMethod(this.paymentMethod)
                .status(newStatus)
                .transactionId(this.transactionId)
                .build();
    }

}
