package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Builder.Default
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 15, scale = 0)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "final_amount", precision = 15, scale = 0)
    private BigDecimal finalAmount = BigDecimal.ZERO;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status; // ORDERED, PAID, FAILED, CANCELLED

    @Builder.Default
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();


    public Order(Long userId, OrderStatus status){
        this.userId = userId;
        this.status = status;
    }

    public static Order create(Long userId) {
        return Order.builder()
                .userId(userId)
                .status(OrderStatus.ORDERED)
                .build();
    }


    // OrderItem 추가 메서드
    public void addOrderItem(OrderItem item) {
        item.setOrder(this);
        BigDecimal itemTotalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        this.totalAmount = this.totalAmount.add(itemTotalPrice);
        this.orderItems.add(item);
    }


    public void updateStatus(OrderStatus newStatus) {
        if (this.status == newStatus) {
            throw new IllegalStateException("The order is already in the " + newStatus + " status.");
        }

        switch (this.status) {
            case ORDERED:
                if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from ORDERED to " + newStatus);
                }
                break;
            case PAID:
                if (newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PAID to " + newStatus);
                }
                break;
            default:
                throw new IllegalStateException("Cannot transition from " + this.status + " to " + newStatus);
        }

        this.status = newStatus;
    }


    public void applyDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("discount amount Invalid ={}",discount);
            throw new IllegalStateException("할인 금액이 유효하지 않습니다.");
        }
        this.discountAmount = discount;
        this.finalAmount = this.totalAmount.subtract(this.discountAmount);
    }

}
