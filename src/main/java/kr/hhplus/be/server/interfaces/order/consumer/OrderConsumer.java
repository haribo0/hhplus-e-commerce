package kr.hhplus.be.server.interfaces.order.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.completed", groupId = "order-group")
    public void consume(String message) {
        OrderCompletedEvent event = null;
        try {
            event = objectMapper.readValue(message, OrderCompletedEvent.class);
            log.info("Received order completed event: " + event.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 변환 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
