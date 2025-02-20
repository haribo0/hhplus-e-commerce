package kr.hhplus.be.server.infra.dataplatform;

import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class OrderCompletedEventListener {

    private final DataPlaform dataPlatform;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @KafkaListener(topics = "order.completed", groupId = "dataplatform-group")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        dataPlatform.publish(event.getOrderId(), event.getPaymentId());
    }

}
