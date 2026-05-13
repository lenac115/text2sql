package com.ai.main.controller;

import com.ai.main.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
@Tag(name = "11. 관리자", description = "대시보드")
public class AdminController {

    private final AdminService adminService;

}
