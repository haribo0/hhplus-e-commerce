package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponPolicy;
import kr.hhplus.be.server.domain.coupon.CouponPolicyRepository;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponRepository couponRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String COUPON_REQUEST_KEY = "coupon:%s:request";
    private static final String COUPON_ISSUED_KEY = "coupon:%s:issued";


    /**
     * 쿠폰 발급 스케줄러 (1초마다 실행)
     */
    @Scheduled(fixedDelay = 1000)
    public void processCouponQueue() {
        log.info("쿠폰 대기열 처리 시작");

        // 1. DB에서 모든 활성화된 쿠폰 정책 가져오기
        List<CouponPolicy> activePolicies = couponPolicyRepository.findAllActivePolicies();
        for (CouponPolicy policy : activePolicies) {
            processPolicyQueue(policy);
        }

        log.info("쿠폰 대기열 처리 완료");
    }

    private void processPolicyQueue(CouponPolicy policy) {
        Long policyId = policy.getId();
        String redisRequestKey = String.format(COUPON_REQUEST_KEY, policyId);
        String redisIssuedKey = String.format(COUPON_ISSUED_KEY, policyId);


        // 2. DB에서 현재 발급 가능한 수량 확인
        int remainingCoupons = policy.getTotalCount()-policy.getIssuedCount();
        if (remainingCoupons <= 0) {
            return; // 더 이상 발급할 쿠폰 없음
        }

        // 3. Redis에서 요청자 목록 가져오기 (발급 가능 수량만큼)
        Long queueSize = redisTemplate.opsForZSet().size(redisRequestKey);
        if (queueSize == null || queueSize == 0) {
            log.warn("쿠폰 대기열이 비어 있습니다. policyId: {}", policyId);
            return;
        }

        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(redisRequestKey, 0, remainingCoupons - 1);
        if (typedTuples == null || typedTuples.isEmpty()) {
            log.warn("ZPOPMIN 실행 후 사용자 목록이 비어 있음. policyId: {}", policyId);
            return;
        }

        List<Long> userIds = typedTuples.stream()
                .map(tuple -> Long.parseLong(tuple.getValue())) // 여기서 `.getValue()`를 사용해서 String 값 추출
                .collect(Collectors.toList());

        redisTemplate.opsForZSet().removeRange(redisRequestKey, 0, userIds.size() - 1);


        // 4. 쿠폰 발급 처리
        for (Long userId : userIds) {
            // 4-1. 중복 발급 확인
            Boolean isAlreadyIssued = redisTemplate.opsForSet().isMember(redisIssuedKey, userId.toString());
            if (Boolean.TRUE.equals(isAlreadyIssued)) {
                log.warn("이미 발급된 사용자 - userId: {}, policyId: {}", userId, policyId);
                continue;
            }

            // 4-2. 쿠폰 발급 수행
            Coupon coupon = Coupon.issue(userId, policy);
            couponRepository.save(coupon);
            policy.issue();

            // 4-3. Redis에 발급 내역 추가
            redisTemplate.opsForSet().add(redisIssuedKey, userId.toString());

            log.info("쿠폰 발급 성공 - userId: {}, policyId: {}, 남은 쿠폰: {}", userId, policyId, policy.getTotalCount()-policy.getIssuedCount());
        }

        // 5. 쿠폰 정책 저장 (발급 카운트 반영)
        couponPolicyRepository.save(policy);
    }
}
