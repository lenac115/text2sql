package com.ai.main.repository;

import com.ai.main.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.id = :id AND uc.user.id = :userId")
    Optional<UserCoupon> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user.email = :email")
    List<UserCoupon> findByUserEmail(@Param("email") String email);

    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}