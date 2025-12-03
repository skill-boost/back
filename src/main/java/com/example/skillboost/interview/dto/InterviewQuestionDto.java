package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestionDto {

    // 세션 내 질문 번호 (1 ~ 5)
    private Long id;

    // TECH / BEHAV
    private QuestionType type;

    // 질문 텍스트
    private String text;
}
