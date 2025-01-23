package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CouponServiceConcurrencyIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private CouponRepository couponRepository;

    private CouponPolicy couponPolicy;

    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰 정책 설정
        BigDecimal discount = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 20;
        couponPolicy = couponPolicyRepository.save(CouponFixture.flatCouponPolicy(min,discount,quantity));
    }

    @Test
    @DisplayName("선착순 20개 쿠폰을 동시에 50명이 발급 요청하면 20명만 성공적으로 발급받는다")
    void issue_concurrentRequests_shouldLimitToTotalCount() throws InterruptedException {
        int threadCount = 50; // 동시 요청 수
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final int userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponService.issue(new CouponCommand( (long) userId, couponPolicy.getId()));
                    successCount.incrementAndGet(); // 성공적으로 발급받은 경우
                } catch (Exception e) {
                    failureCount.incrementAndGet(); // 발급 실패한 경우
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        // 쿠폰 정책 확인
        CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
        assertThat(updatedPolicy.getIssuedCount()).isEqualTo(20); // 발급된 수량이 최대치인지 확인
        assertThat(successCount.get()).isEqualTo(20); // 성공한 요청 수
        assertThat(failureCount.get()).isEqualTo(30); // 실패한 요청 수
    }
}
