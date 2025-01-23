package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.user.User;

public record PointFixture() {

    public static Point point(Long userId){
        return Point.builder()
                .userId(userId)
                .points(0)
                .build();
    }

    public static Point point(Long userId, int point){
        return Point.builder()
                .userId(userId)
                .points(point)
                .build();
    }



}
