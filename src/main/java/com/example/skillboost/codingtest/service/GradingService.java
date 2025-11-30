package com.example.skillboost.codingtest.service;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.dto.SubmissionRequestDto;
import com.example.skillboost.codingtest.dto.SubmissionResultDto;
import com.example.skillboost.codingtest.judge.GeminiJudge;
import com.example.skillboost.codingtest.repository.CodingProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradingService {

    private final CodingProblemRepository problemRepository;
    private final GeminiJudge geminiJudge;

    /**
     * 실제 AI 기반 채점
     */
    public SubmissionResultDto grade(SubmissionRequestDto request) {

        // 1) 문제 조회
        CodingProblem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        // 2) AI 채점 실행
        SubmissionResultDto aiResult = geminiJudge.grade(
                problem,
                request.getCode(),
                request.getLanguage()
        );

        // 3) 결과 그대로 반환 (AI가 최종 판정)
        return aiResult;
    }
}
