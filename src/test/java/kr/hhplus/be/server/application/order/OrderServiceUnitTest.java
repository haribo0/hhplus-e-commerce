package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("유효한 주문 요청을 처리하면 주문이 생성된다")
    void create_whenValidOrder_thenSuccess() {
        // given
        Long userId = 1L;
        Long productId1 = 101L;
        Long productId2 = 102L;

        User user = new User(userId, "test-user", "test@example.com");
        Product product1 = new Product(productId1, "Product1", BigDecimal.valueOf(1000), "Description1", null);
        Product product2 = new Product(productId2, "Product2", BigDecimal.valueOf(2000), "Description2", null);

        Stock stock1 = new Stock(1L, product1, 10);
        Stock stock2 = new Stock(2L, product2, 5);

        OrderCommand.Create command = new OrderCommand.Create(userId,
                List.of(
                        new OrderCommand.OrderItem(productId1, 2),
                        new OrderCommand.OrderItem(productId2, 3)
                ),
                null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(stockRepository.findByProductIdsWithLock(List.of(productId1, productId2))).thenReturn(List.of(stock1, stock2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Order order = orderService.create(command);

        // then
        assertThat(order).isNotNull();
        assertThat(order.getOrderItems()).hasSize(2);

        assertThat(order.getOrderItems().get(0).getProduct().getId()).isEqualTo(productId1);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(2);

        assertThat(order.getOrderItems().get(1).getProduct().getId()).isEqualTo(productId2);
        assertThat(order.getOrderItems().get(1).getQuantity()).isEqualTo(3);

        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 주문을 생성하면 예외가 발생한다")
    void create_whenUserNotFound_thenThrowsException() {
        // given
        Long userId = 1L;
        OrderCommand.Create command = new OrderCommand.Create(userId, List.of(), null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userRepository).findById(userId);
        verifyNoInteractions(stockRepository, orderRepository);
    }

    @Test
    @DisplayName("재고가 부족한 경우 예외가 발생한다")
    void create_whenStockNotEnough_thenThrowsException() {
        // given
        Long userId = 1L;
        Long productId = 101L;

        User user = new User(userId, "test-user", "test@example.com");
        Product product = new Product(productId, "Product1", BigDecimal.valueOf(1000), "Description1", null);

        Stock stock = new Stock(1L, product, 1); // 재고가 1개뿐

        OrderCommand.Create command = new OrderCommand.Create(userId,
                List.of(new OrderCommand.OrderItem(productId, 2)), // 2개 주문
                null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(stockRepository.findByProductIdsWithLock(List.of(productId))).thenReturn(List.of(stock));

        // when & then
        assertThatThrownBy(() -> orderService.create(command))
                .isInstanceOf(IllegalStateException.class);

        verify(stockRepository).findByProductIdsWithLock(List.of(productId));
        verifyNoInteractions(orderRepository);
    }
}
