package kr.hhplus.be.server.interfaces.coupon.api;

import kr.hhplus.be.server.application.coupon.CouponCommand;

public record CouponRequest(Long userId, Long couponPolicyId) {

    public static CouponCommand toCommand(CouponRequest request) {
        return new CouponCommand(request.userId(), request.couponPolicyId());
    }

}
