# HotDeal Market — Frontend

Spring Boot 백엔드(`main` 프로젝트)와 짝을 이루는 React 프론트엔드.

## 스택
- Vite + React 18 + TypeScript
- React Router v6
- Zustand (auth 상태 + localStorage 영속)
- Axios (JWT access/refresh 자동 갱신 인터셉터)
- TailwindCSS

## 실행 방법

1. Node 18+ 설치 확인
2. 의존성 설치:
   ```bash
   cd frontend
   npm install
   ```
3. 백엔드 먼저 기동: `./gradlew bootJar -x test && java -jar build/libs/*.jar` (포트 8081)
4. 프론트 dev 서버:
   ```bash
   npm run dev
   ```
   → http://localhost:5173 (Vite proxy로 `/v1/*` → `http://localhost:8081`)

## 주의사항

- 백엔드의 `DataInitializer`가 만든 시드 유저(`email0~49999@gmail.com`)는 평문 비밀번호로 저장돼 BCrypt와 맞지 않으므로 로그인이 안 됩니다. **회원가입 페이지에서 새 계정을 만들어 테스트**하세요.
- ADMIN 메뉴(타임딜/쿠폰/상품/카테고리 생성, text2SQL)는 별도로 ADMIN 권한이 부여된 계정이 필요합니다. DB에서 `users.role = 'ADMIN'`으로 직접 변경하거나, `DataInitializer`에 ADMIN 시드를 추가하세요.
- 결제는 `MockPgClient`가 5% 확률로 실패합니다 (의도된 동작).

## 빌드 & 백엔드 통합 (선택)

```bash
npm run build
# dist/ 결과물을 src/main/resources/static/ 으로 복사하면
# Spring Boot가 정적 호스팅합니다 (별도 CORS 불필요).
```

## 페이지

| 경로 | 설명 |
|---|---|
| `/` | 홈 (진행중 타임딜 + 최신 상품) |
| `/login`, `/register` | 인증 |
| `/products`, `/products/:id` | 상품 목록/상세 (카테고리/검색/페이지네이션) |
| `/cart` | 장바구니 |
| `/checkout` | 주문/결제 (쿠폰 선택, 배송지, 결제수단) |
| `/orders`, `/orders/:id` | 주문 내역 + SSE 실시간 상태 갱신 |
| `/deals`, `/deals/:id` | 타임딜 (실시간 카운트다운, 잔여 수량) |
| `/coupons` | 쿠폰 발급 + 보유 목록 |
| `/me` | 프로필/배송지/비밀번호 |
| `/admin/query` | (ADMIN) Text2SQL 콘솔 |