package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.point.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointServiceIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 ID로 포인트를 조회하면 포인트를 반환한다")
    void get_whenUserIdExists_thenReturnsPoint() {
        // given
        Long userId = 1L;
        User user = UserFixture.user(userId);
        userRepository.save(user);

        // when
        Point point = pointService.get(userId);

        // then
        assertThat(point).isNotNull();
        assertThat(point.getUserId()).isEqualTo(userId);
        assertThat(point.getBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("포인트 충전 요청 시 잔액이 증가한다")
    void charge_whenValidCommand_thenIncreasesBalance() {
        // given
        Long userId = 1L;
        User user = UserFixture.user(userId);
        userRepository.save(user);
        PointCommand.Charge command = new PointCommand.Charge(userId, 500);

        // when
        Point updatedPoint = pointService.charge(command);

        // then
        assertThat(updatedPoint).isNotNull();
        assertThat(updatedPoint.getBalance()).isEqualTo(500);

        Point savedPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(savedPoint.getBalance()).isEqualTo(500);
    }

    @Test
    @DisplayName("포인트 사용 요청 시 잔액이 감소한다")
    void use_whenValidCommand_thenDecreasesBalance() {
        // given
        Long userId = 1L;
        User user = UserFixture.user(userId);
        userRepository.save(user);
        Point point = new Point(userId);
        point.charge(1000); // 초기 잔액 충전
        pointRepository.save(point);

        PointCommand.Use command = new PointCommand.Use(userId, 300);

        // when
        Point updatedPoint = pointService.use(command);

        // then
        assertThat(updatedPoint).isNotNull();
        assertThat(updatedPoint.getBalance()).isEqualTo(700);

        Point savedPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(savedPoint.getBalance()).isEqualTo(700);
    }

    @Test
    @DisplayName("사용 가능한 잔액보다 많은 금액을 사용하면 예외가 발생한다")
    void use_whenAmountExceedsBalance_thenThrowsException() {
        // given
        Long userId = 1L;
        User user = UserFixture.user(userId);
        userRepository.save(user);
        Point point = new Point(userId);
        point.charge(100); // 초기 잔액 충전
        pointRepository.save(point);

        PointCommand.Use command = new PointCommand.Use(userId, 200);

        // when & then
        assertThatThrownBy(() -> pointService.use(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("포인트 사용처리 중 문제가 발생했습니다.");
    }

    @Test
    @DisplayName("동시에 여러 포인트 충전 요청이 들어오면 정확히 처리된다")
    void charge_whenConcurrentRequests_thenHandlesProperly() throws InterruptedException {
        // given
        Long userId = 1L;
        User user = UserFixture.user(userId);
        userRepository.save(user);
        Point point = new Point(userId);
        pointRepository.save(point);

        // when
        Runnable task = () -> {
            PointCommand.Charge command = new PointCommand.Charge(userId, 100);
            pointService.charge(command);
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // then
        Point savedPoint = pointRepository.findByUserId(userId).orElseThrow();
        assertThat(savedPoint.getBalance()).isEqualTo(200); // 두 요청이 각각 100씩 충전됨
    }
}
