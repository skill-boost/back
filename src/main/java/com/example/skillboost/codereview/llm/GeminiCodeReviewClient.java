// src/main/java/com/example/skillboost/codereview/llm/GeminiCodeReviewClient.java
package com.example.skillboost.codereview.llm;

import com.example.skillboost.codereview.dto.CodeReviewResponse;
import com.example.skillboost.codereview.github.GithubFile;
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

    // 🔹 코드만 사용하는 기존 모드 (호환용)
    public CodeReviewResponse requestReview(String code, String comment) {
        return requestReview(code, comment, null);
    }

    // 🔹 레포지터리 컨텍스트까지 함께 넘기는 확장 버전
    public CodeReviewResponse requestReview(String code, String comment, List<GithubFile> repoContext) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            String prompt = buildPrompt(code, comment, repoContext);

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
     * 코드 + (선택) GitHub 레포지터리 컨텍스트(README, 파일구조, 일부 코드)를 포함한 프롬프트
     */
    private String buildPrompt(String code, String comment, List<GithubFile> repoContext) {
        String userRequirement = (comment != null && !comment.trim().isEmpty())
                ? comment.trim()
                : "특별한 추가 요구사항은 없습니다. 핵심만 간결하게 리뷰해줘.";

        StringBuilder sb = new StringBuilder();

        // 1) 레포지터리 전체 맥락
        if (repoContext != null && !repoContext.isEmpty()) {
            sb.append("이 코드는 GitHub 레포지터리 전체 맥락 안에 있는 일부 코드입니다.\n")
                    .append("레포지터리의 README와 파일 구조, 주요 코드 파일을 참고해서 '요구사항을 만족하는지'와 '아키텍처 적절성'까지 함께 리뷰해 주세요.\n\n");

            // README 찾기
            GithubFile readme = repoContext.stream()
                    .filter(f -> f.getPath().equalsIgnoreCase("README.md")
                            || f.getPath().toLowerCase().endsWith("/readme.md"))
                    .findFirst()
                    .orElse(null);

            if (readme != null && readme.getContent() != null) {
                String readmeContent = readme.getContent();
                if (readmeContent.length() > 2000) {
                    readmeContent = readmeContent.substring(0, 2000) + "\n... (생략)";
                }

                sb.append("=== README (요구사항 기준) ===\n");
                sb.append(readmeContent).append("\n\n");
            }

            // 파일 목록 (최대 40개)
            sb.append("=== 프로젝트 파일 구조 (일부) ===\n");
            repoContext.stream()
                    .limit(40)
                    .forEach(f -> sb.append("- ").append(f.getPath()).append("\n"));
            if (repoContext.size() > 40) {
                sb.append("... 외 ").append(repoContext.size() - 40).append("개 파일 더 있음\n");
            }
            sb.append("\n");

            // 주요 코드 샘플 (java 위주 최대 5개)
            sb.append("=== 주요 코드 샘플 (일부) ===\n");
            repoContext.stream()
                    .filter(f -> f.getPath().endsWith(".java"))
                    .limit(5)
                    .forEach(f -> {
                        sb.append("#### ").append(f.getPath()).append("\n");
                        String c = f.getContent();
                        if (c != null && c.length() > 1200) {
                            c = c.substring(0, 1200) + "\n... (생략)";
                        }
                        sb.append(c == null ? "" : c).append("\n\n");
                    });

            sb.append("위 정보를 참고하여, 아래 사용자가 제공한 코드가 이 레포지터리/README 요구사항과 잘 맞는지 검토해 주세요.\n\n");
        }

        // 2) 여기부터는 JSON 형식 / 출력 규칙 안내 (기존 로직 유지)
        sb.append("""
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
            """).append("\n")
                .append(userRequirement).append("\n\n")
                .append("리뷰할 코드:\n")
                .append(code);

        return sb.toString();
    }

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

        String cleaned = stripCodeFence(rawText);

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
            CodeReviewResponse resp = new CodeReviewResponse();
            resp.setReview(cleaned);
            resp.setQuestions(Collections.emptyList());
            return resp;
        }
    }

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
