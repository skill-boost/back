package com.example.skillboost.codereview.client;

import com.example.skillboost.codereview.dto.CodeReviewResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GeminiCodeReviewClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiCodeReviewClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public CodeReviewResponse requestReview(String code, String comment) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            String prompt = buildPrompt(code, comment);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String body = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                CodeReviewResponse fallback = new CodeReviewResponse();
                fallback.setReview("AI 코드 리뷰 요청에 실패했습니다. 상태코드: " + response.getStatusCode());
                fallback.setQuestions(Collections.emptyList());
                return fallback;
            }

            return parseGeminiResponse(body);

        } catch (Exception e) {
            CodeReviewResponse fallback = new CodeReviewResponse();
            fallback.setReview("AI 코드 리뷰 중 오류가 발생했습니다: " + e.getMessage());
            fallback.setQuestions(Collections.emptyList());
            return fallback;
        }
    }

    /**
     * 리뷰 + 질문을 "간결하고 핵심적"이고 "□ 포맷", "1. 2. 질문 구조"로 내보내도록 만드는 프롬프트
     */
    private String buildPrompt(String code, String comment) {
        String userRequirement = (comment != null && !comment.trim().isEmpty())
                ? comment.trim()
                : "특별한 추가 요구사항은 없습니다. 핵심만 간결하게 리뷰해줘.";

        return """
            너는 숙련된 시니어 백엔드 개발자이자 코드 리뷰어야.
            아래 코드를 분석해서 반드시 **JSON 형식 하나만** 출력해.

            ⚠️ 모든 출력은 반드시 한국어로 작성해.
            마크다운 금지(**, ```, # 등)
            JSON 외 텍스트 출력 금지.

            🔒 출력 형식 규칙
            - review 항목은:
              - 모든 줄을 '□ ' 로 시작
              - 한 줄은 핵심 한 문장
              - 항목 사이에는 빈 줄(\\n\\n) 있어야 함

            - questions 항목은:
              - 배열 형태
              - 각 질문은 한국어 한 문장
              - 번호(1. 2.)는 넣지 말 것

            JSON 예시:

            {
              "review": "□ 핵심 피드백입니다.\\n\\n□ 또 다른 핵심 피드백입니다.",
              "questions": [
                "이 코드에서 개선할 수 있는 부분은 무엇인가요?",
                "예외 처리를 추가한다면 어떤 케이스를 고려하겠습니까?"
              ]
            }

            사용자가 요청한 요구사항:
            %s

            리뷰할 코드:
            %s
            """.formatted(userRequirement, code);
    }

    /**
     * Gemini 응답(JSON 스트링)을 CodeReviewResponse로 변환
     */
    private CodeReviewResponse parseGeminiResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            CodeReviewResponse resp = new CodeReviewResponse();
            resp.setReview("AI 응답이 비어 있습니다.");
            resp.setQuestions(Collections.emptyList());
            return resp;
        }

        JsonNode textNode = candidates.get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text");

        String rawText = textNode.asText("");
        if (rawText.isEmpty()) {
            CodeReviewResponse resp = new CodeReviewResponse();
            resp.setReview("AI 응답 텍스트를 찾지 못했습니다.");
            resp.setQuestions(Collections.emptyList());
            return resp;
        }

        // ```json ... ``` 형태 제거
        String cleaned = stripCodeFence(rawText);

        // JSON 파싱
        try {
            JsonNode json = objectMapper.readTree(cleaned);

            String review = json.path("review").asText("");
            if (review.isEmpty()) review = cleaned;

            List<String> questions = new ArrayList<>();
            JsonNode qNode = json.path("questions");
            if (qNode.isArray()) {
                for (JsonNode q : qNode) questions.add(q.asText());
            }

            CodeReviewResponse resp = new CodeReviewResponse();
            resp.setReview(review);
            resp.setQuestions(questions);
            return resp;

        } catch (Exception e) {
            // JSON 파싱 실패 시 그대로 리뷰로 전달
            CodeReviewResponse resp = new CodeReviewResponse();
            resp.setReview(cleaned);
            resp.setQuestions(Collections.emptyList());
            return resp;
        }
    }

    /**
     *  ```json
     *  {...}
     *  ```
     *  같은 코드블럭 제거
     */
    private String stripCodeFence(String text) {
        if (text == null) return "";
        String trimmed = text.trim();

        if (!trimmed.startsWith("```")) return trimmed;

        int firstNewline = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");

        if (firstNewline != -1 && lastFence != -1 && lastFence > firstNewline) {
            return trimmed.substring(firstNewline + 1, lastFence).trim();
        }

        return trimmed;
    }
}
