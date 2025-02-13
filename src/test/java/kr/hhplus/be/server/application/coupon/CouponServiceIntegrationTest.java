package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.infra.coupon.CouponJpaRepository;
import kr.hhplus.be.server.infra.coupon.CouponPolicyJpaRepository;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class CouponServiceIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private CouponPolicyJpaRepository couponPolicyJpaRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponScheduler couponScheduler;

    @Autowired
    private CouponInitializer couponInitializer;

    @BeforeEach
    void setUp() {
        // 초기 데이터 설정
    }



    @Test
    @DisplayName("유효한 쿠폰 정책으로 쿠폰을 요청하면 대기열에 추가되고, 스케줄러 실행 후 정상 발급된다")
    void request_whenValidPolicy_thenSuccess() {
        // given
        Long userId = 1L;
        BigDecimal discount = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 10;
        CouponPolicy couponPolicy = CouponFixture.flatCouponPolicy(min, discount, quantity);
        CouponPolicy policy = couponPolicyRepository.save(couponPolicy);
        CouponCommand command = new CouponCommand(userId, policy.getId());

        // when
        couponService.request(command);

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Optional<Coupon> issuedCoupon = couponRepository.findByUserIdAndPolicyId(userId, policy.getId());
                    assertThat(issuedCoupon).isPresent();
                    assertThat(issuedCoupon.get().getCouponPolicy().getId()).isEqualTo(policy.getId());
                });


    }


    @Test
    @Transactional
    @DisplayName("정액 할인 쿠폰을 사용하면 올바른 할인 금액을 반환한다")
    void use_whenFlatCoupon_thenReturnsCorrectDiscount() {
        // given
        Long userId = 1L;
        BigDecimal discountAmt = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 10;
        CouponPolicy couponPolicy = couponPolicyRepository.save(CouponFixture.flatCouponPolicy(min, discountAmt, quantity));

        // 쿠폰 요청 → 스케줄러 실행 → 쿠폰 발급
        couponService.request(new CouponCommand(userId, couponPolicy.getId()));
        couponScheduler.processCouponQueue();

        // 발급된 쿠폰 조회
        Coupon issuedCoupon = couponRepository.findByUserIdAndPolicyId(userId, couponPolicy.getId()).orElseThrow();

        // when
        BigDecimal discount = couponService.use(BigDecimal.valueOf(15000), issuedCoupon.getId());

        // then
        assertThat(discount).isEqualTo(BigDecimal.valueOf(1000));
    }
}
