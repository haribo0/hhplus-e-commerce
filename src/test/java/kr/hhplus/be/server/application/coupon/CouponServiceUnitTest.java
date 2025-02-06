package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

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

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private CouponService couponService;

    private static final String COUPON_REQUEST_KEY = "coupon:request:";
    private static final String COUPON_ISSUED_KEY = "coupon:issued:";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("쿠폰 정책이 없으면 예외를 발생시킨다")
    void request_whenCouponPolicyNotFound_thenThrowsException() {
        // given
        Long couponPolicyId = 1L;
        CouponCommand command = new CouponCommand(123L, couponPolicyId);

        when(couponPolicyRepository.findById(couponPolicyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.request(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("쿠폰 요청이 정상적으로 Redis 대기열에 추가된다")
    void request_whenValidRequest_thenAddsToQueue() {
        // given
        Long couponPolicyId = 1L;
        Long userId = 123L;
        CouponCommand command = new CouponCommand(userId, couponPolicyId);
        CouponPolicy policy = mock(CouponPolicy.class);

        // Redis 키 생성 로직과 동일하게 작성
        String redisKey = String.format("coupon:%d:request", couponPolicyId);

        when(couponPolicyRepository.findById(couponPolicyId)).thenReturn(Optional.of(policy));
        when(zSetOperations.add(eq(redisKey), eq(userId.toString()), anyDouble())).thenReturn(true);

        // when
        couponService.request(command);

        // then
        verify(zSetOperations).add(eq(redisKey), eq(userId.toString()), anyDouble());
    }



    @Test
    @DisplayName("쿠폰 발급 요청이 중복되면 예외를 발생시킨다")
    void request_whenDuplicateRequest_thenThrowsException() {
        // given
        Long couponPolicyId = 1L;
        Long userId = 123L;
        CouponCommand command = new CouponCommand(userId, couponPolicyId);
        CouponPolicy policy = mock(CouponPolicy.class);

        when(couponPolicyRepository.findById(couponPolicyId)).thenReturn(Optional.of(policy));
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(1L); // 이미 존재하는 사용자
        when(setOperations.isMember(anyString(), anyString())).thenReturn(true); // 중복 발급된 사용자

        // when & then
        assertThatThrownBy(() -> couponService.request(command))
                .isInstanceOf(IllegalStateException.class);
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
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("정액 할인 쿠폰을 사용하면 정확한 할인 금액을 반환한다")
    void use_whenFlatCoupon_thenReturnsCorrectDiscount() {
        // given
        Long couponId = 1L;
        BigDecimal totalPrice = BigDecimal.valueOf(1000);
        BigDecimal discountValue = BigDecimal.valueOf(200);
        BigDecimal minOrderAmount = BigDecimal.valueOf(500);

        CouponPolicy policy = mock(CouponPolicy.class);
        Coupon coupon = mock(Coupon.class);

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
}
