package com.ai.main.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI hotDealOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HotDeal Market API")
                        .version("v1")
                        .description("""
                                Spring Boot 3.4 기반 핫딜 e커머스 + Text2SQL 백엔드 API.

                                ## 인증
                                대부분 엔드포인트는 JWT Bearer 토큰이 필요합니다.
                                `/v1/auth/login` 또는 `/v1/auth/register` 호출 → `accessToken`을
                                상단의 **Authorize** 버튼에 입력하세요.

                                ## 권한
                                - `USER`: 일반 사용자 기능 (상품 조회, 장바구니, 주문, 결제, 쿠폰 발급/사용, 타임딜 구매)
                                - `ADMIN`: 추가로 상품/카테고리/쿠폰/타임딜 생성·수정·삭제,
                                  주문 상태 강제 변경, Text2SQL 쿼리 실행 권한 보유
                                """)
                        .contact(new Contact().name("main project")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local"),
                        new Server().url("/").description("동일 호스트 / 프록시 경유")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Access Token (`/v1/auth/login` 응답의 accessToken)")
                        )
                );
    }
}