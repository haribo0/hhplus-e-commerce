package kr.hhplus.be.server.domain.order.event;

public interface OrderDataPublisher {
    void publish(OrderCompletedEvent event);
}
