package com.example.skillboost.codingtest.service;

import com.example.skillboost.codingtest.dto.SubmissionRequestDto;
import com.example.skillboost.codingtest.dto.SubmissionResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodingTestService {

    private final GradingService gradingService;

    /**
     * 코딩 테스트 제출 처리
     */
    public SubmissionResultDto submitCode(SubmissionRequestDto request) {
        return gradingService.grade(request);
    }
}
