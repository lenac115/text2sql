package com.ai.main.controller;

import com.ai.main.dto.coupon.CouponCreateRequest;
import com.ai.main.dto.coupon.CouponIssueRequest;
import com.ai.main.dto.coupon.UserCouponResponse;
import com.ai.main.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/coupons")
@Tag(name = "08. 쿠폰", description = "쿠폰 정의 (ADMIN) / 사용자 발급 / 보유 목록")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 쿠폰 정의 생성 (FIXED / PERCENTAGE)")
    public ResponseEntity<Void> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        couponService.createCoupon(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/issue")
    @Operation(summary = "쿠폰 발급 (코드로 본인 계정에 등록, 1인 1회)")
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @Valid @RequestBody CouponIssueRequest request,
            Authentication auth) {
        return ResponseEntity.ok(couponService.issueCoupon(auth.getName(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "내가 보유한 쿠폰 목록")
    public ResponseEntity<List<UserCouponResponse>> getMyCoupons(Authentication auth) {
        return ResponseEntity.ok(couponService.getMyCoupons(auth.getName()));
    }
}