package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.application.coupon.CouponService;
import kr.hhplus.be.server.application.payment.PaymentService;
import kr.hhplus.be.server.application.point.PointService;
import kr.hhplus.be.server.application.product.ProductService;
import kr.hhplus.be.server.application.product.StockService;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponPolicy;
import kr.hhplus.be.server.domain.coupon.CouponPolicyRepository;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.point.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.product.StockRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.util.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrderFacadeIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Test
    @DisplayName("주문 과정이 올바르게 처리된다")
    void order_whenValidCommand_thenSuccess() {
        // given
        User user = userRepository.save(UserFixture.user("testUser"));
        Product product = productRepository.save(ProductFixture.product("연필", BigDecimal.valueOf(1000), "문구"));
        Stock stock = stockRepository.save(ProductFixture.stock(product, 10));
        int quantity = 2;

        CouponPolicy couponPolicy = couponPolicyRepository.save(CouponFixture.flatCouponPolicy(BigDecimal.valueOf(1_000), BigDecimal.valueOf(1_000), 10));
        Coupon coupon = couponRepository.save(CouponFixture.coupon(user.getId(),couponPolicy));
        userRepository.save(user);
        pointRepository.save(PointFixture.point(user.getId(),10_000));

        OrderCommand.Create command = new OrderCommand.Create(
                user.getId(),
                List.of(new OrderCommand.OrderItem(product.getId(), quantity)),
                coupon.getId() // 쿠폰 ID 설정
        );

        // when
        OrderInfo orderInfo = orderFacade.order(command);

        // then
        Order order = orderRepository.findById(orderInfo.orderId()).orElseThrow();
        assertThat(order).isNotNull();
        assertThat(order.getUserId()).isEqualTo(user.getId());
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(product.getPrice().multiply(BigDecimal.valueOf(quantity))); // 2개의 상품

        assertThat(order.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(1_000));
        assertThat(order.getFinalAmount()).isEqualByComparingTo(order.getTotalAmount().subtract(order.getDiscountAmount()));

        // Payment 검증
        assertThat(paymentRepository.findAll()).hasSize(1);

        // Point 검증
        assertThat(pointHistoryRepository.findAll()).hasSize(1);

        // Stock 차감 검증
        Stock updatedStock = stockRepository.findById(stock.getId()).orElseThrow();
        assertThat(updatedStock.getQuantity()).isEqualTo(8); // 10 - 2
    }
}
