package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.base.BaseEntity;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public Stock(Product product, Integer quantity){
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * 재고 감소
     */
    public void decrease(int quantity) {
        if(quantity<=0){
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }
        if (this.quantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.quantity -= quantity;
    }

    /**
     * 재고 복구
     */
    public void restore(int amount) {
        this.quantity += amount;
    }

}
