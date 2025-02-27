package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.infra.coupon.CouponJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CouponSchedulerIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private CouponScheduler couponScheduler;
    @Autowired
    private CouponPolicyRepository couponPolicyRepository;
    @Autowired
    private CouponJpaRepository couponRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CouponInitializer couponInitializer;

    @Test
    @DisplayName("통합 테스트: Redis 대기열에서 선착순으로 쿠폰 발급이 진행된다")
    void testCouponIssuance() {
        // given
        CouponPolicy policy = couponPolicyRepository.save(CouponPolicy.builder()
                .name("테스트 쿠폰")
                .type(CouponType.FLAT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.valueOf(30000))
                .maxDiscountAmount(BigDecimal.valueOf(10000))
                .status(CouponPolicyStatus.ACTIVE)
                .totalCount(10)
                .issuedCount(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .expirationDate(LocalDateTime.now().plusDays(7))
                .build()
        );
        String redisRequestKey = String.format("coupon:%s:request", policy.getId());

        redisTemplate.opsForZSet().add(redisRequestKey, "100", System.currentTimeMillis());
        redisTemplate.opsForZSet().add(redisRequestKey, "200", System.currentTimeMillis());

        couponInitializer.initializeCoupons();


        // when
        couponScheduler.processCouponQueue();

        // then
        List<Coupon> issuedCoupons = couponRepository.findAll();
        assertThat(issuedCoupons).hasSize(2);
    }
}
