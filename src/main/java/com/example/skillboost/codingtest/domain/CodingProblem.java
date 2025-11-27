package com.example.skillboost.codingtest.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")   // 긴 문제 설명 저장용
    private String description;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    // 예: "array,implementation"
    private String tags;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodingTestCase> testCases = new ArrayList<>();
}
