package kr.hhplus.be.server.config.kafka;

import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"order.completed"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class OrderKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setupConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음부터 읽기

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList("order.completed"));
    }

    @DisplayName("producer_가_보낸_메시지를_consumer_가_받아야_한다")
    @Test
    void consumerShouldConsumeEventSentByProducer() {
        OrderCompletedEvent event = new OrderCompletedEvent(1L, 2L);
        kafkaTemplate.send("order.completed", event);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "order.completed");
            assertThat(record).isNotNull();
            OrderCompletedEvent receivedEvent = objectMapper.readValue(record.value(), OrderCompletedEvent.class);
            assertThat(receivedEvent.getOrderId()).isEqualTo(event.getOrderId());
            assertThat(receivedEvent.getPaymentId()).isEqualTo(event.getPaymentId());
        });
        System.out.println("Kafka Producer → Consumer 연동 테스트 성공");
    }
}
