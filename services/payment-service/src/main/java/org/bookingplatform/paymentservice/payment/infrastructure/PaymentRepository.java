package org.bookingplatform.paymentservice.payment.infrastructure;

import org.bookingplatform.paymentservice.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByBookingId(UUID bookingId);

    Optional<Payment> findByPaymentIdempotencyKey(String paymentIdempotencyKey);
}