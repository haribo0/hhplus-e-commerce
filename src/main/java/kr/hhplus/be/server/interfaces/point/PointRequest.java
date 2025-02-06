package kr.hhplus.be.server.interfaces.point;

import kr.hhplus.be.server.application.point.PointCommand;
import kr.hhplus.be.server.domain.point.PointHistoryType;

public record PointRequest(Long userId, int amount) {

    public PointCommand.Use toUseCommand() {
        return PointCommand.Use.builder()
                .userId(this.userId)
                .amount(this.amount)
                .build();
    }
    public PointCommand.Charge toChargeCommand() {
        return PointCommand.Charge.builder()
                .userId(this.userId)
                .amount(this.amount)
                .build();
    }
}

