package kr.hhplus.be.server.application.payment;


import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentMethod;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointHistoryType;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;

    public Payment create(PaymentCommand.Create command) {
        Payment payment = Payment.create(command.orderId(),command.totalAmount(),PaymentMethod.POINTS);
//        PaymentMethod paymentMethod = payment.getPaymentMethod();
//        if(!paymentMethod.equals(PaymentMethod.POINTS)){
//            throw new IllegalStateException("현재 사용할 수 없는 결제수단입니다.");
//        }
        return paymentRepository.save(payment);
    }


    public Payment changeStatus(Payment payment, PaymentStatus status) {
        Payment changedPayment = payment.changeStatus(status);
        paymentRepository.save(changedPayment);
        return changedPayment;
    }


}
