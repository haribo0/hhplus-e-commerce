package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponStatus;

import java.time.LocalDateTime;

public record CouponInfo(
        Long id,
        Long userId,
        Long couponPolicyId,
        CouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
) {

    // Coupon 엔티티를 CouponInfo로 변환하는 메서드
    public static CouponInfo from(Coupon coupon) {
        return new CouponInfo(
                coupon.getId(),
                coupon.getUserId(),
                coupon.getCouponPolicy().getId(),
                coupon.getStatus(),
                coupon.getIssuedAt(),
                coupon.getUsedAt(),
                coupon.getExpiredAt()
        );
    }
}