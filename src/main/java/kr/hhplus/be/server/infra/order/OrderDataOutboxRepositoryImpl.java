package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.outbox.OrderDataOutbox;
import kr.hhplus.be.server.domain.order.outbox.OrderDataOutboxRepository;
import kr.hhplus.be.server.domain.order.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderDataOutboxRepositoryImpl implements OrderDataOutboxRepository {

    private final OrderDataOutboxJpaRepository jpaRepository;

    @Override
    public List<OrderDataOutbox> findAllByStatus(OutboxStatus status) {
        return jpaRepository.findAllByStatus(status);
    }

    @Override
    public List<OrderDataOutbox> findAllByStatusInAndRetryCountLessThan(List<OutboxStatus> statuses, int maxRetries) {
        return jpaRepository.findAllByStatusInAndRetryCountLessThan(statuses, maxRetries);
    }

    @Override
    public void saveAll(List<OrderDataOutbox> events) {
        jpaRepository.saveAll(events);
    }

    @Override
    public OrderDataOutbox save(OrderDataOutbox event) {
        return jpaRepository.save(event);
    }
}
