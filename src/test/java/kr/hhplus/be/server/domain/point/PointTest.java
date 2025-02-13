package kr.hhplus.be.server.domain.point;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointTest {

    @DisplayName("포인트 객체 생성")
    @Nested
    class createPointA {
        @DisplayName("유저 아이디로 보유포인트가 0인 새 포인트 객체를 생성할 수 있다.")
        @Test
        void createPoint() {
            // given
            final Long userId = 1L;
            final int expectedPoint = 0;
            // when
            final Point result = Point.createPoint(userId);
            // then
            assertThat(result).isNotNull()
                    .extracting("userId", "points")
                    .containsExactly(userId, expectedPoint);
        }
    }

    @DisplayName("포인트 충전")
    @Nested
    class chargePoint {
        @DisplayName("포인트 충전을 성공하면 기존 포인트와 충전된 금액의 합을 보유한다.")
        @Test
        void charge() {
            // given
            final Long userId = 1L;
            final int previousBalance = 1000;
            final int chargeAmount = 1000;
            final int expectedBalance = previousBalance + chargeAmount;
            final Point point = Point.builder()
                    .userId(userId)
                    .points(previousBalance)
                    .build();
            // when
            point.charge(chargeAmount);
            // then
            assertThat(point).isNotNull()
                    .extracting("userId", "points")
                    .containsExactly(userId, expectedBalance);
        }
        @DisplayName("충전 금액이 양수가 아닐 경우 IllegalArgumentException이 발생한다.")
        @Test
        void chargeInvalidAmount() {
            // given
            final Long userId = 1L;
            final int previousBalance = 1000;
            final int chargeAmount = -1000;
            final Point point = Point.builder()
                    .userId(userId)
                    .points(previousBalance)
                    .build();
            // when&then
            assertThatThrownBy(() -> point.charge(chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
    @DisplayName("포인트 사용")
    @Nested
    class usePoint {
        @DisplayName("포인트 사용을 성공 시 기존 포인트에서 사용포인트가 차감된 금액을 보유한다.")
        @Test
        void use() {
            // given
            final Long userId = 1L;
            final int previousBalance = 1000;
            final int useAmount = 1000;
            final int expectedBalance = previousBalance - useAmount;
            final Point point = Point.builder()
                    .userId(userId)
                    .points(previousBalance)
                    .build();
            // when
            point.use(useAmount);
            // then
            assertThat(point).isNotNull()
                    .extracting("userId", "points")
                    .containsExactly(userId, expectedBalance);
        }
        @DisplayName("사용 금액이 양수가 아닐 경우 IllegalArgumentException이 발생한다.")
        @Test
        void useInvalidAmount() {
            // given
            final Long userId = 1L;
            final int previousBalance = 1000;
            final int useAmount = -1000;
            final Point point = Point.builder()
                    .userId(userId)
                    .points(previousBalance)
                    .build();
            // when&then
            assertThatThrownBy(() -> point.use(useAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        @DisplayName("사용 금액이 보유포인트 보다 클 경우 IllegalStateException 발생한다.")
        @Test
        void useInsufficientAmount() {
            // given
            final Long userId = 1L;
            final int previousBalance = 2000;
            final int useAmount = 3000;
            final Point point = Point.builder()
                    .userId(userId)
                    .points(previousBalance)
                    .build();
            // when&then
            assertThatThrownBy(() -> point.use(useAmount))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

}