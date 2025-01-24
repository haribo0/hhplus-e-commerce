package kr.hhplus.be.server.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);

    List<Payment> findAll();
}
