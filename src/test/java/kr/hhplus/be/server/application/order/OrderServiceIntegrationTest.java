package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
public class OrderServiceIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Test
    @Transactional
    @DisplayName("재고가 충분한 경우 주문 생성에 성공한다")
    void create_whenStockIsEnough_thenSuccess() {
        // given

        User user = userRepository.save(UserFixture.user("testUser"));
        Long userId = user.getId();

        Product product = productRepository.save(ProductFixture.product("아이폰", BigDecimal.valueOf(1_999_999), "전자기기"));

        stockRepository.save(ProductFixture.stock(product,10));

        OrderCommand.Create command = new OrderCommand.Create(userId,
                List.of(new OrderCommand.OrderItem(product.getId(), 2)), // 2개 주문
                null);

        // when
        Order order = orderService.create(command);

        // then
        assertThat(order).isNotNull();
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(2);
    }
}
