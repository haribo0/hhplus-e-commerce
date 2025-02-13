package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentMethod;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("POINTS 결제 수단으로 결제를 생성하면 성공한다")
    void create_whenPaymentMethodIsPoints_thenSuccess() {
        // given
        Long orderId = 1L;
        BigDecimal totalAmount = BigDecimal.valueOf(1000);
        PaymentCommand.Create command = new PaymentCommand.Create(orderId, totalAmount);
        Payment payment = Payment.create(orderId, totalAmount, PaymentMethod.POINTS);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // when
        Payment result = paymentService.create(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getAmount()).isEqualByComparingTo(totalAmount);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.POINTS);

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 상태를 변경하면 성공한다")
    void changeStatus_whenValidPayment_thenSuccess() {
        // given
        Payment payment = Payment.create(1L, BigDecimal.valueOf(1000), PaymentMethod.POINTS);
        PaymentStatus newStatus = PaymentStatus.SUCCESS;
        Payment changedPayment = payment.changeStatus(newStatus);

        when(paymentRepository.save(any(Payment.class))).thenReturn(changedPayment);

        // when
        Payment result = paymentService.changeStatus(payment, newStatus);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
