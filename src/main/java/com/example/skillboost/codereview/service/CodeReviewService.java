package com.example.skillboost.codereview.service;

import com.example.skillboost.codereview.dto.CodeReviewRequest;
import com.example.skillboost.codereview.dto.CodeReviewResponse;

public interface CodeReviewService {

    CodeReviewResponse review(CodeReviewRequest request);
}
