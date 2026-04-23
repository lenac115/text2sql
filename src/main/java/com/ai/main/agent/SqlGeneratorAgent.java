package com.ai.main.agent;

import com.ai.main.schema.SchemaMetadataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SqlGeneratorAgent {

    private final ChatClient chatClient;
    private final SchemaMetadataProvider schemaProvider;

    public SqlGeneratorAgent(ChatClient.Builder chatClient, SchemaMetadataProvider schemaProvider) {
        this.chatClient = chatClient.build();
        this.schemaProvider = schemaProvider;
    }

    public String generateSql(String userQuestion) {
        String systemPrompt = buildSystemPrompt();

        log.info("=== System Prompt ===\n{}", systemPrompt);
        log.info("=== User Question === {}", userQuestion);


        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userQuestion)
                .call()
                .content();

        log.info("=== LLM Response === {}", response);


        return extractSql(response);
    }

    public String summarize(String question, List<Map<String, Object>> rows) {
        String resultText = rows.toString();

        String response = chatClient.prompt()
                .system("너는 데이터 분석 결과를 한국어로 요약하는 어시스턴트다. 핵심 수치를 포함해서 2~3문장으로 요약하라.")
                .user("사용자 질문 : " + question + "\n조회 결과 : " + resultText)
                .call()
                .content();

        return extractSql(response);
    }

    private String buildSystemPrompt() {
        String schema = schemaProvider.getSchemaPrompt();

        return """
        너는 MySQL 전용 SQL 생성기이다. 아래 규칙을 반드시 따라라.

        [규칙]
        1. 반드시 SELECT 문만 생성하라. INSERT, UPDATE, DELETE, DROP 등은 절대 생성하지 마라.
        2. SQL만 응답하라. 설명, 마크다운, 코드블록(```) 없이 순수 SQL 한 문장만 반환하라.
        3. 아래 제공된 스키마의 테이블명과 컬럼명만 사용하라. 없는 테이블이나 컬럼을 만들지 마라.
        4. 날짜 필터링 시 함수로 컬럼을 감싸지 마라.
           나쁜 예: WHERE YEAR(ordered_at) = 2026
           좋은 예: WHERE ordered_at >= '2026-01-01' AND ordered_at < '2026-02-01'
        5. 가능한 한 인덱스를 활용할 수 있는 형태로 작성하라.
        6. 결과 행 수가 많을 수 있는 경우 LIMIT 100을 추가하라.
        7. 금액 관련 질문에서 특별한 지정이 없으면 status = 'COMPLETED'인 주문만 집계하라.
        8. 현재 날짜는 %s이다.
        9. 드라이빙 테이블 선정 규칙 (매우 중요):
           - FROM 절에 가장 먼저 오는 테이블이 드라이빙 테이블이 된다.
           - 드라이빙 테이블은 WHERE 조건으로 가장 많이 필터링되는 테이블이어야 한다.
           - 필터링 조건이 있는 컬럼에 인덱스가 걸린 테이블을 우선 선택하라.
                
           좋은 예:
               WHERE o.order_status = 'COMPLETED' AND o.ordered_at >= '2026-01-01'
                   → orders가 필터링되므로 FROM orders o 로 시작
                
           나쁜 예:
               FROM product p JOIN order_items oi ... JOIN orders o ...
                   WHERE o.order_status = 'COMPLETED'
                   → product는 필터링이 없는데 드라이빙이 됨. 비효율.
        10. 이 스키마의 권장 JOIN 패턴:
           - "WHERE o.order_status = ... AND o.ordered_at ..." 조건이 있으면
              반드시 FROM orders o 로 시작.
           - "카테고리별", "상품 정보" 관련 질문은 product를 나중에 JOIN.
           - 일반적 권장 순서: orders → order_items → product/users

        [스키마 정보]
        %s
        """.formatted(LocalDate.now().toString(), schema);
    }

    private String extractSql(String response) {
        String sql = response.trim();

        // 혹시 마크다운 코드 블록으로 감싸져 왔을 경우 제거
        if (sql.startsWith("```")) {
            sql = sql.replaceAll("```sql\\s*", "")
                    .replaceAll("```\\s*", "");
        }

        // 세미 콜론 제거
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        return sql.trim();
    }
}
