package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.IntegrationServiceTest;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentMethod;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.util.fixture.OrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class PaymentServiceIntegrationTest extends IntegrationServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("결제를 생성하면 성공한다")
    void create_whenPaymentMethodIsPoints_thenSuccess() {
        // given
        Order order = orderRepository.save(OrderFixture.order(1L, BigDecimal.valueOf(10000)));
        Long orderId = order.getId();
        BigDecimal totalAmount = order.getTotalAmount();

        PaymentCommand.Create command = new PaymentCommand.Create(orderId, totalAmount);

        // when
        Payment payment = paymentService.create(command);

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualByComparingTo(totalAmount);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.POINTS);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // DB 검증
        Payment savedPayment = paymentRepository.findById(payment.getId()).orElse(null);
        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
    }

}
