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

    @PostMapping("/submissions")
    public ResponseEntity<SubmissionResultDto> submitCode(@RequestBody SubmissionRequestDto request) {

        log.info("채점 요청 도착: problemId={}, language={}",
                request.getProblemId(), request.getLanguage());

        if (request.getCode() == null || request.getCode().isEmpty()) {
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
