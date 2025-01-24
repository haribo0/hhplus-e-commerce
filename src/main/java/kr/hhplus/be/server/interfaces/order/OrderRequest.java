package kr.hhplus.be.server.interfaces.order;


import kr.hhplus.be.server.application.order.OrderCommand;

import java.util.List;

public record OrderRequest(Long userId, List<Item> items, Long couponId) {

    public record Item(Long productId, int quantity) {}

    public static OrderCommand.Create toCommand(OrderRequest orderRequest) {
        List<OrderCommand.OrderItem> orderItems = orderRequest.items().stream()
                .map(item -> new OrderCommand.OrderItem(item.productId(), item.quantity())) // OrderRequest.Item -> OrderCommand.OrderItem 변환
                .toList();

        return new OrderCommand.Create(
                orderRequest.userId(),
                orderItems,
                orderRequest.couponId()
        );
    }
}
