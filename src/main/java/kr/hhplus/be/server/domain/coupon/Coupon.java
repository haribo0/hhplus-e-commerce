package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType type; // FLAT, PERCENT

    private int discountValue;

    private int minOrderAmount;

    private Integer maxDiscountAmount;

    @Enumerated(EnumType.STRING)
    private CouponStatus status; // ACTIVE, INACTIVE, EXHAUSTED

    private int totalCount;

    private int issuedCount;

    private LocalDateTime startDate;

    private LocalDateTime expirationDate;
}
