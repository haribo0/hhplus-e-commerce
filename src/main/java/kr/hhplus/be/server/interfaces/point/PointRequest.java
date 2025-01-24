package kr.hhplus.be.server.interfaces.point;

import kr.hhplus.be.server.application.point.PointCommand;

public record PointRequest(Long userId, int amount) {

    public PointCommand toCommand() {
        return PointCommand.builder()
                .userId(this.userId)
                .amount(this.amount)
                .build();
    }
}

