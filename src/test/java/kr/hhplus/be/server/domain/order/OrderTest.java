package kr.hhplus.be.server.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("Order를 생성하면 초기 상태가 ORDERED이다.")
    void create_whenNewOrder_thenStatusIsOrdered() {
        // given
        Long userId = 1L;

        // when
        Order order = Order.create(userId);

        // then
        assertThat(order).isNotNull();
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getFinalAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getOrderItems()).isEmpty();
    }

    @Test
    @DisplayName("Order에 OrderItem을 추가하면 총 금액(totalAmount)이 업데이트된다.")
    void addOrderItem_whenItemAdded_thenTotalAmountUpdated() {
        // given
        Order order = Order.create(1L);

        OrderItem item1 = new OrderItem(null, order, null, 2, BigDecimal.valueOf(1000));
        OrderItem item2 = new OrderItem(null, order, null, 3, BigDecimal.valueOf(2000));

        BigDecimal item1Price = item1.getPrice().multiply(BigDecimal.valueOf(item1.getQuantity()));
        BigDecimal item2Price = item2.getPrice().multiply(BigDecimal.valueOf(item2.getQuantity()));

        // when
        order.addOrderItem(item1);
        order.addOrderItem(item2);

        // then
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualTo(item1Price.add(item2Price));
    }

    @Test
    @DisplayName("할인을 적용하면 할인 금액과 최종 금액이 올바르게 계산된다.")
    void applyDiscount_whenValidDiscount_thenDiscountAndFinalAmountUpdated() {
        // given
        Order order = Order.create(1L);
        order.addOrderItem(new OrderItem(null, order, null, 1, BigDecimal.valueOf(5000)));

        // when
        order.applyDiscount(BigDecimal.valueOf(1000));

        // then
        assertThat(order.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(order.getFinalAmount()).isEqualTo(BigDecimal.valueOf(4000));
    }

    @Test
    @DisplayName("할인 금액이 0 이하이면 예외가 발생한다.")
    void applyDiscount_whenInvalidDiscount_thenThrowsException() {
        // given
        Order order = Order.create(1L);

        // when & then
        assertThatThrownBy(() -> order.applyDiscount(BigDecimal.valueOf(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("할인 금액이 유효하지 않습니다.");

        assertThatThrownBy(() -> order.applyDiscount(BigDecimal.valueOf(-1000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("할인 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("주문 상태를 업데이트할 수 있다.")
    void updateStatus_whenValidTransition_thenSuccess() {
        // given
        Order order = Order.create(1L);

        // when
        order.updateStatus(OrderStatus.PAID);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("잘못된 주문 상태로 변경 시 예외가 발생한다.")
    void updateStatus_whenInvalidTransition_thenThrowsException() {
        // given
        Order order = Order.create(1L);

        // when & then
        assertThatThrownBy(() -> order.updateStatus(OrderStatus.FAILED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition from ORDERED to FAILED");

        order.updateStatus(OrderStatus.PAID);

        assertThatThrownBy(() -> order.updateStatus(OrderStatus.ORDERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition from PAID to ORDERED");
    }

    @Test
    @DisplayName("이미 동일한 상태로 변경하려고 하면 예외가 발생한다.")
    void updateStatus_whenSameStatus_thenThrowsException() {
        // given
        Order order = Order.create(1L);

        // when & then
        assertThatThrownBy(() -> order.updateStatus(OrderStatus.ORDERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("The order is already in the ORDERED status.");
    }
}
