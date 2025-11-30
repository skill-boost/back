package com.example.skillboost.codeReview.controller;

import com.example.skillboost.codeReview.service.CodeReviewService;
import com.example.skillboost.codeReview.GithubFile;
import com.example.skillboost.codeReview.service.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "http://localhost:3000") // React 개발 서버 주소
public class CodeReviewController {

    @Autowired
    private GithubService githubService;

    @Autowired
    private CodeReviewService codeReviewService;

    @PostMapping
    public ResponseEntity<?> reviewCode(
            @RequestParam("code") String code,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "repo_url", required = false) String repoUrl,
            @RequestParam(value = "branch", defaultValue = "main") String branch
    ) {
        try {
            System.out.println("=".repeat(60));
            System.out.println(" 코드 리뷰 요청 받음");
            System.out.println("  - 코드 길이: " + code.length() + "자");
            System.out.println("  - 코멘트: " + (comment != null ? comment : "(없음)"));
            System.out.println("  - Repo URL: " + (repoUrl != null ? repoUrl : "(없음)"));

            // 1. GitHub repo 코드 가져오기 (repo_url이 있을 때만)
            List<GithubFile> repoContext = null;
            if (repoUrl != null && !repoUrl.isEmpty()) {
                System.out.println("\n GitHub Repository 분석 시작...");
                long startTime = System.currentTimeMillis();

                repoContext = githubService.fetchRepoCode(repoUrl, branch);

                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println(" " + repoContext.size() + "개 파일 로드 완료 (" + elapsed + "ms)");

                // 파일 목록 출력 (처음 10개만)
                System.out.println("\n 로드된 파일 샘플:");
                int count = 0;
                for (GithubFile file : repoContext) {
                    if (count++ >= 10) break;
                    System.out.println("  - " + file.getPath() + " (" + file.getContent().length() + "자)");
                }
                if (repoContext.size() > 10) {
                    System.out.println("  ... 외 " + (repoContext.size() - 10) + "개 파일");
                }
            }

            // 2. AI 리뷰 생성
            System.out.println("\n AI 리뷰 생성 중...");
            String reviewResult = codeReviewService.reviewWithContext(code, comment, repoContext);

            // 3. 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("review", reviewResult);
            response.put("context_files_count", repoContext != null ? repoContext.size() : 0);
            response.put("repo_url", repoUrl != null ? repoUrl : "");
            response.put("success", true);

            System.out.println(" 리뷰 완료! (리뷰 길이: " + reviewResult.length() + "자)");
            System.out.println("=".repeat(60) + "\n");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.err.println(" 잘못된 요청: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            System.err.println(" 서버 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
