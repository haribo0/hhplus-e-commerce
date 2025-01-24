package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.user.User;

public record UserFixture() {

    public static User user(Long userId){
        return User.builder()
                .id(userId)
                .username("testUser")
                .email("abc@abc.com")
                .build();
    }

    public static User user(String username){
        return User.builder()
                .username(username)
                .email("abc@abc.com")
                .build();
    }

    public static User user(String username, Point point){
        return User.builder()
                .username(username)
                .email("abc@abc.com")
                .build();
    }

}
