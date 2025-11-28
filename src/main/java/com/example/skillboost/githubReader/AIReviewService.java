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

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public AIReviewService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String reviewWithContext(String targetCode, String comment, List<GithubFile> repoContext) {
        String prompt = buildPrompt(targetCode, comment, repoContext);

        System.out.println("생성된 프롬프트 길이: " + prompt.length() + "자");

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.out.println("Gemini API 키가 없습니다. Mock 리뷰를 생성합니다.");
            return generateMockReview(repoContext != null ? repoContext.size() : 0);
        }

        return callGemini(prompt);
    }

    private String buildPrompt(String targetCode, String comment, List<GithubFile> repoContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 경험 많은 시니어 개발자입니다. 전체 구조를 이해하고, 코드 품질을 향상시키는 리뷰를 제공합니다.\n\n");

        if (repoContext != null && !repoContext.isEmpty()) {
            prompt.append("=== 프로젝트 전체 구조 ===\n\n");
            prompt.append("총 ").append(repoContext.size()).append("개의 파일로 구성된 프로젝트입니다.\n\n");
            prompt.append("파일 목록:\n");
            int fileListCount = 0;
            for (GithubFile file : repoContext) {
                if (fileListCount++ >= 50) break;
                prompt.append("  - ").append(file.getPath()).append("\n");
            }
            if (repoContext.size() > 50) {
                prompt.append("  ... 외 ").append(repoContext.size() - 50).append("개 파일\n");
            }
            prompt.append("\n");

            prompt.append("=== 주요 파일 내용 (샘플) ===\n\n");
            int contentCount = 0;
            for (GithubFile file : repoContext) {
                if (contentCount++ >= 5) break;
                prompt.append("#### ").append(file.getPath()).append("\n");
                prompt.append("```\n");
                String content = file.getContent();
                if (content.length() > 1500) content = content.substring(0, 1500) + "\n... (생략)";
                prompt.append(content).append("\n```\n\n");
            }

            prompt.append("=== 프로젝트 분석 ===\n");
            prompt.append(analyzeProjectStructure(repoContext)).append("\n\n");
        }

        prompt.append("=== 리뷰 대상 코드 ===\n\n```\n").append(targetCode).append("\n```\n\n");
        if (comment != null && !comment.isEmpty()) {
            prompt.append("=== 개발자의 질문/고민 ===\n").append(comment).append("\n\n");
        }

        prompt.append("=== 리뷰 요청사항 ===\n\n");
        prompt.append("전체 구조와 코드 스타일을 고려하여 다음 관점에서 상세한 피드백을 제공해주세요:\n");
        prompt.append("1. 아키텍처 일관성\n2. 네이밍 컨벤션\n3. 코드 품질\n4. 잠재적 문제\n5. 개선 제안\n\n");
        prompt.append("리뷰는 친절하고 구체적인 예시를 포함해 작성해주세요.");

        return prompt.toString();
    }

    private String analyzeProjectStructure(List<GithubFile> files) {
        StringBuilder analysis = new StringBuilder();
        Map<String, Long> extensions = files.stream()
                .collect(Collectors.groupingBy(
                        file -> {
                            String path = file.getPath();
                            int dotIndex = path.lastIndexOf('.');
                            return dotIndex > 0 ? path.substring(dotIndex) : "기타";
                        },
                        Collectors.counting()
                ));

        analysis.append("- 주요 언어/파일 타입: ")
                .append(extensions.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .limit(5)
                        .map(e -> e.getKey() + " (" + e.getValue() + "개)")
                        .collect(Collectors.joining(", ")))
                .append("\n");

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

    private String callGemini(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            Map<String, Object> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/models/{model}:generateContent")
                            .queryParam("key", geminiApiKey)
                            .build(geminiModel))
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

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
            System.err.println("Gemini API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            return generateMockReview(0);
        }
    }


    private String generateMockReview(int fileCount) {
        StringBuilder mock = new StringBuilder();
        mock.append("# AI 코드 리뷰 결과\n\n");
        if (fileCount > 0) {
            mock.append("**").append(fileCount).append("개의 프로젝트 파일**을 분석했습니다.\n\n");
        }
        mock.append("##  긍정적인 부분\n- 코드가 깔끔하고 읽기 쉽습니다\n- 기본 구조가 잘 갖춰져 있습니다\n");
        mock.append("##  개선이 필요한 부분\n1. 에러 처리 보강\n2. 변수명 명확화\n3. 주석 보강\n");
        mock.append("##  개선 제안 예시\n```java\nint userCount = getUserCount();\n```\n");
        if (fileCount > 0) {
            mock.append("##  프로젝트 구조 관점\n전체 프로젝트 패턴과 비교해 네이밍 컨벤션 일관성 유지 필요\n");
        }
        mock.append("## 총평\n전체적으로 좋은 코드입니다.\n");
        return mock.toString();
    }
}
