package com.ai.main.repository;

import com.ai.main.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p LEFT JOIN FETCH p.category " +
           "WHERE ci.user.email = :email ORDER BY ci.addedAt DESC")
    List<CartItem> findByUserEmail(@Param("email") String email);

    Optional<CartItem> findByUser_EmailAndProduct_Id(String email, Long productId);

    Optional<CartItem> findByIdAndUser_Email(Long id, String email);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}