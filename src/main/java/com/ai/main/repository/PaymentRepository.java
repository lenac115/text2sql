package com.ai.main.repository;

import com.ai.main.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.orders WHERE p.orders.id = :orderId")
    Optional<Payment> findByOrderIdWithOrder(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.orders o JOIN FETCH o.users " +
           "WHERE p.status = com.ai.main.domain.Payment.PaymentStatus.PENDING " +
           "AND p.requestedAt < :threshold")
    List<Payment> findStaleRequests(@Param("threshold") LocalDateTime threshold);
}