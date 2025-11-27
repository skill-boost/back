package com.example.skillboost.codingtest.service;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.dto.SubmissionRequestDto;
import com.example.skillboost.codingtest.dto.SubmissionResultDto;
import com.example.skillboost.codingtest.judge.GeminiJudge;
import com.example.skillboost.codingtest.repository.CodingProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {

    private final CodingProblemRepository problemRepository;
    private final GeminiJudge judge;

    public SubmissionResultDto grade(SubmissionRequestDto request) {

        CodingProblem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        // DB에 제출 저장 같은 건 나중에 하고,
        // 일단 AI 채점만 연결
        return judge.grade(problem, request.getCode(), request.getLanguage());
    }
}
