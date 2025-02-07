package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_policy")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType type; // FLAT, PERCENT

    @Column(name = "discount_value", precision = 10, scale = 0)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", precision = 15, scale = 0)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount",precision = 15, scale = 0)
    private BigDecimal maxDiscountAmount;

    @Enumerated(EnumType.STRING)
    private CouponPolicyStatus status; // ACTIVE, INACTIVE, EXHAUSTED

    private int totalCount;

    private int issuedCount;

    private LocalDateTime startDate;

    private LocalDateTime expirationDate;

    /**
     * 쿠폰 발행 수 증가
     */
    public void issue() {
        if (this.issuedCount >= this.totalCount) {
            status = CouponPolicyStatus.EXHAUSTED;
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        this.issuedCount++;
        if(issuedCount==totalCount) status = CouponPolicyStatus.EXHAUSTED;
    }

    public void validateBeforeRequest() {
        if (this.startDate.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("아직 발급 기간이 아닙니다.");
        }
        if (this.expirationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("발급 기간이 만료되었습니다.");
        }
        if (this.status == CouponPolicyStatus.EXHAUSTED) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        if (this.status == CouponPolicyStatus.INACTIVE) {
            throw new IllegalStateException("유효한 쿠폰이 아닙니다.");
        }
    }

}
