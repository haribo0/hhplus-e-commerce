package kr.hhplus.be.server.application.coupon;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CouponService {

    final CouponRepository couponRepository;
    final CouponPolicyRepository couponPolicyRepository;

    @Transactional
    public CouponInfo issue(CouponCommand command){
        try {
            CouponPolicy couponPolicy = couponPolicyRepository.findByIdWithLock(command.couponPolicyId())
                    .orElseThrow(() -> new IllegalArgumentException("Coupon not found for id: " + command.couponPolicyId()));
            couponPolicy.validateIssuance();
            Coupon coupon = Coupon.issue(command.userId(), couponPolicy);
            couponRepository.save(coupon);
            couponPolicy.incrementIssuedCount();
            couponPolicyRepository.save(couponPolicy);
            return CouponInfo.from(coupon);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 해당 쿠폰이 발급되었습니다.", e);
        }
    }


    public BigDecimal calculateDiscount(BigDecimal totalPrice, Long couponId) {

        if(couponId==null) return BigDecimal.ZERO;

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(()->new IllegalArgumentException("쿠폰이 유효하지 않습니다."));
        CouponPolicy policy = coupon.getCouponPolicy();

        // 쿠폰 만료 처리
        coupon.validateExpiration();

        // 최소 주문 금액 확인
        if (totalPrice.compareTo(policy.getMinOrderAmount()) < 0) {
            throw new IllegalStateException("주문 금액이 쿠폰 사용 조건을 충족하지 않습니다.");
        }


        BigDecimal discount;
        if (policy.getType() == CouponType.FLAT) {
            discount = policy.getDiscountValue(); // Flat 할인은 BigDecimal로 처리
        } else { // CouponType.PERCENT
            discount = totalPrice.multiply(policy.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_UP);
            if (policy.getMaxDiscountAmount() != null) {
                discount = discount.min(policy.getMaxDiscountAmount());
            }
        }

        // 쿠폰 사용 처리
        coupon.use();

        return discount.min(totalPrice);
    }

}
