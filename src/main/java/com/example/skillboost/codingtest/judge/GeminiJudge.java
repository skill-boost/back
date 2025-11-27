package com.example.skillboost.codingtest.judge;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.dto.SubmissionResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiJudge {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final ObjectMapper objectMapper;

    public SubmissionResultDto grade(CodingProblem problem, String userCode, String language) {

        String prompt = createPrompt(problem, userCode, language);
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            return parseResponse(response.getBody(), problem.getTestCases().size());

        } catch (Exception e) {
            log.error("AI 채점 실패", e);
            return SubmissionResultDto.builder()
                    .status("ERROR")
                    .score(0)
                    .message("AI 서버와 연결할 수 없습니다.")
                    .aiFeedback("일시적인 오류입니다. 잠시 후 다시 시도해주세요.")
                    .build();
        }
    }

    private SubmissionResultDto parseResponse(String jsonResponse, int totalTestCases) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            String rawText = null;
            JsonNode cand = root.path("candidates").get(0);

            if (cand.has("output_text")) {
                rawText = cand.path("output_text").asText();
            }

            if (rawText == null || rawText.isEmpty()) {
                JsonNode parts = cand.path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    rawText = parts.get(0).path("text").asText();
                }
            }

            if (rawText == null || rawText.isEmpty()) {
                throw new RuntimeException("AI 응답 파싱 실패");
            }

            rawText = rawText.replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode resultNode = objectMapper.readTree(rawText);

            String status = resultNode.path("status").asText("WA");
            int score = resultNode.path("score").asInt(0);
            String feedback = resultNode.path("feedback").asText("피드백 없음");

            int passedCount = (score == 100)
                    ? totalTestCases
                    : (int) Math.round(totalTestCases * (score / 100.0));

            return SubmissionResultDto.builder()
                    .status(status)
                    .score(score)
                    .passedCount(passedCount)
                    .totalCount(totalTestCases)
                    .message(score == 100 ? "정답입니다! 🎉" : "오답입니다.")
                    .aiFeedback(feedback)
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패", e);
            return SubmissionResultDto.builder()
                    .status("ERROR")
                    .score(0)
                    .message("채점 오류")
                    .aiFeedback("AI 응답 분석 실패")
                    .build();
        }
    }

    private String createPrompt(CodingProblem problem, String userCode, String language) {
        return """
            You are a strict Algorithm Coding Test Judge.

            [PROBLEM TITLE]: %s
            [PROBLEM DESCRIPTION]: %s

            [USER CODE - %s]:
            %s

            Return ONLY pure JSON (no extra text):

            {
              "status": "AC" or "WA",
              "score": 0~100,
              "feedback": "한국어 피드백"
            }
            """.formatted(
                problem.getTitle(),
                problem.getDescription(),
                language,
                userCode
        );
    }
}
