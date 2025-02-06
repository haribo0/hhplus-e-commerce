package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.LongAdder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;


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


//    @Test
//    @DisplayName("선착순 20개 쿠폰을 동시에 50명이 발급 요청하면 20명만 성공적으로 발급받는다 - 속도 측정")
//    void issue_concurrentRequests_shouldLimitToTotalCount_withTiming() throws InterruptedException {
//        int threadCount = 50; // 동시 요청 수
//        ExecutorService executorService = Executors.newFixedThreadPool(20);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failureCount = new AtomicInteger();
//        LongAdder totalTime = new LongAdder(); // 전체 처리 시간 누적
//        List<Long> individualTimes = Collections.synchronizedList(new ArrayList<>()); // 개별 요청 처리 시간 저장
//
//        for (int i = 0; i < threadCount; i++) {
//            final int userId = i + 1;
//            executorService.submit(() -> {
//                long startTime = System.nanoTime(); // 시작 시간 기록
//                try {
//                    couponService.issue(new CouponCommand((long) userId, couponPolicy.getId()));
//                    successCount.incrementAndGet(); // 성공적으로 발급받은 경우
//                } catch (Exception e) {
//                    failureCount.incrementAndGet(); // 발급 실패한 경우
//                } finally {
//                    long elapsedTime = System.nanoTime() - startTime; // 처리 시간 계산
//                    totalTime.add(elapsedTime); // 총 처리 시간에 추가
//                    individualTimes.add(elapsedTime); // 개별 요청 시간 저장
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // 모든 스레드가 종료될 때까지 대기
//        executorService.shutdown();
//
//        // 전체 처리 시간 계산 (밀리초로 변환)
//        long totalMillis = totalTime.sum() / 1_000_000;
//
//        // 요청 시간 분석
//        long maxTime = individualTimes.stream().mapToLong(Long::longValue).max().orElse(0) / 1_000_000;
//        long minTime = individualTimes.stream().mapToLong(Long::longValue).min().orElse(0) / 1_000_000;
//        double avgTime = individualTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000;
//
//        // 결과 검증
//        CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
//        assertThat(updatedPolicy.getIssuedCount()).isEqualTo(20); // 발급된 수량이 최대치인지 확인
//        assertThat(successCount.get()).isEqualTo(20); // 성공한 요청 수
//        assertThat(failureCount.get()).isEqualTo(30); // 실패한 요청 수
//
//        // 성능 결과 출력
//        System.out.println("총 처리 시간: " + totalMillis + "ms");
//        System.out.println("최소 처리 시간: " + minTime + "ms");
//        System.out.println("최대 처리 시간: " + maxTime + "ms");
//        System.out.println("평균 처리 시간: " + avgTime + "ms");
//    }

}
