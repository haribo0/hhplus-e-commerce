package kr.hhplus.be.server.domain.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class CouponPolicyTest {

    private CouponPolicy flatCouponPolicy;

    @BeforeEach
    void setUp() {
        // Flat 쿠폰 타입
        flatCouponPolicy = new CouponPolicy(
                1L,                     // ID
                "Flat Discount Coupon",  // 이름
                CouponType.FLAT,         // 쿠폰 타입 (Flat)
                BigDecimal.valueOf(1000), // 할인 금액을 BigDecimal로
                BigDecimal.valueOf(5000), // 최소 주문 금액
                BigDecimal.valueOf(10000), // 최대 할인 금액
                CouponPolicyStatus.ACTIVE, // 상태
                100,                     // 총 발행 수량 (정수)
                0,                       // 이미 발행된 쿠폰 수 (정수)
                LocalDateTime.now(),     // 시작일
                LocalDateTime.now().plusDays(10)  // 만료일
        );
    }

    @Test
    @DisplayName("쿠폰의 발행 수를 증가시키면 발행 수량이 1 증가한다")
    void issue_ShouldIncreaseIssuedCount_WhenValid_WithFlatCoupon() {
        // given
        int initialIssuedCount = flatCouponPolicy.getIssuedCount();

        // when
        flatCouponPolicy.issue();

        // then
        assertThat(flatCouponPolicy.getIssuedCount()).isEqualTo(initialIssuedCount + 1);
    }

    @Test
    @DisplayName("쿠폰의 발행 수가 총 수량을 초과하면 예외가 발생한다")
    void issue_ShouldThrowException_WhenIssuedCountExceedsTotalCount_WithFlatCoupon() {
        // given
        flatCouponPolicy = new CouponPolicy(
                1L, "Flat Coupon", CouponType.FLAT, BigDecimal.valueOf(1000), BigDecimal.valueOf(5000), BigDecimal.valueOf(10000),
                CouponPolicyStatus.ACTIVE, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(10)
        );

        // when & then
        assertThatThrownBy(() -> flatCouponPolicy.issue())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("쿠폰의 발행 수를 감소시키면 발행 수량이 1 감소한다")
    void decrementIssuedCount_ShouldDecreaseIssuedCount_WhenValid_WithFlatCoupon() {
        // given
        flatCouponPolicy.issue(); // 발행 수량 1 증가
        int initialIssuedCount = flatCouponPolicy.getIssuedCount();

        // when
        flatCouponPolicy.decrementIssuedCount();

        // then
        assertThat(flatCouponPolicy.getIssuedCount()).isEqualTo(initialIssuedCount - 1);
    }

    @Test
    @DisplayName("쿠폰의 발행 수가 0일 때 감소시키면 예외가 발생한다")
    void decrementIssuedCount_ShouldThrowException_WhenNoIssuedCoupons_WithFlatCoupon() {
        // when & then
        assertThatThrownBy(() -> flatCouponPolicy.decrementIssuedCount())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("쿠폰이 소진 상태일 때 발급을 시도하면 예외가 발생한다")
    void issue_ShouldThrowException_WhenCouponIsExhausted_WithCoupon() {
        // given
        flatCouponPolicy = new CouponPolicy(
                1L, "Flat Coupon", CouponType.FLAT, BigDecimal.valueOf(1000), BigDecimal.valueOf(5000), BigDecimal.valueOf(10000),
                CouponPolicyStatus.EXHAUSTED, 100, 100, LocalDateTime.now(), LocalDateTime.now().plusDays(10)
        );

        // when & then
        assertThatThrownBy(() -> flatCouponPolicy.issue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("쿠폰 발급 수량 초과");
    }

    @Test
    @DisplayName("쿠폰이 비활상태일 때 발급을 시도하면 예외가 발생한다")
    void issue_ShouldThrowException_WhenCouponIsInactive_WithCoupon() {
        // given
        flatCouponPolicy = new CouponPolicy(
                1L, "Flat Coupon", CouponType.FLAT, BigDecimal.valueOf(1000), BigDecimal.valueOf(5000), BigDecimal.valueOf(10000),
                CouponPolicyStatus.EXHAUSTED, 100, 100, LocalDateTime.now(), LocalDateTime.now().plusDays(10)
        );

        // when & then
        assertThatThrownBy(() -> flatCouponPolicy.issue())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("쿠폰의 발행 수량이 유효하면 발급이 정상적으로 된다")
    void issue_ShouldNotThrowException_WhenIssuable_WithFlatCoupon() {
        // given
        flatCouponPolicy.issue(); // 발행 수량 1 증가

        // when & then
        flatCouponPolicy.issue();  // 예외가 발생하지 않아야 한다.
    }
}
