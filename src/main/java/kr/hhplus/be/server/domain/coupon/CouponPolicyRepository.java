package kr.hhplus.be.server.domain.coupon;

import java.util.Optional;

public interface CouponPolicyRepository {
    CouponPolicy save(CouponPolicy couponPolicy);

    Optional<CouponPolicy> findById(Long id);

    Optional<CouponPolicy> findByIdWithLock(Long id);
}