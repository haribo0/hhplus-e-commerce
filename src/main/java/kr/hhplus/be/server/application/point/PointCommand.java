package kr.hhplus.be.server.application.point;

import lombok.Builder;

public class PointCommand{

    @Builder
    public record Use(Long userId, Integer amount){

    }

    @Builder
    public record Charge(Long userId, Integer amount){

    }

}
