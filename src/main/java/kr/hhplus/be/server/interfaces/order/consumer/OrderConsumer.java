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

    @KafkaListener(topics = "order.completed", groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderCompletedEvent event) {
        log.info("🟢 Kafka 메시지 수신 - OrderID: {}, PaymentID: {}", event.getOrderId(), event.getPaymentId());
    }

}
