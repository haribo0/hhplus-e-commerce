package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    private CouponScheduler couponScheduler;

    @Autowired
    private CouponInitializer couponInitializer;

    private CouponPolicy couponPolicy;


    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰 정책 설정
        BigDecimal discount = BigDecimal.valueOf(1000);
        BigDecimal min = BigDecimal.valueOf(1000);
        int quantity = 20;
        couponPolicy = couponPolicyRepository.save(CouponFixture.flatCouponPolicy(min,discount,quantity));
        couponInitializer.initializeCoupons();
    }

    @Test
    @DisplayName("선착순 20개 쿠폰을 동시에 50명이 요청하면 대기열에 정상 등록되고, 스케줄러 실행 후 20명만 성공한다")
    void request_concurrentRequests_shouldLimitToTotalCount() throws InterruptedException {
        int threadCount = 50; // 동시 요청 수
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final int userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponService.request(new CouponCommand((long) userId, couponPolicy.getId()));
                    successCount.incrementAndGet(); // 정상적으로 요청 등록된 경우
                } catch (Exception e) {
                    failureCount.incrementAndGet(); // 요청 실패한 경우
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        // 대기열에 정상 등록되었는지 확인
        assertThat(successCount.get()).isEqualTo(50);
        assertThat(failureCount.get()).isEqualTo(0);

        // ✅ 테스트에서만 **스케줄러 실행** (서비스 코드 변경 X)
        couponScheduler.processCouponQueue();

        // 발급된 쿠폰 수 검증
        CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
        assertThat(updatedPolicy.getIssuedCount()).isEqualTo(20); // 발급된 수량이 최대치인지 확인
    }

    @Test
    @DisplayName("동일 사용자가 여러 번 요청해도 한 번만 요청된다")
    void request_concurrentRequests_shouldPreventDuplicateRequests() throws InterruptedException {
        int userCount = 50; // 50명의 사용자
        int totalRequests = userCount * 2; // 각 사용자가 2번씩 요청 (100개 요청)
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateFailureCount = new AtomicInteger();

        for (int i = 0; i < userCount; i++) {
            final int userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponService.request(new CouponCommand((long) userId, couponPolicy.getId()));
                    successCount.incrementAndGet(); // 정상적으로 요청 등록된 경우
                } catch (Exception e) {
                    duplicateFailureCount.incrementAndGet(); // 중복 요청으로 실패한 경우
                } finally {
                    latch.countDown();
                }
            });

            executorService.submit(() -> {
                try {
                    couponService.request(new CouponCommand((long) userId, couponPolicy.getId())); // 같은 유저가 2번 요청
                    successCount.incrementAndGet(); // (이론상 여기 도달하면 안 됨)
                } catch (Exception e) {
                    duplicateFailureCount.incrementAndGet(); // 중복 요청으로 실패한 경우
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        // 모든 사용자 요청이 하나만 정상 등록되어야 함
        assertThat(successCount.get()).isEqualTo(50);
        assertThat(duplicateFailureCount.get()).isEqualTo(50);

        couponScheduler.processCouponQueue();

        // 발급된 쿠폰 수 검증
        CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
        assertThat(updatedPolicy.getIssuedCount()).isEqualTo(20); // 발급된 수량이 최대치인지 확인
    }

//    @Test
//    @DisplayName("선착순 20개 쿠폰을 동시에 50명이 발급 요청하면 20명만 성공적으로 발급받는다")
//    void issue_concurrentRequests_shouldLimitToTotalCount() throws InterruptedException {
//        int threadCount = 50; // 동시 요청 수
//        ExecutorService executorService = Executors.newFixedThreadPool(20);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failureCount = new AtomicInteger();
//
//
//        for (int i = 0; i < threadCount; i++) {
//            final int userId = i + 1;
//            executorService.submit(() -> {
//                try {
//                    couponService.issue(new CouponCommand( (long) userId, couponPolicy.getId()));
//                    successCount.incrementAndGet(); // 성공적으로 발급받은 경우
//                } catch (Exception e) {
//                    failureCount.incrementAndGet(); // 발급 실패한 경우
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 스레드가 종료될 때까지 대기
//        executorService.shutdown();
//
//        // 쿠폰 정책 확인
//        CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
//        assertThat(updatedPolicy.getIssuedCount()).isEqualTo(20); // 발급된 수량이 최대치인지 확인
//        assertThat(successCount.get()).isEqualTo(20); // 성공한 요청 수
//        assertThat(failureCount.get()).isEqualTo(30); // 실패한 요청 수
//    }
//
//    @Test
//    @DisplayName("동일 사용자가 여러 번 요청해도 한 번만 쿠폰을 받을 수 있다")
//    void issue_concurrentRequests_shouldPreventDuplicateRequests() throws InterruptedException {
//        int userCount = 50; // 50명의 사용자
//        int totalRequests = userCount * 2; // 각 사용자가 2번씩 요청 (100개 요청)
//        ExecutorService executorService = Executors.newFixedThreadPool(20);
//        CountDownLatch latch = new CountDownLatch(totalRequests);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failureCount = new AtomicInteger();
//
//        for (int i = 0; i < userCount; i++) {
//            final int userId = i + 1 + 100; // 중복방지용 100 더해주기;
//            executorService.submit(() -> {
//                try {
//                    couponService.issue(new CouponCommand((long) userId, couponPolicy.getId()));
//                    successCount.incrementAndGet(); // 성공적으로 발급받은 경우
//                } catch (Exception e) {
//                    failureCount.incrementAndGet(); // 발급 실패한 경우
//                } finally {
//                    latch.countDown();
//                }
//            });
//
//            executorService.submit(() -> {
//                try {
//                    couponService.issue(new CouponCommand((long) userId, couponPolicy.getId())); // 같은 유저가 2번 요청
//                    successCount.incrementAndGet(); // 성공적으로 발급받은 경우
//                } catch (Exception e) {
//                    failureCount.incrementAndGet(); // 발급 실패한 경우
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 스레드가 종료될 때까지 대기
//        executorService.shutdown();
//
//        // 쿠폰 정책 확인
//        CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
//        assertThat(updatedPolicy.getIssuedCount()).isEqualTo(20); // 발급된 수량이 최대치인지 확인
//        assertThat(successCount.get()).isEqualTo(20); // 성공한 요청 수
//        assertThat(failureCount.get()).isEqualTo(80); // 실패한 요청 수 (100개 요청 중 20개만 성공)
//    }

}
