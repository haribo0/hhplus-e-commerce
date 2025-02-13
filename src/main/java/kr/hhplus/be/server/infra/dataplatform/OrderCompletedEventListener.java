package kr.hhplus.be.server.infra.dataplatform;

import kr.hhplus.be.server.application.order.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCompletedEventListener {

    private final DataPlaform dataPlatform;

    @KafkaListener(topics = "order.completed", groupId = "dataplatform-group")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        dataPlatform.publish(event.getOrderId(), event.getPaymentId());
    }

}
