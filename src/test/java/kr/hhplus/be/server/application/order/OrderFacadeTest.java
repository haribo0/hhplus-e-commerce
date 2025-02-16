package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.product.ProductService;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infra.point.PointJpaRepository;
import kr.hhplus.be.server.infra.product.ProductJpaRepository;
import kr.hhplus.be.server.infra.product.StockJpaRepository;
import kr.hhplus.be.server.infra.user.UserJpaRepository;
import kr.hhplus.be.server.util.fixture.PointFixture;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedKafka(partitions = 1, topics = {"order.completed"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092"})
public class OrderFacadeTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, OrderCompletedEvent> consumerFactory;

    private Consumer<String, OrderCompletedEvent> consumer;

    @Autowired
    ProductJpaRepository productJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    StockJpaRepository stockJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;


    @BeforeAll
    void setup() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음부터 읽기

        consumer = new DefaultKafkaConsumerFactory<>(consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderCompletedEvent.class)
        ).createConsumer();

        consumer.subscribe(Collections.singleton("order.completed"));
    }


    @Test
    @DisplayName("주문 완료 시 Kafka 이벤트를 발행한다.")
    void publish_kafka_event_after_completing_order() {
        // given
        User user = UserFixture.user("userA");
        userJpaRepository.save(user);
        Point point = PointFixture.point(user.getId(), 1_000_000);
        pointJpaRepository.save(point);
        Product product = ProductFixture.product("에어팟", BigDecimal.valueOf(10000), "전자제품");
        productJpaRepository.save(product);
        Stock stock = ProductFixture.stock(product, 10);
        stockJpaRepository.save(stock);

        OrderCommand.OrderItem orderItem = new OrderCommand.OrderItem(product.getId(), 1);
        OrderCommand.Create command = new OrderCommand.Create(1L, List.of(orderItem), null);

        // when
        OrderInfo orderInfo = orderFacade.order(command);

        // then
        assertThat(orderInfo).isNotNull();

        // Kafka 메시지가 정상적으로 발행되었는지 확인
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, OrderCompletedEvent> records = KafkaTestUtils.getRecords(consumer);
            assertThat(records.count()).isGreaterThan(0);

            for (ConsumerRecord<String, OrderCompletedEvent> record : records) {
                assertThat(record.value()).isNotNull();
                assertThat(record.value().getOrderId()).isEqualTo(orderInfo.orderId());
            }
        });
    }
}
