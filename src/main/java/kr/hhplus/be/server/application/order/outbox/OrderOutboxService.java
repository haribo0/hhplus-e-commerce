package kr.hhplus.be.server.application.order.outbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.outbox.OrderDataOutbox;
import kr.hhplus.be.server.domain.order.outbox.OrderDataOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxService {

    private final OrderDataOutboxRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(OrderCompletedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OrderDataOutbox outbox = new OrderDataOutbox(event.getClass().getSimpleName(), payload);
            repository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}