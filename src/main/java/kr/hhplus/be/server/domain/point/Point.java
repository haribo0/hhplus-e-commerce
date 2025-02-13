package kr.hhplus.be.server.domain.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import kr.hhplus.be.server.domain.user.User;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "point")
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int points;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Integer version; // 낙관적락

    public Point(Long userId) {
        this.userId = userId;
        this.points = 0;  // 초기 포인트는 0
        this.updatedAt = LocalDateTime.now();
    }

    public static Point createPoint(Long userId){
        return new Point(userId);
    }

    /**
     * 포인트 확인
     */
    public int getBalance() {
        return this.points;
    }

    /**
     * 포인트 충전
     */
    public void charge(int amount) {
        if(amount<=0) throw new IllegalArgumentException("포인트 값이 올바르지 않습니다.");
        this.points += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 포인트 차감
     */
    public void use(int amount) {
        if(amount<=0) throw new IllegalArgumentException("포인트 값이 올바르지 않습니다.");
        if (this.points < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.points -= amount;
        this.updatedAt = LocalDateTime.now();
    }

}
