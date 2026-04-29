package com.ai.main.service;

import com.ai.main.domain.Coupon;
import com.ai.main.domain.UserCoupon;
import com.ai.main.domain.Users;
import com.ai.main.dto.coupon.CouponCreateRequest;
import com.ai.main.dto.coupon.CouponIssueRequest;
import com.ai.main.dto.coupon.UserCouponResponse;
import com.ai.main.repository.CouponRepository;
import com.ai.main.repository.UserCouponRepository;
import com.ai.main.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public void createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.code())
                .name(request.name())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderAmount(request.minOrderAmount())
                .maxDiscountAmount(request.maxDiscountAmount())
                .totalQuantity(request.totalQuantity())
                .issuedQuantity(0)
                .expiresAt(request.expiresAt())
                .createdAt(LocalDateTime.now())
                .build();
        couponRepository.save(coupon);
    }

    @Transactional
    public UserCouponResponse issueCoupon(String email, CouponIssueRequest request) {
        Coupon coupon = couponRepository.findByCode(request.code())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 쿠폰 코드입니다."));

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        coupon.issue(); // 발급 가능 여부 확인 + issuedQuantity 증가

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .issuedAt(LocalDateTime.now())
                .build();

        userCouponRepository.save(userCoupon);
        return UserCouponResponse.from(userCoupon);
    }

    @Transactional(readOnly = true)
    public List<UserCouponResponse> getMyCoupons(String email) {
        return userCouponRepository.findByUserEmail(email).stream()
                .map(UserCouponResponse::from)
                .toList();
    }
}