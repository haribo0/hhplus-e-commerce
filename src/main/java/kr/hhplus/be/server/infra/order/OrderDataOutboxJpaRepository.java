package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.outbox.OrderDataOutbox;
import kr.hhplus.be.server.domain.order.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDataOutboxJpaRepository extends JpaRepository<OrderDataOutbox, Long> {

    List<OrderDataOutbox> findAllByStatus(OutboxStatus status);
    List<OrderDataOutbox> findAllByStatusInAndRetryCountLessThan(List<OutboxStatus> statuses, int maxRetries);
}
