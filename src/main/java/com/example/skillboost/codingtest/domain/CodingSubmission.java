package com.example.skillboost.codingtest.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coding_submission")
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 문제 ID
    @Column(nullable = false)
    private Long problemId;

    // 유저 ID
    @Column(nullable = false)
    private Long userId;

    // 사용 언어 (python / java / cpp ...)
    @Column(length = 20)
    private String language;

    // 제출 코드
    @Lob
    @Column(nullable = false)
    private String sourceCode;

    // "AC", "WA", "PARTIAL", "ERROR" 등
    @Column(length = 20)
    private String status;

    // 0 ~ 100 점
    private Integer score;

    // 통과/전체 테스트 수
    private Integer passedCount;
    private Integer totalCount;

    // 간단 메시지
    @Column(length = 255)
    private String message;

    // 🔹 AI 코드 리뷰 (TEXT)
    @Lob
    private String aiFeedback;

    // 🔥 예상 면접 질문 (JSON 문자열로 저장)
    @Lob
    private String interviewQuestionsJson;

    // 생성 시각
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
