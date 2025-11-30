// src/main/java/com/example/skillboost/codereview/github/GithubService.java
package com.example.skillboost.codereview.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class GithubService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${github.token:}")
    private String githubToken;

    private static final List<String> TEXT_EXTENSIONS = List.of(
            ".java", ".kt", ".xml", ".json", ".yml", ".yaml",
            ".md", ".gradle", ".gitignore", ".txt", ".properties", ".csv"
    );

    public List<GithubFile> fetchRepoCode(String repoUrl, String branch) {
        if (repoUrl == null || !repoUrl.contains("github.com/")) {
            throw new IllegalArgumentException("잘못된 GitHub URL 형식입니다.");
        }

        try {
            String[] parts = repoUrl.replace("https://github.com/", "")
                    .replace("http://github.com/", "")
                    .split("/");
            if (parts.length < 2) throw new IllegalArgumentException("잘못된 GitHub URL 형식입니다.");

            String owner = parts[0];
            String repo = parts[1];

            String treeUrl = String.format(
                    "https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1",
                    owner, repo, branch
            );

            log.info("[GithubService] tree 호출: {}", treeUrl);

            HttpHeaders headers = new HttpHeaders();
            if (githubToken != null && !githubToken.isEmpty()) {
                headers.setBearerAuth(githubToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                    treeUrl, HttpMethod.GET, entity, Map.class
            );

            Map<String, Object> body = resp.getBody();
            if (body == null || !body.containsKey("tree")) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> tree = (List<Map<String, Object>>) body.get("tree");
            List<GithubFile> files = new ArrayList<>();

            for (Map<String, Object> file : tree) {
                if (!"blob".equals(file.get("type"))) continue;

                String path = (String) file.get("path");
                if (!isTextFile(path)) continue;

                String rawUrl = String.format(
                        "https://raw.githubusercontent.com/%s/%s/%s/%s",
                        owner, repo, branch, path
                );

                String content = fetchFileContent(rawUrl);
                files.add(new GithubFile(path, content));
            }

            log.info("[GithubService] {} 개 파일 로드 완료", files.size());
            return files;

        } catch (Exception e) {
            log.error("[GithubService] 레포지터리 로드 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String fetchFileContent(String rawUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (githubToken != null && !githubToken.isEmpty()) {
                headers.setBearerAuth(githubToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    rawUrl, HttpMethod.GET, entity, String.class
            );
            return resp.getBody() != null ? resp.getBody() : "";
        } catch (Exception e) {
            log.warn("[GithubService] 파일 읽기 실패: {} ({})", rawUrl, e.getMessage());
            return "";
        }
    }

    private boolean isTextFile(String path) {
        String lower = path.toLowerCase();
        for (String ext : TEXT_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }
}
