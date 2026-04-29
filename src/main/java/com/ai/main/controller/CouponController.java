package com.ai.main.controller;

import com.ai.main.dto.CouponCreateRequest;
import com.ai.main.dto.CouponIssueRequest;
import com.ai.main.dto.UserCouponResponse;
import com.ai.main.service.CouponService;
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
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        couponService.createCoupon(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/issue")
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @Valid @RequestBody CouponIssueRequest request,
            Authentication auth) {
        return ResponseEntity.ok(couponService.issueCoupon(auth.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserCouponResponse>> getMyCoupons(Authentication auth) {
        return ResponseEntity.ok(couponService.getMyCoupons(auth.getName()));
    }
}