package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor              // ← 필요시를 대비한 기본 생성자
@AllArgsConstructor
public class InterviewFeedbackResponse {

    // 전체 점수 (0 ~ 100)
    private int overallScore;

    // 전체 답변에 대한 요약 한 문단
    private String summary;

    // 각 질문별 점수 + 피드백 리스트
    private List<QuestionFeedbackDto> details;
}
