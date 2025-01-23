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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @BeforeEach
    void setUp() {
        // 초기 데이터 설정
    }

    @Test
    @DisplayName("유효한 쿠폰 정책으로 쿠폰을 발급하면 성공한다")
    void issue_whenValidPolicy_thenSuccess() {
        // given
        Long userId =1L;
        BigDecimal discount = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 10;
        CouponPolicy couponPolicy = CouponFixture.flatCouponPolicy(min,discount,quantity);
        CouponPolicy policy = couponPolicyRepository.save(couponPolicy);
        CouponCommand command = new CouponCommand(userId, policy.getId());

        // when
        CouponInfo couponInfo = couponService.issue(command);

        // then
        assertThat(couponInfo).isNotNull();
        assertThat(couponInfo.couponPolicyId()).isEqualTo(couponPolicy.getId());
        assertThat(couponInfo.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("중복 쿠폰 발급 시 예외가 발생한다")
    void issue_whenDuplicateCoupon_thenThrowsException() {
        // given
        Long userId = 1L;
        BigDecimal discount = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 10;
        CouponPolicy couponPolicy = CouponFixture.flatCouponPolicy(min,discount,quantity);
        CouponPolicy policy = couponPolicyRepository.save(couponPolicy);
        CouponCommand command = new CouponCommand(userId, policy.getId());

        // 첫 번째 발급 성공
        couponService.issue(command);

        // when & then
        assertThatThrownBy(() -> couponService.issue(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 해당 쿠폰이 발급되었습니다.");
    }

    @Test
    @Transactional
    @DisplayName("정액 할인 쿠폰으로 올바른 할인 금액을 반환한다")
    void calculateDiscount_whenFlatCoupon_thenReturnsCorrectDiscount() {
        // given
        Long userId = 1L;
        BigDecimal discountAmt = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 10;
        CouponPolicy couponPolicy = couponPolicyRepository.save(CouponFixture.flatCouponPolicy(min, discountAmt, quantity));
        Coupon coupon = couponRepository.save(CouponFixture.coupon(userId,couponPolicy));

        // when
        BigDecimal discount = couponService.calculateDiscount(BigDecimal.valueOf(15000), coupon.getId());

        // then
        assertThat(discount).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @Transactional
    @DisplayName("최소 주문 금액 조건을 충족하지 못하면 예외가 발생한다")
    void calculateDiscount_whenBelowMinOrderAmount_thenThrowsException() {
        // given
        Long userId = 1L;
        BigDecimal discountAmt = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 10;
        CouponPolicy couponPolicy = couponPolicyRepository.save(CouponFixture.flatCouponPolicy(min, discountAmt, quantity));
        Coupon coupon = couponRepository.save(CouponFixture.coupon(userId,couponPolicy));

        // when & then
        assertThatThrownBy(() -> couponService.calculateDiscount(BigDecimal.valueOf(500), coupon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
