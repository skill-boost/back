// src/main/java/com/example/skillboost/codereview/service/CodeReviewServiceImpl.java
package com.example.skillboost.codereview.service;

import com.example.skillboost.codereview.dto.CodeReviewRequest;
import com.example.skillboost.codereview.dto.CodeReviewResponse;
import com.example.skillboost.codereview.github.GithubFile;
import com.example.skillboost.codereview.github.GithubService;
import com.example.skillboost.codereview.llm.GeminiCodeReviewClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final GeminiCodeReviewClient geminiCodeReviewClient;
    private final GithubService githubService;

    @Override
    public CodeReviewResponse review(CodeReviewRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("코드가 비어 있습니다.");
        }

        String code = request.getCode();
        String comment = request.getComment();
        String repoUrl = request.getRepoUrl();
        String branch = StringUtils.hasText(request.getBranch()) ? request.getBranch() : "main";

        List<GithubFile> repoContext = Collections.emptyList();

        // 🔹 repoUrl 이 있으면 GitHub 레포 전체 읽어오기
        if (StringUtils.hasText(repoUrl)) {
            repoContext = githubService.fetchRepoCode(repoUrl, branch);
        }

        // 🔹 코드 + (있다면) 레포 컨텍스트 기반으로 Gemini에 리뷰 요청
        return geminiCodeReviewClient.requestReview(code, comment, repoContext);
    }
}
