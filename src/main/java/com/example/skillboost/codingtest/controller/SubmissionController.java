package com.example.skillboost.codingtest.controller;

import com.example.skillboost.codingtest.dto.SubmissionRequestDto;
import com.example.skillboost.codingtest.dto.SubmissionResultDto;
import com.example.skillboost.codingtest.service.GradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/coding")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final GradingService gradingService;

    /**
     * 코딩 테스트 제출 + 채점
     * POST /api/coding/submissions
     */
    @PostMapping("/submissions")
    public ResponseEntity<SubmissionResultDto> submit(@RequestBody SubmissionRequestDto request) {
        log.info("코딩테스트 제출 요청: problemId={}, language={}, userId={}",
                request.getProblemId(), request.getLanguage(), request.getUserId());

        if (request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest().body(
                    SubmissionResultDto.builder()
                            .status("ERROR")
                            .score(0)
                            .message("코드가 비어 있습니다.")
                            .build()
            );
        }

        SubmissionResultDto result = gradingService.grade(request);
        return ResponseEntity.ok(result);
    }
}
