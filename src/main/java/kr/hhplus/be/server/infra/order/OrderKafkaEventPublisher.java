package kr.hhplus.be.server.infra.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.outbox.OutboxStatus;
import kr.hhplus.be.server.domain.order.outbox.OrderDataOutbox;
import kr.hhplus.be.server.domain.order.outbox.OrderDataOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderDataOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3; // 최대 재시도 횟수


    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OrderDataOutbox> events = repository.findAllByStatusInAndRetryCountLessThan(
                List.of(OutboxStatus.INIT, OutboxStatus.FAILED),
                MAX_RETRIES
        );

        for (OrderDataOutbox event : events) {
            try {
                OrderCompletedEvent orderEvent = objectMapper.readValue(event.getPayload(), OrderCompletedEvent.class);
                kafkaTemplate.send("order.completed", orderEvent).get();
                event.markProcessed();
            } catch (Exception e) {
                event.markFailed();
                System.err.println("Failed to send event to Kafka: " + e.getMessage());
            }
        }

        repository.saveAll(events);
    }

}
