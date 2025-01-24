package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.PointRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.util.fixture.PointFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class PointServiceUnitTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UserRepository userRepository;

    @DisplayName("포인트 조회 기능")
    @Nested
    class GetPoint {
        @DisplayName("유저 아이디로 유저가 보유한 포인트를 조회할 수 있다.")
        @Test
        void getPointByUserId() throws Exception {
            // given
            final long userId = 1L;
            final int pointBalance = 10000;
            User user = UserFixture.user(userId);
            Point point = PointFixture.point(userId, pointBalance);
            given(pointRepository.findByUserId(userId))
                    .willReturn(Optional.of(point));
            // when
            final Point result = pointService.get(userId);
            // then
            assertThat(result).isNotNull()
                    .extracting("userId", "points")
                    .containsExactly(userId, pointBalance);
        }

        @DisplayName("유저 아이디로 보유한 포인트가 없으면 0포인트를 반환한다.")
        @Test
        void getPointByUserIdNoPoint() throws Exception {

            // given
            final long userId = 1L;
            final int emptyPoint = 0;
            User user = UserFixture.user(userId);
            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));
            given(pointRepository.findByUserId(userId))
                    .willReturn(Optional.empty());
            Point point = PointFixture.point(userId);
            given(pointRepository.save(any(Point.class)))
                    .willReturn(point);

            // when
            final Point result = pointService.get(userId);

            // then
            assertThat(result).isNotNull()
                    .extracting("userId", "points")
                    .containsExactly(userId, emptyPoint);
            then(pointRepository).should(times(1)).save(any(Point.class));
        }

        @DisplayName("존재하지 않는 유저의 포인트를 조회하면 EntityNotFoundException 예외가 발생한다.")
        @Test
        void getPointByInvalidUser() throws Exception {
            // given
            final long userId = 1L;
            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());
            // when & then
            assertThatThrownBy(() -> pointService.get(userId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @DisplayName("포인트 충전/사용 기능")
    @Nested
    class chargePoint {
        @DisplayName("포인트를 충전할 수 있다.")
        @Test
        void chargePoint() throws Exception {
            // given
            final Long userId = 1L;
            final int chargePoint = 1000;
            final int expectedPoint = chargePoint;
            final PointCommand command = PointCommand.builder()
                    .userId(userId)
                    .amount(chargePoint)
                    .build();
            final Point point = Point.createPoint(userId);
            given(pointRepository.findByUserIdWithLock(userId))
                    .willReturn(Optional.of(point));
            // when
            final Point result = pointService.charge(command);
            // then
            assertThat(result.getPoints()).isEqualTo(expectedPoint);
            then(pointHistoryRepository).should(times(1)).save(any(PointHistory.class));
        }

        @DisplayName("포인트를 사용할 수 있다.")
        @Test
        void usePoint() throws Exception {
            // given
            final Long userId = 1L;
            final int balance = 5000;
            final int usePoint = 1000;
            final int expectedPoint = balance - usePoint;
            final PointCommand command = PointCommand.builder()
                    .userId(userId)
                    .amount(usePoint)
                    .build();
            final Point point = Point.builder()
                    .userId(userId)
                    .points(balance)
                    .build();
            given(pointRepository.findByUserIdWithLock(userId))
                    .willReturn(Optional.of(point));
            // when
            final Point result = pointService.use(command);
            // then
            assertThat(result.getPoints()).isEqualTo(expectedPoint);
            then(pointHistoryRepository).should(times(1)).save(any(PointHistory.class));
        }

        @DisplayName("유효하지 않은 사용자의 포인트 충전을 요청하면 EntityNotFoundException 예외가 발생한다.")
        @Test
        void chargeInvalidUserPoint() throws Exception {
            // given
            final Long userId = 1L;
            final int amount = 1000;
            final PointCommand command = PointCommand.builder()
                    .userId(userId)
                    .amount(amount)
                    .build();
            // when
            assertThatThrownBy(() -> pointService.charge(command))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("충전 요청 포인트가 양수가 아니면 IllegalArgumentException 예외가 발생한다.")
        @Test
        void chargeWithNotPositiveChargePoint() throws Exception {
            // given
            final Long userId = 1L;
            final int pointBalance = 1000;
            final Point point = Point.builder()
                    .userId(userId)
                    .points(pointBalance)
                    .build();
            given(pointRepository.findByUserIdWithLock(userId))
                    .willReturn(Optional.of(point));
            final int chargePoint = 0;
            final PointCommand request = PointCommand.builder()
                    .userId(userId)
                    .amount(chargePoint)
                    .build();
            // when & then
            assertThatThrownBy(() -> pointService.charge(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}