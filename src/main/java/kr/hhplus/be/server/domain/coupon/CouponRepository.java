package kr.hhplus.be.server.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(Long id);

    Optional<Coupon> findByIdWithLock(Long id);
}
