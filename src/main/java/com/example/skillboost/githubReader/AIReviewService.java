package com.example.skillboost.githubReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIReviewService {

    private final WebClient webClient;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public AIReviewService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    /**
     * GitHub repo 컨텍스트를 기반으로 코드 리뷰 생성
     */
    public String reviewWithContext(String targetCode, String comment, List<GithubFile> repoContext) {
        String prompt = buildPrompt(targetCode, comment, repoContext);

        System.out.println("📝 생성된 프롬프트 길이: " + prompt.length() + "자");

        // Gemini API 호출 (API 키가 없으면 Mock 리뷰 생성)
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.out.println("⚠️ Gemini API 키가 없습니다. Mock 리뷰를 생성합니다.");
            return generateMockReview(repoContext != null ? repoContext.size() : 0);
        }

        return callGemini(prompt);
    }

    /**
     * AI 프롬프트 생성
     */
    private String buildPrompt(String targetCode, String comment, List<GithubFile> repoContext) {
        StringBuilder prompt = new StringBuilder();

        // 1. 역할 설정
        prompt.append("당신은 경험 많은 시니어 개발자입니다. ");
        prompt.append("프로젝트의 전체 구조를 이해하고, 코드 품질을 향상시키는 리뷰를 제공합니다.\n\n");

        // 2. 프로젝트 컨텍스트
        if (repoContext != null && !repoContext.isEmpty()) {
            prompt.append("=== 📦 프로젝트 전체 구조 ===\n\n");
            prompt.append("총 ").append(repoContext.size()).append("개의 파일로 구성된 프로젝트입니다.\n\n");

            // 파일 목록 (최대 50개)
            prompt.append("📁 파일 목록:\n");
            int fileListCount = 0;
            for (GithubFile file : repoContext) {
                if (fileListCount++ >= 50) break;
                prompt.append("  - ").append(file.getPath()).append("\n");
            }
            if (repoContext.size() > 50) {
                prompt.append("  ... 외 ").append(repoContext.size() - 50).append("개 파일\n");
            }
            prompt.append("\n");

            // 주요 파일 내용 (최대 5개, 각 1500자 제한)
            prompt.append("=== 📄 주요 파일 내용 (샘플) ===\n\n");
            int contentCount = 0;
            for (GithubFile file : repoContext) {
                if (contentCount++ >= 5) break;

                prompt.append("#### ").append(file.getPath()).append("\n");
                prompt.append("```\n");

                String content = file.getContent();
                if (content.length() > 1500) {
                    content = content.substring(0, 1500) + "\n... (생략)";
                }
                prompt.append(content);
                prompt.append("\n```\n\n");
            }

            // 프로젝트 특징 분석
            prompt.append("=== 🔍 프로젝트 분석 ===\n");
            prompt.append(analyzeProjectStructure(repoContext));
            prompt.append("\n\n");
        }

        // 3. 리뷰 대상 코드
        prompt.append("=== 🎯 리뷰 대상 코드 ===\n\n");
        prompt.append("```\n").append(targetCode).append("\n```\n\n");

        // 4. 사용자 코멘트
        if (comment != null && !comment.isEmpty()) {
            prompt.append("=== 💬 개발자의 질문/고민 ===\n");
            prompt.append(comment).append("\n\n");
        }

        // 5. 리뷰 가이드라인
        prompt.append("=== ✅ 리뷰 요청사항 ===\n\n");
        prompt.append("위 프로젝트의 전체 구조와 코드 스타일을 고려하여, 다음 관점에서 상세한 피드백을 제공해주세요:\n\n");
        prompt.append("1. **아키텍처 일관성**: 프로젝트의 기존 패턴과 일치하는가?\n");
        prompt.append("2. **네이밍 컨벤션**: 프로젝트의 네이밍 규칙을 따르는가?\n");
        prompt.append("3. **코드 품질**: 가독성, 유지보수성, 효율성은 어떤가?\n");
        prompt.append("4. **잠재적 문제**: 버그, 보안 이슈, 성능 문제가 있는가?\n");
        prompt.append("5. **개선 제안**: 구체적인 코드 예시와 함께 개선 방안 제시\n\n");
        prompt.append("리뷰는 친절하고 건설적인 톤으로, 구체적인 예시를 포함해 작성해주세요.");

        return prompt.toString();
    }

    /**
     * 프로젝트 구조 분석
     */
    private String analyzeProjectStructure(List<GithubFile> files) {
        StringBuilder analysis = new StringBuilder();

        // 언어/파일 타입 분석
        Map<String, Long> extensions = files.stream()
                .collect(Collectors.groupingBy(
                        file -> {
                            String path = file.getPath();
                            int dotIndex = path.lastIndexOf('.');
                            return dotIndex > 0 ? path.substring(dotIndex) : "기타";
                        },
                        Collectors.counting()
                ));

        analysis.append("- 주요 언어/파일 타입: ");
        analysis.append(extensions.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> e.getKey() + " (" + e.getValue() + "개)")
                .collect(Collectors.joining(", ")));
        analysis.append("\n");

        // 아키텍처 패턴 분석
        boolean hasController = files.stream().anyMatch(f -> f.getPath().toLowerCase().contains("controller"));
        boolean hasService = files.stream().anyMatch(f -> f.getPath().toLowerCase().contains("service"));
        boolean hasRepository = files.stream().anyMatch(f -> f.getPath().toLowerCase().contains("repository"));
        boolean hasComponent = files.stream().anyMatch(f -> f.getPath().toLowerCase().contains("component"));

        if (hasController && hasService && hasRepository) {
            analysis.append("- 아키텍처: Layered Architecture (Controller-Service-Repository 패턴)\n");
        } else if (hasComponent) {
            analysis.append("- 아키텍처: Component 기반 구조\n");
        }

        return analysis.toString();
    }

    /**
     * Google Gemini API 호출
     */
    private String callGemini(String prompt) {
        try {
            // Gemini API 요청 형식
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            // Gemini API 호출
            String apiUrl = String.format("/v1beta/models/gemini-pro:generateContent?key=%s", geminiApiKey);

            Map<String, Object> response = webClient.post()
                    .uri(apiUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 응답 파싱
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }

            throw new RuntimeException("Gemini API 응답 형식이 올바르지 않습니다.");

        } catch (Exception e) {
            System.err.println("❌ Gemini API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            return generateMockReview(0);
        }
    }

    /**
     * Mock 리뷰 생성 (API 키가 없을 때)
     */
    private String generateMockReview(int fileCount) {
        StringBuilder mock = new StringBuilder();

        mock.append("# 🔍 AI 코드 리뷰 결과\n\n");

        if (fileCount > 0) {
            mock.append("✅ **").append(fileCount).append("개의 프로젝트 파일**을 분석하여 전체 구조를 파악했습니다.\n\n");
        }

        mock.append("## ✅ 긍정적인 부분\n\n");
        mock.append("- 코드가 깔끔하고 읽기 쉽습니다\n");
        mock.append("- 기본적인 구조는 잘 갖춰져 있습니다\n");

        if (fileCount > 0) {
            mock.append("- 프로젝트의 전체적인 아키텍처 패턴을 잘 따르고 있습니다\n");
        }

        mock.append("\n## ⚠️ 개선이 필요한 부분\n\n");
        mock.append("1. **에러 처리**: 예외 상황에 대한 처리가 부족합니다\n");
        mock.append("2. **변수명**: 더 명확한 이름을 사용하면 좋겠습니다\n");
        mock.append("3. **주석**: 복잡한 로직에 설명 주석을 추가해주세요\n");

        mock.append("\n## 💡 구체적인 개선 제안\n\n");
        mock.append("```java\n");
        mock.append("// 개선 전\n");
        mock.append("int x = getData();\n\n");
        mock.append("// 개선 후\n");
        mock.append("int userCount = getUserCount();\n");
        mock.append("```\n\n");

        if (fileCount > 0) {
            mock.append("## 🏗️ 프로젝트 구조 관점\n\n");
            mock.append("전체 프로젝트를 분석한 결과, 이 코드는 기존 패턴과 잘 맞습니다. ");
            mock.append("다만 네이밍 컨벤션을 더 일관되게 유지하면 좋을 것 같습니다.\n\n");
        }

        mock.append("## 📝 총평\n\n");
        mock.append("전체적으로 좋은 코드입니다. 위 제안사항들을 반영하면 더욱 완성도 높은 코드가 될 것입니다!\n\n");
        mock.append("---\n");
        mock.append("*※ 이 리뷰는 Mock 데이터입니다. 실제 AI 리뷰를 받으려면 Gemini API 키를 `application.yml`에 설정해주세요.*\n");

        return mock.toString();
    }
}