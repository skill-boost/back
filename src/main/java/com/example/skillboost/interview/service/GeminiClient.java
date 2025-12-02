package com.example.skillboost.interview.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private WebClient webClient() {
        return webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    /**
     * 단순 텍스트 프롬프트 요청 → 첫 번째 candidate의 text 반환
     */
    public String generateText(String prompt) {

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        GeminiResponse response = null;

        try {
            response = webClient()
                    .post()
                    .uri("/models/" + model + ":generateContent?key=" + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .onErrorResume(ex -> {
                        log.error("Gemini API 호출 실패: {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .block();

        } catch (Exception e) {
            log.error("Gemini 요청 중 서버 오류", e);
            return "";   // 완전 실패 시 빈 문자열
        }

        if (response == null || response.candidates == null || response.candidates.isEmpty()) {
            log.warn("Gemini 응답이 비어 있음");
            return "";
        }

        // 첫 후보 꺼내기
        GeminiCandidate first = response.candidates.get(0);

        if (first.content == null || first.content.parts == null || first.content.parts.isEmpty()) {
            log.warn("Gemini content.parts 없음");
            return "";
        }

        String text = first.content.parts.get(0).text;
        return text != null ? text.trim() : "";
    }

    // =============================
    // 내부 응답 DTO
    // =============================

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        private List<GeminiCandidate> candidates;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiCandidate {
        private GeminiContent content;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiContent {
        private List<GeminiPart> parts;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiPart {
        @JsonProperty("text")
        private String text;
    }
}
