package kr.hhplus.be.server.domain.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import kr.hhplus.be.server.domain.user.User;
import lombok.*;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PointHistoryType type;

    private int amount;

    private String description;

    public static PointHistory createPointHistory(Point point, PointHistoryType type, Integer amount, String desc) {

        PointHistory pointHistory = PointHistory.builder()
                .userId(point.getUserId())  // 동일한 사용자
                .type(type)  // 사용 내역
                .amount(amount)  // 사용된 포인트 금액
                .description(desc)  // 설명
                .build();
        return pointHistory;
    }
}
