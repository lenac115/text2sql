package com.ai.main.repository;

import com.ai.main.domain.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @Query("SELECT o FROM Orders o JOIN FETCH o.orderItemsList i JOIN FETCH i.product " +
           "WHERE o.users.email = :email ORDER BY o.orderedAt DESC")
    List<Orders> findByUserEmailWithItems(@Param("email") String email);

    @Query("SELECT o FROM Orders o JOIN FETCH o.orderItemsList i JOIN FETCH i.product " +
           "WHERE o.id = :id AND o.users.email = :email")
    Optional<Orders> findByIdAndUserEmail(@Param("id") Long id, @Param("email") String email);

    @Query("SELECT o FROM Orders o JOIN FETCH o.orderItemsList i JOIN FETCH i.product WHERE o.id = :id")
    Optional<Orders> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM Orders o JOIN o.orderItemsList oi " +
           "WHERE o.users.email = :email AND o.timeDeal.id = :dealId AND o.orderStatus <> 'CANCELLED'")
    int countUserDealPurchases(@Param("email") String email, @Param("dealId") Long dealId);

    @Query(value = "SELECT o FROM Orders o JOIN FETCH o.users " +
                   "WHERE (:status IS NULL OR o.orderStatus = :status)",
           countQuery = "SELECT COUNT(o) FROM Orders o WHERE (:status IS NULL OR o.orderStatus = :status)")
    Page<Orders> findAllForAdmin(@Param("status") Orders.OrderStatus status, Pageable pageable);
}