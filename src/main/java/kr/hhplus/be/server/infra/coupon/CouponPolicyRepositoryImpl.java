package kr.hhplus.be.server.infra.coupon;


import kr.hhplus.be.server.domain.coupon.CouponPolicy;
import kr.hhplus.be.server.domain.coupon.CouponPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponPolicyRepositoryImpl implements CouponPolicyRepository {

    private final CouponPolicyJpaRepository jpaRepository;


    @Override
    public CouponPolicy save(CouponPolicy couponPolicy) {
        return jpaRepository.save(couponPolicy);
    }

    @Override
    public Optional<CouponPolicy> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<CouponPolicy> findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id);
    }
}
