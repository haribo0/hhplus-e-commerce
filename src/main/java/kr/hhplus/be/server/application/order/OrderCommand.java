package kr.hhplus.be.server.application.order;


import java.util.List;

public class OrderCommand {

    public record Create(Long userId, List<OrderCommand.OrderItem> items, Long couponId) {}

    public record OrderItem(Long productId, int quantity) {}


}
