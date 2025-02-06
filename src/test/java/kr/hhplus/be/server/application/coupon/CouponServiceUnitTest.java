package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
class CouponServiceUnitTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponPolicyRepository couponPolicyRepository;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("쿠폰 정책이 없으면 예외를 발생시킨다")
    void issue_whenCouponPolicyNotFound_thenThrowsException() {
        // given
        Long couponPolicyId = 1L;
        CouponCommand command = new CouponCommand(couponPolicyId, 123L);

        when(couponPolicyRepository.findByIdWithLock(couponPolicyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.issue(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 발급된 쿠폰이면 예외를 발생시킨다")
    void issue_whenCouponAlreadyIssued_thenThrowsException() {
        // given
        Long couponPolicyId = 1L;
        Long userId = 1L;
        CouponCommand command = new CouponCommand(userId, couponPolicyId);
        CouponPolicy policy = mock(CouponPolicy.class);

        when(couponPolicyRepository.findByIdWithLock(couponPolicyId)).thenReturn(Optional.of(policy));
        doThrow(new DataIntegrityViolationException("Duplicate entry")).when(couponRepository).save(any(Coupon.class));

        // when & then
        assertThatThrownBy(() -> couponService.issue(command))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("쿠폰 발급 요청이 성공하면 쿠폰 정보를 반환한다")
    void issue_whenValidRequest_thenReturnsCouponInfo() {
        // given
        Long couponPolicyId = 1L;
        Long userId = 123L;
        CouponCommand command = new CouponCommand(userId, couponPolicyId);
        CouponPolicy policy = mock(CouponPolicy.class);
        Coupon coupon = mock(Coupon.class);

        when(couponPolicyRepository.findByIdWithLock(couponPolicyId)).thenReturn(Optional.of(policy));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        when(coupon.getId()).thenReturn(1L);
        when(coupon.getUserId()).thenReturn(userId);

        // when
        CouponInfo result = couponService.issue(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        verify(couponRepository).save(any(Coupon.class));
        verify(couponPolicyRepository).save(policy);
    }


    @Test
    @DisplayName("쿠폰 ID가 null이면 할인 금액은 0이다")
    void use_whenCouponIdIsNull_thenReturnsZero() {
        // given
        BigDecimal totalPrice = BigDecimal.valueOf(1000);

        // when
        BigDecimal discount = couponService.use(totalPrice, null);

        // then
        assertThat(discount).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("유효하지 않은 쿠폰 ID를 사용하면 예외를 발생시킨다")
    void use_whenCouponIdIsInvalid_thenThrowsException() {
        // given
        Long invalidCouponId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(1000);

        when(couponRepository.findById(invalidCouponId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.use(totalPrice, invalidCouponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("쿠폰이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("주문 금액이 최소 금액 미만이면 예외를 발생시킨다")
    void use_whenTotalPriceBelowMinOrderAmount_thenThrowsException() {
        // given
        Long couponId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(500);
        BigDecimal minOrderAmount = BigDecimal.valueOf(1000);
        BigDecimal discountValue = BigDecimal.valueOf(100);
        CouponPolicy policy = mock(CouponPolicy.class);
        Coupon coupon = mock(Coupon.class);
        when(policy.getMinOrderAmount()).thenReturn(minOrderAmount);
        when(policy.getDiscountValue()).thenReturn(discountValue);
        when(coupon.getCouponPolicy()).thenReturn(policy);
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        // when & then
        assertThatThrownBy(() -> couponService.use(totalPrice, couponId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정액 할인 쿠폰을 사용하면 정확한 할인 금액을 반환한다")
    void use_whenFlatCoupon_thenReturnsCorrectDiscount() {
        // given
        Long couponId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(1000);
        BigDecimal discountValue = BigDecimal.valueOf(200);
        BigDecimal minOrderAmount = BigDecimal.valueOf(500);

        // Mock 객체 생성
        CouponPolicy policy = mock(CouponPolicy.class);
        Coupon coupon = mock(Coupon.class);

        // 스텁 동작 정의
        when(policy.getDiscountValue()).thenReturn(discountValue);
        when(policy.getMinOrderAmount()).thenReturn(minOrderAmount);
        when(policy.getType()).thenReturn(CouponType.FLAT);

        when(coupon.getCouponPolicy()).thenReturn(policy);
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        // when
        BigDecimal discount = couponService.use(totalPrice, couponId);

        // then
        assertThat(discount)
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(discountValue);
        verify(coupon).use(); // 쿠폰 사용 여부 검증
    }


    @Test
    @DisplayName("퍼센트 할인 쿠폰을 사용하면 정확한 할인 금액을 반환한다")
    void use_whenPercentCoupon_thenReturnsCorrectDiscount() {
        // given
        Long couponId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(1000);
        BigDecimal discountVal = BigDecimal.valueOf(10); // 10%
        BigDecimal minAmount = BigDecimal.valueOf(500);
        BigDecimal maxAmount = BigDecimal.valueOf(10000);
        int quantity = 10;
        CouponPolicy policy = mock(CouponPolicy.class);
        Coupon coupon = mock(Coupon.class);
        when(policy.getDiscountValue()).thenReturn(discountVal);
        when(policy.getMinOrderAmount()).thenReturn(minAmount);
        when(policy.getMaxDiscountAmount()).thenReturn(maxAmount);
        when(policy.getType()).thenReturn(CouponType.PERCENT);
        when(coupon.getCouponPolicy()).thenReturn(policy);
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        // when
        BigDecimal discount = couponService.use(totalPrice, couponId);

        // then
        assertThat(discount)
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(BigDecimal.valueOf(100));
        verify(coupon).use();
    }

    @Test
    @DisplayName("퍼센트 할인 쿠폰이 최대 할인 금액을 초과하면 최대 금액을 반환한다")
    void use_whenPercentCouponExceedsMax_thenReturnsMaxDiscount() {
        // given
        Long couponId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(50000);
        BigDecimal discountVal = BigDecimal.valueOf(10); // 10%
        BigDecimal minAmount = BigDecimal.valueOf(500);
        BigDecimal maxAmount = BigDecimal.valueOf(1000);
        CouponPolicy policy = mock(CouponPolicy.class);
        Coupon coupon = mock(Coupon.class);
        when(policy.getDiscountValue()).thenReturn(discountVal);
        when(policy.getMinOrderAmount()).thenReturn(minAmount);
        when(policy.getMaxDiscountAmount()).thenReturn(maxAmount);
        when(policy.getType()).thenReturn(CouponType.PERCENT);

        when(coupon.getCouponPolicy()).thenReturn(policy);
        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        // when
        BigDecimal discount = couponService.use(totalPrice, couponId);

        // then
        assertThat(discount)
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(maxAmount); // 최대 할인 금액을 검증
        verify(coupon).use(); // 쿠폰 사용 여부를 검증
    }

}
