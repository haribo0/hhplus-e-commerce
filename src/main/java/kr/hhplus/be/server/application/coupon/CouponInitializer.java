package kr.hhplus.be.server.application.coupon;
import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.domain.coupon.CouponPolicy;
import kr.hhplus.be.server.domain.coupon.CouponPolicyRepository;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponInitializer {

    private final RedisTemplate<String, String> redisTemplate;
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;

    private static final String COUPON_COUNT_KEY = "coupon:%s:count";
    private static final String COUPON_ISSUED_KEY = "coupon:%s:issued";

    /**
     * 서버 시작 시 쿠폰 정보를 Redis에 반영
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCoupons() {
        List<CouponPolicy> policies = couponPolicyRepository.findAll();
        for (CouponPolicy policy : policies) {
            Long policyId = policy.getId();
            String redisCountKey = String.format(COUPON_COUNT_KEY, policyId);
            String redisIssuedKey = String.format(COUPON_ISSUED_KEY, policyId);

            // 1. DB에서 현재 발급 개수 확인
            int issuedCoupons = couponRepository.countByPolicyId(policyId);
            int totalCoupons = policy.getTotalCount();
            int remainingCoupons = totalCoupons - issuedCoupons;

            // 2. Redis에 남은 쿠폰 개수 설정
            redisTemplate.opsForValue().set(redisCountKey, String.valueOf(remainingCoupons));

            // 3. Redis에 발급된 사용자 ID 추가
            List<Long> issuedUsers = couponRepository.findAllIssuedUserIdsByPolicyId(policyId);
            for (Long userId : issuedUsers) {
                redisTemplate.opsForSet().add(redisIssuedKey, userId.toString());
            }

            log.info("쿠폰 초기화 완료 - policyId: {}, 총 쿠폰: {}, 발급된 쿠폰: {}, 남은 쿠폰: {}",
                    policyId, totalCoupons, issuedCoupons, remainingCoupons);
        }
    }
}
