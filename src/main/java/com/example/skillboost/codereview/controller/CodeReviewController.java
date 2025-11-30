package com.example.skillboost.codereview.controller;

import com.example.skillboost.codereview.dto.CodeReviewRequest;
import com.example.skillboost.codereview.dto.CodeReviewResponse;
import com.example.skillboost.codereview.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CodeReviewResponse review(@RequestBody CodeReviewRequest request) {
        return codeReviewService.review(request);
    }
}
