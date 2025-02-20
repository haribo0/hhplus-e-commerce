package kr.hhplus.be.server.domain.order.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDataOutboxRepository {
    List<OrderDataOutbox> findAllByStatus(OutboxStatus status);
    List<OrderDataOutbox> findAllByStatusInAndRetryCountLessThan(List<OutboxStatus> statuses, int maxRetries);

    void saveAll(List<OrderDataOutbox> events);

    OrderDataOutbox save(OrderDataOutbox event);
}
