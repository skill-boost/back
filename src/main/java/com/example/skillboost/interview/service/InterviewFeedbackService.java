package com.example.skillboost.interview.service;

import com.example.skillboost.interview.dto.InterviewAnswerDto;
import com.example.skillboost.interview.dto.InterviewFeedbackRequest;
import com.example.skillboost.interview.dto.InterviewFeedbackResponse;
import com.example.skillboost.interview.dto.QuestionFeedbackDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewFeedbackService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public InterviewFeedbackResponse createFeedback(InterviewFeedbackRequest request) {

        // 1. 질문/답변 리스트를 JSON 형태로 준비
        List<Map<String, Object>> qaList = new ArrayList<>();
        // questionId -> questionText 매핑용
        Map<Long, String> idToQuestion = new HashMap<>();

        for (InterviewAnswerDto answer : request.getAnswers()) {
            qaList.add(Map.of(
                    "questionId", answer.getQuestionId(),
                    "question", answer.getQuestion(),
                    "answer", answer.getAnswerText()
            ));
            if (answer.getQuestionId() != null) {
                idToQuestion.put(answer.getQuestionId(), answer.getQuestion());
            }
        }

        String qaJson;
        try {
            qaJson = objectMapper.writeValueAsString(qaList);
        } catch (Exception e) {
            throw new RuntimeException("질문/답변 JSON 변환 실패", e);
        }

        // 2. Gemini에 평가 요청
        String prompt = """
            당신은 시니어 개발자/리더 면접관입니다.
            아래는 지원자가 기술/인성 면접에서 답변한 질문/답변 목록입니다.
            각 질문에 대해 0~20점 사이의 점수를 매기고,
            구체적인 피드백을 작성해 주세요.
            또한 전체적인 인상에 대한 한 문단 요약과 0~100점 사이의 총점을 만들어 주세요.

            질문/답변 목록(JSON):
            %s

            출력 형식은 반드시 아래 JSON 형식만 사용하세요.

            {
              "overallScore": 87,
              "summary": "전체적인 인상 요약 문단",
              "details": [
                {
                  "questionId": 1,
                  "score": 18,
                  "feedback": "이 답변이 왜 좋은지/부족한지에 대한 구체적 피드백"
                },
                {
                  "questionId": 2,
                  "score": 14,
                  "feedback": "..."
                }
              ]
            }

            - 다른 아무 텍스트도 추가하지 말고, JSON만 출력하세요.
            - score는 반드시 0~20 범위의 정수로 주세요.
            - 질문을 이해하지 못했거나 답변이 거의 없는 경우, 낮은 점수를 주고 그 이유를 feedback에 명확히 적어 주세요.
            - 특히, ```json, ``` 같은 코드 블록 마크다운은 절대로 붙이지 마세요.
            """.formatted(qaJson);

        String json = geminiClient.generateText(prompt);
        if (json == null || json.isBlank()) {
            return new InterviewFeedbackResponse(
                    0,
                    "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                    List.of()
            );
        }

        try {
            // 🔥 코드블록(```json ... ```) 등 앞뒤 잡소리 제거
            json = cleanupJson(json);
            log.info("Gemini output after cleanup: {}", json);

            Map<String, Object> root = objectMapper.readValue(json, Map.class);

            int overallScore = ((Number) root.getOrDefault("overallScore", 0)).intValue();
            String summary = (String) root.getOrDefault("summary", "요약 정보를 생성하지 못했습니다.");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailsRaw =
                    (List<Map<String, Object>>) root.getOrDefault("details", List.of());

            List<QuestionFeedbackDto> details = new ArrayList<>();
            for (Map<String, Object> d : detailsRaw) {
                Long qid = d.get("questionId") != null
                        ? ((Number) d.get("questionId")).longValue()
                        : null;
                int score = d.get("score") != null
                        ? ((Number) d.get("score")).intValue()
                        : 0;
                String feedback = (String) d.getOrDefault("feedback", "");

                // questionId로 원래 질문 텍스트 찾기
                String questionText = (qid != null) ? idToQuestion.getOrDefault(qid, "") : "";

                details.add(new QuestionFeedbackDto(qid, questionText, score, feedback));
            }

            return new InterviewFeedbackResponse(overallScore, summary, details);

        } catch (Exception e) {
            log.error("Interview feedback JSON 파싱 오류. raw={}", json, e);
            return new InterviewFeedbackResponse(
                    0,
                    "AI 분석 결과를 해석하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                    List.of()
            );
        }
    }

    /**
     * ```json ... ``` 처럼 감싸져 올 경우 대비용 헬퍼
     */
    private String cleanupJson(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        return trimmed;
    }
}
