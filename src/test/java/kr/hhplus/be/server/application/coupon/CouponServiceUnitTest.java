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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.time.Duration;
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
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("쿠폰 요청이 정상적으로 Redis 대기열에 추가된다")
    void request_whenValidRequest_thenAddsToQueue() {
        // given
        Long couponPolicyId = 1L;
        Long userId = 123L;
        CouponCommand command = new CouponCommand(userId, couponPolicyId);
        String redisCountKey = String.format("coupon:%d:count", couponPolicyId);
        String redisIssuedKey = String.format("coupon:%d:issued", couponPolicyId);
        String redisRequestKey = String.format("coupon:%d:request", couponPolicyId);
        String userLockKey = "lock:coupon:request:" + couponPolicyId + ":" + userId;

        // RedisTemplate과 관련된 mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // 쿠폰 재고 Redis 관리 키 관련 Mock 설정
        when(valueOperations.decrement(redisCountKey)).thenReturn(5L); // 쿠폰 감소 후 5개 남았다고 가정
        when(valueOperations.increment(redisCountKey)).thenReturn(6L); // 롤백 시 +1

        // 중복 요청 방지 (setIfAbsent)
        when(valueOperations.setIfAbsent(eq(userLockKey), eq("1"), any(Duration.class))).thenReturn(true);

        // Redis ZSet 추가 (대기열 등록)
        when(zSetOperations.add(eq(redisRequestKey), eq(userId.toString()), anyDouble())).thenReturn(true);

        // when
        couponService.request(command);

        // then
        verify(valueOperations).decrement(redisCountKey); // 쿠폰 수량 감소 확인
        verify(valueOperations).setIfAbsent(eq(userLockKey), eq("1"), any(Duration.class)); // 중복 요청 방지 확인
        verify(zSetOperations).add(eq(redisRequestKey), eq(userId.toString()), anyDouble()); // Redis 대기열 추가 확인
    }




    @Test
    @DisplayName("쿠폰 발급 요청이 중복되면 예외를 발생시킨다")
    void request_whenDuplicateRequest_thenThrowsException() {
        // given
        Long couponPolicyId = 1L;
        Long userId = 123L;
        CouponCommand command = new CouponCommand(userId, couponPolicyId);
        String redisCountKey = String.format("coupon:%d:count", couponPolicyId);
        String userLockKey = "lock:coupon:request:" + couponPolicyId + ":" + userId;

        // RedisTemplate 관련 Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 쿠폰 재고 관련 Mock 설정 (여기선 재고가 남아있다고 가정)
        when(valueOperations.decrement(redisCountKey)).thenReturn(5L);

        // 중복 요청 방지 (setIfAbsent가 실패하도록 설정)
        when(valueOperations.setIfAbsent(eq(userLockKey), eq("1"), any(Duration.class))).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> couponService.request(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 쿠폰을 발급 요청한 사용자입니다.");

        // 쿠폰 재고 롤백 검증
        verify(valueOperations).increment(redisCountKey);
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
