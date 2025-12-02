// src/main/java/com/example/skillboost/interview/dto/QuestionFeedbackDto.java
package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFeedbackDto {

    private Long questionId;
    private String questionText;  // ✅ 질문 내용 추가
    private int score;
    private String feedback;
}
