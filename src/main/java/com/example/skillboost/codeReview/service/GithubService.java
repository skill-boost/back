package com.example.skillboost.codeReview.service;

import com.example.skillboost.codeReview.GithubFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GithubService {

    private final WebClient webClient;

    @Value("${github.token:}")
    private String githubToken;

    public GithubService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.github.com").build();
    }

    public List<GithubFile> fetchRepoCode(String repoUrl, String branch) {
        String[] parts = repoUrl.replace("https://github.com/", "").split("/");
        if (parts.length < 2) throw new IllegalArgumentException("잘못된 GitHub URL 형식입니다.");

        String owner = parts[0];
        String repo = parts[1];
        String treeUrl = String.format("/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, branch);

        // 전체 트리 조회
        Map<String, Object> response = webClient.get()
                .uri(treeUrl)
                .headers(h -> {
                    if (!githubToken.isEmpty())
                        h.setBearerAuth(githubToken);
                })
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> tree = (List<Map<String, Object>>) response.get("tree");
        List<GithubFile> files = new ArrayList<>();

        // 텍스트 파일만 필터링
        for (Map<String, Object> file : tree) {
            if ("blob".equals(file.get("type"))) {
                String path = (String) file.get("path");

                if (!isTextFile(path)) continue;

                String rawUrl = String.format(
                        "https://raw.githubusercontent.com/%s/%s/%s/%s",
                        owner, repo, branch, path
                );

                String content = fetchFileContent(rawUrl);
                files.add(new GithubFile(path, content));
            }
        }

        return files;
    }

    // 개별 파일 내용 불러오기
    private String fetchFileContent(String rawUrl) {
        try {
            return webClient.get()
                    .uri(rawUrl)
                    .headers(h -> {
                        if (!githubToken.isEmpty())
                            h.setBearerAuth(githubToken);
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.just("")) // 오류 발생 시 빈 문자열 반환
                    .block();
        } catch (Exception e) {
            return "";
        }
    }

    // 텍스트 파일 확장자 필터
    private static final List<String> TEXT_EXTENSIONS = List.of(
            ".java", ".kt", ".xml", ".json", ".yml", ".yaml",
            ".md", ".gradle", ".gitignore", ".txt", ".properties", ".csv"
    );

    private boolean isTextFile(String path) {
        for (String ext : TEXT_EXTENSIONS) {
            if (path.toLowerCase().endsWith(ext)) return true;
        }
        return false;
    }
}
