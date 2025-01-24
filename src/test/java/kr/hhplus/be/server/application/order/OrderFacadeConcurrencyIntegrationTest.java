package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.util.fixture.PointFixture;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderFacadeConcurrencyIntegrationTest {


    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PointRepository pointRepository;


    @Test
    @DisplayName("동시 주문 요청: 재고가 1인 상품에 대해 5명의 사용자가 주문 요청을 보낼 경우 1명만 성공")
    void order_whenConcurrency_thenOnlyOneSuccess() throws InterruptedException {
        // given
        User user1 = userRepository.save(UserFixture.user("user1"));
        User user2 = userRepository.save(UserFixture.user("user2"));
        User user3 = userRepository.save(UserFixture.user("user3"));
        User user4 = userRepository.save(UserFixture.user("user4"));
        User user5 = userRepository.save(UserFixture.user("user5"));

        // 각 사용자에게 충분한 포인트 추가
        pointRepository.save(PointFixture.point(user1.getId(), 10_000));
        pointRepository.save(PointFixture.point(user2.getId(), 10_000));
        pointRepository.save(PointFixture.point(user3.getId(), 10_000));
        pointRepository.save(PointFixture.point(user4.getId(), 10_000));
        pointRepository.save(PointFixture.point(user5.getId(), 10_000));

        Product product = productRepository.save(ProductFixture.product("동시성 테스트 상품", BigDecimal.valueOf(10_000), "테스트"));
        Stock stock = stockRepository.save(ProductFixture.stock(product, 1)); // 재고 1개

        List<OrderCommand.Create> commands = List.of(
                new OrderCommand.Create(user1.getId(), List.of(new OrderCommand.OrderItem(product.getId(), 1)), null),
                new OrderCommand.Create(user2.getId(), List.of(new OrderCommand.OrderItem(product.getId(), 1)), null),
                new OrderCommand.Create(user3.getId(), List.of(new OrderCommand.OrderItem(product.getId(), 1)), null),
                new OrderCommand.Create(user4.getId(), List.of(new OrderCommand.OrderItem(product.getId(), 1)), null),
                new OrderCommand.Create(user5.getId(), List.of(new OrderCommand.OrderItem(product.getId(), 1)), null)
        );

        // 동시성을 위해 ExecutorService 생성
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<OrderInfo>> futures = new ArrayList<>();

        // when
        commands.forEach(command -> futures.add(executor.submit(() -> {
            try {
                return orderFacade.order(command);
            } catch (Exception e) {
                return null; // 실패한 경우 null 반환
            }
        })));

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then
        List<OrderInfo> successfulOrders = new ArrayList<>();
        futures.forEach(future -> {
            try {
                OrderInfo orderInfo = future.get();
                if (orderInfo != null) {
                    successfulOrders.add(orderInfo);
                }
            } catch (Exception e) {
                // Future 실패 무시
            }
        });

        // 성공한 주문은 1개여야 한다
        assertThat(successfulOrders).hasSize(1);

        // 성공한 주문 확인
        Order successfulOrder = orderRepository.findById(successfulOrders.get(0).orderId()).orElseThrow();
        assertThat(successfulOrder.getOrderItems()).hasSize(1);
        assertThat(successfulOrder.getOrderItems().get(0).getQuantity()).isEqualTo(1);

        // 재고는 0이어야 한다
        Stock updatedStock = stockRepository.findById(stock.getId()).orElseThrow();
        assertThat(updatedStock.getQuantity()).isEqualTo(0);
    }

}
