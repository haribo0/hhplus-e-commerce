package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "coupon",
    uniqueConstraints = @UniqueConstraint(
            name = "uq_user_coupon_policy",
            columnNames = {"user_id", "coupon_policy_id"}
    )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_policy_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CouponPolicy couponPolicy;

    @Enumerated(EnumType.STRING)
    private CouponStatus status; // ISSUED, USED, EXPIRED, CANCELLED

    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    private LocalDateTime expiredAt;



    /**
     * 새로운 쿠폰 생성
     *
     * @param userId 사용자 ID
     * @param couponPolicy 쿠폰 정책
     * @return 새로 생성된 쿠폰
     */
    public static Coupon issue(Long userId, CouponPolicy couponPolicy) {
        return Coupon.builder()
                .userId(userId)
                .couponPolicy(couponPolicy)
                .status(CouponStatus.ISSUED) // 기본 상태는 ISSUED
                .issuedAt(LocalDateTime.now())
                .build();
    }



    /**
     * 쿠폰 만료 처리
     */
    public void validateExpiration() {
        if (couponPolicy.getExpirationDate().isBefore(LocalDateTime.now())) {
            this.status = CouponStatus.EXPIRED;
            this.expiredAt = LocalDateTime.now();
            throw new IllegalStateException("쿠폰의 사용기간이 만료되었습니다.");
        }
    }

    /**
     * 쿠폰 사용 처리
     */
    public void use() {

        if (this.status == CouponStatus.EXPIRED) {
            throw new IllegalStateException("쿠폰의 사용기간이 만료되었습니다.");
        }else if(this.status == CouponStatus.USED){
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

}
