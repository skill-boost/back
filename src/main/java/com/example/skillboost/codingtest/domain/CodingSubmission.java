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
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private CodingProblem problem;

    // ★ [추가] 누가 풀었는지 저장해야 합니다!
    private Long userId;

    private String language;

    @Column(columnDefinition = "TEXT")
    private String sourceCode;

    private String verdict;
    private int passedCount;
    private int totalCount;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}