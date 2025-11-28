package com.example.skillboost.codingtest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultDto {

    private Long submissionId;

    // "AC"(정답), "WA"(오답) 등
    private String status;

    // 0 ~ 100점
    private Integer score;

    // 통과한 테스트케이스 수 (없으면 null 가능)
    private Integer passedCount;

    // 전체 테스트케이스 수 (없으면 null 가능)
    private Integer totalCount;

    // "정답입니다! 🎉" 같은 간단 메시지
    private String message;

    // 🔹 AI 코드 리뷰 텍스트
    private String aiFeedback;

    // 🔥 예상 면접 질문 리스트 (프론트에서 1. 2. 3. 으로 뿌려줌)
    private List<String> interviewQuestions;
}
