package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.util.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class StockTest {

    private Product product;
    private Stock stock;

    @BeforeEach
    void setUp() {
        product = ProductFixture.product(1L,"아이폰16", BigDecimal.valueOf(1_999_999), "전자기기");
        stock = ProductFixture.stock(product, 100);
    }

    @Test
    @DisplayName("decrease 메서드를 호출하면 재고가 정상적으로 차감된다")
    void decrease_ShouldDecreaseStock_WhenValidQuantity() {
        // given
        int decreaseQuantity = 20; // 차감할 수량: 20

        // when
        stock.decrease(decreaseQuantity);

        // then
        assertThat(stock.getQuantity()).isEqualTo(80); // 재고는 100에서 20이 차감되어 80이어야 한다
    }

    @Test
    @DisplayName("decrease 메서드를 호출하면 차감할 수량이 0 이하일 때 예외가 발생한다")
    void decrease_ShouldThrowException_WhenQuantityIsZeroOrLess() {
        // given
        int decreaseQuantity = 0; // 차감할 수량이 0 이하일 때

        // when & then
        assertThatThrownBy(() -> stock.decrease(decreaseQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("차감할 수량은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("decrease 메서드를 호출하면 재고보다 많은 수량을 차감할 때 예외가 발생한다")
    void decrease_ShouldThrowException_WhenQuantityIsGreaterThanStock() {
        // given
        int decreaseQuantity = 150; // 차감할 수량이 재고보다 많을 때

        // when & then
        assertThatThrownBy(() -> stock.decrease(decreaseQuantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    @DisplayName("restore 메서드를 호출하면 재고가 정상적으로 증가한다")
    void restore_ShouldIncreaseStock_WhenValidAmount() {
        // given
        int restoreAmount = 50; // 복구할 수량: 50

        // when
        stock.restore(restoreAmount);

        // then
        assertThat(stock.getQuantity()).isEqualTo(150); // 재고는 100에서 50이 증가하여 150이어야 한다
    }
}
