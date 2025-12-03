package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedbackRequest {

    // 선택적이지만 있으면 리포팅/로깅에 도움 됨
    private String sessionId;

    // AI 평가용 전체 질문/답변 리스트
    private List<InterviewAnswerDto> answers;
}
