package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAnswerDto {

    // 어떤 질문에 대한 답변인지 구분용
    private Long questionId;

    // 질문 타입 (기술 / 인성)
    private QuestionType type;

    // 실제 질문 텍스트
    private String question;

    // STT로 변환된 지원자의 답변 텍스트
    private String answerText;

    // 답변에 사용된 시간(초) - 지금은 0으로 둬도 되고, 나중에 프론트에서 계산해서 넣어도 됨
    private int durationSec;
}
