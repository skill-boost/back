package com.example.skillboost.codingtest.judge;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.dto.SubmissionResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Gemini API를 이용해
 *  - 사용자가 작성한 코드를 채점하고
 *  - 한국어 코드 리뷰(aiFeedback)
 *  - 예상 면접 질문(interviewQuestions)
 * 을 생성하는 Judge.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiJudge {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    /**
     * AI 채점 메인 로직
     */
    public SubmissionResultDto grade(CodingProblem problem, String userCode, String language) {
        try {
            // 1) 프롬프트 만들기
            String prompt = buildPrompt(problem, language, userCode);

            // 2) Gemini 요청 바디 만들기
            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            root.set("contents", contents);

            String body = objectMapper.writeValueAsString(root);

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + model
                            + ":generateContent?key="
                            + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Gemini API 호출 실패: {}", response.getBody());
                return buildErrorResult("AI 채점 서버 응답이 올바르지 않습니다.");
            }

            // 3) Gemini 응답 파싱
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // 최신 Gemini 응답구조: candidates → content → parts → text
            JsonNode candidates = rootNode.path("candidates");
            if (!candidates.isArray() || candidates.size() == 0) {
                log.error("Gemini 응답에 candidates 없음: {}", response.getBody());
                return buildErrorResult("AI 응답이 비어 있습니다.");
            }

            JsonNode contentNode = candidates.get(0).path("content");
            JsonNode partsNode = contentNode.path("parts");
            if (!partsNode.isArray() || partsNode.size() == 0) {
                log.error("Gemini 응답에 parts 없음: {}", response.getBody());
                return buildErrorResult("AI 응답 파싱 실패.");
            }

            String rawText = partsNode.get(0).path("text").asText();
            if (rawText == null || rawText.isBlank()) {
                log.error("Gemini 응답 text 없음: {}", response.getBody());
                return buildErrorResult("AI 응답이 비어 있습니다.");
            }

            // 🔥 4) text 안에서 JSON 부분만 추출
            String jsonString = extractJsonString(rawText);
            if (jsonString == null) {
                log.error("Gemini 응답에서 JSON 부분 추출 실패. rawText={}", rawText);
                return buildErrorResult("AI 응답 JSON 파싱 실패");
            }

            // 5) JSON 파싱
            JsonNode json;
            try {
                json = objectMapper.readTree(jsonString);
            } catch (Exception e) {
                log.error("AI JSON 파싱 실패. jsonString={}", jsonString, e);
                return buildErrorResult("AI 응답 JSON 파싱 실패");
            }

            // 6) AI 결과 해석
            String status = json.path("status").asText("WA");   // 기본값 WA
            int score = json.path("score").asInt(0);
            String feedback = json.path("feedback").asText("");

            // 7) 면접 질문 파싱
            List<String> interviewQuestions = new ArrayList<>();
            JsonNode qNode = json.path("interviewQuestions");
            if (qNode.isArray()) {
                for (JsonNode q : qNode) {
                    if (q.isTextual()) interviewQuestions.add(q.asText());
                }
            }

            // 8) 테스트케이스 기반 점수 계산 (문제 데이터 기반)
            Integer totalTestCases = problem.getTestCases() != null
                    ? problem.getTestCases().size()
                    : null;
            Integer passedCount = null;
            if (totalTestCases != null && totalTestCases > 0) {
                passedCount = (int) Math.round(totalTestCases * (score / 100.0));
            }

            // 9) 최종 반환
            return SubmissionResultDto.builder()
                    .status(status)
                    .score(score)
                    .passedCount(passedCount)
                    .totalCount(totalTestCases)
                    .message(status.equals("AC") ? "정답입니다! 🎉" : "오답입니다.")
                    .aiFeedback(feedback)
                    .interviewQuestions(interviewQuestions)
                    .build();

        } catch (Exception e) {
            log.error("AI 채점 실패", e);
            return buildErrorResult("AI 채점 중 오류 발생");
        }
    }

    /**
     * AI 실패 fallback
     */
    private SubmissionResultDto buildErrorResult(String message) {
        List<String> fallbackQuestions = List.of(
                "이 문제를 해결하기 위해 선택한 자료구조와 알고리즘을 설명해주세요.",
                "시간 복잡도를 줄이기 위해 어떤 개선이 가능할까요?",
                "극단적인 입력값이 들어왔을 때 어떤 문제가 발생할 수 있을까요?"
        );

        return SubmissionResultDto.builder()
                .status("WA")   // 실패 시 절대 AC로 보이지 않게
                .score(0)
                .message(message)
                .aiFeedback("AI 분석 실패: " + message)
                .interviewQuestions(fallbackQuestions)
                .build();
    }

    /**
     * 프롬프트 생성
     */
    private String buildPrompt(CodingProblem problem, String language, String userCode) {
        return """
                너는 코딩 테스트 문제를 채점하는 한국인 시니어 개발자이다.

                아래 문제와 사용자의 코드를 보고 JSON만 출력해라.

                오직 아래 JSON 형식만, 앞뒤 설명 없이 출력해야 한다:

                {
                  "status": "AC" 또는 "WA",
                  "score": 0~100,
                  "feedback": "한국어 코드 리뷰",
                  "interviewQuestions": [
                    "질문1",
                    "질문2",
                    "질문3"
                  ]
                }

                --- 문제 정보 ---
                제목: %s

                설명:
                %s

                --- 사용 언어 ---
                %s

                --- 사용자 코드 ---
                %s
                """.formatted(
                problem.getTitle(),
                problem.getDescription(),
                language,
                userCode
        );
    }

    /**
     * 모델이 쓸데없이 앞뒤에 텍스트를 붙일 때,
     * 그 안에서 JSON 부분만 잘라내기 위한 유틸 함수.
     */
    private String extractJsonString(String rawText) {
        if (rawText == null) return null;

        String text = rawText.trim();

        // ```json ... ``` 같은 코드블럭 제거
        if (text.startsWith("```")) {
            int firstBrace = text.indexOf('{');
            int lastBrace = text.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                return text.substring(firstBrace, lastBrace + 1);
            }
        }

        // 일반 텍스트일 때도 첫 '{' ~ 마지막 '}' 사이만 추출
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) {
            return null;
        }

        return text.substring(start, end + 1).trim();
    }
}
