package kr.hhplus.be.server.infra.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Coupon> findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id);
    }

    @Override
    public int countByPolicyId(Long id) {
        return jpaRepository.countByPolicyId(id);
    }

    @Override
    public List<Long> findAllIssuedUserIdsByPolicyId(Long id) {
        return jpaRepository.findAllIssuedUserIdsByPolicyId(id);
    }

    @Override
    public void saveAll(List<Coupon> issuedCoupons) {
        jpaRepository.saveAll(issuedCoupons);
    }

    @Override
    public Optional<Coupon> findByUserIdAndPolicyId(Long userId, Long policyId) {
        return jpaRepository.findByUserIdAndCouponPolicy_Id(userId, policyId);
    }
}
