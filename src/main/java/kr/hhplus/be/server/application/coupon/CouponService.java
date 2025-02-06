package kr.hhplus.be.server.application.coupon;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String COUPON_REQUEST_KEY = "coupon:%s:request";
    private static final String COUPON_ISSUED_KEY = "coupon:%s:issued";

    /**
     * 선착순 쿠폰 요청 (사용자가 신청할 때 실행됨)
     */
    public void request(CouponCommand command) {
        Long policyId = command.couponPolicyId();
        Long userId = command.userId();
        String redisRequestKey = String.format(COUPON_REQUEST_KEY, policyId);
        String redisIssuedKey = String.format(COUPON_ISSUED_KEY, policyId);
        String userLockKey = "lock:coupon:request:" + policyId + ":" + userId; // 사용자별 중복 요청 방지용 키

        // 1. 쿠폰 정책 조회 후 사전 검증
        CouponPolicy policy = couponPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰 정책입니다."));
        policy.validateBeforeRequest(); // 쿠폰 정책 검증

        // 쿠폰 잔여 수량 확인
        int remainingCoupons = policy.getTotalCount() - policy.getIssuedCount();
        if (remainingCoupons <= 0) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다."); // 즉시 실패 반환
        }

        // 2. 중복 발급 여부 확인
        Boolean isNewRequest = redisTemplate.opsForValue().setIfAbsent(userLockKey, "1");
        if (Boolean.FALSE.equals(isNewRequest)) {
            throw new IllegalStateException("이미 쿠폰을 발급 요청한 사용자입니다.");
        }


        // 3. Redis Sorted Set에 요청 추가 (score: 요청 시각)
        redisTemplate.opsForZSet().add(redisRequestKey, userId.toString(), Instant.now().toEpochMilli());

        log.info("쿠폰 요청 등록 - userId: {}, policyId: {}", userId, policyId);
    }




    public BigDecimal use(BigDecimal totalPrice, Long couponId) {

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
