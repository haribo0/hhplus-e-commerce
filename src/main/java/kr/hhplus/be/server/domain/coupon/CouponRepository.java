package kr.hhplus.be.server.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(Long id);

    Optional<Coupon> findByIdWithLock(Long id);

    int countByPolicyId(Long id);

    List<Long> findAllIssuedUserIdsByPolicyId(Long id);

    void saveAll(List<Coupon> issuedCoupons);

    Optional<Coupon> findByUserIdAndPolicyId(Long userId, Long policyId);
}
