package kr.hhplus.be.server.infra.coupon;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(Long id);


    /**
     * 특정 쿠폰 정책 ID에 대한 발급된 쿠폰 수 조회
     */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.couponPolicy.id = :policyId")
    int countByPolicyId(@Param("policyId") Long policyId);

    /**
     * 특정 쿠폰 정책 ID에 대한 모든 발급된 사용자 ID 조회
     */
    @Query("SELECT c.userId FROM Coupon c WHERE c.couponPolicy.id = :policyId")
    List<Long> findAllIssuedUserIdsByPolicyId(@Param("policyId") Long policyId);

    Optional<Coupon> findByUserIdAndCouponPolicy_Id(Long userId, Long policyId);
}
