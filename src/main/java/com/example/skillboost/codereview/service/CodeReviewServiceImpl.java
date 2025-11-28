package com.example.skillboost.codereview.service;

import com.example.skillboost.codereview.client.GeminiCodeReviewClient;
import com.example.skillboost.codereview.dto.CodeReviewRequest;
import com.example.skillboost.codereview.dto.CodeReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final GeminiCodeReviewClient geminiCodeReviewClient;

    @Override
    public CodeReviewResponse review(CodeReviewRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("코드가 비어 있습니다.");
        }

        String code = request.getCode();
        String comment = request.getComment();

        return geminiCodeReviewClient.requestReview(code, comment);
    }
}
