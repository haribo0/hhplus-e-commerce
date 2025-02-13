package kr.hhplus.be.server.application.point;

import lombok.Builder;

@Builder
public record PointCommand(Long userId, Integer amount){

}
