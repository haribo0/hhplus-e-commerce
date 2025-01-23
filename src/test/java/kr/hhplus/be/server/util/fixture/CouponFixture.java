package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.coupon.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponFixture() {

    public static Coupon coupon(Long userId){

        return Coupon.builder()
                .userId(userId)
                .build();
    }

    public static Coupon coupon(Long userId, CouponPolicy policy){

        return Coupon.builder()
                .userId(userId)
                .couponPolicy(policy)
                .status(CouponStatus.ISSUED)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    public static CouponPolicy flatCouponPolicy(BigDecimal discount, BigDecimal min, int quantity){

        return CouponPolicy.builder()
                .totalCount(quantity)
                .issuedCount(0)
                .discountValue(discount)
                .minOrderAmount(min)
                .type(CouponType.FLAT)
                .status(CouponPolicyStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(10))
                .build();
    }

    public static CouponPolicy rateCouponPolicy(BigDecimal discount, BigDecimal min, BigDecimal max, int quantity){

        return CouponPolicy.builder()
                .totalCount(quantity)
                .issuedCount(0)
                .discountValue(discount)
                .minOrderAmount(min)
                .type(CouponType.PERCENT)
                .status(CouponPolicyStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(10))
                .build();
    }


}
