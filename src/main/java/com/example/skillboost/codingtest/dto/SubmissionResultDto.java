package com.example.skillboost.codingtest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultDto {
    private Long submissionId;
    private String status;       // "AC"(정답), "WA"(오답)
    private Integer score;       // 0 ~ 100점
    private Integer passedCount; // (AI 추정치)
    private Integer totalCount;
    private String message;      // "정답입니다!" 같은 간단 메시지

    // ★ [추가] AI 선생님의 상세 피드백
    private String aiFeedback;
}