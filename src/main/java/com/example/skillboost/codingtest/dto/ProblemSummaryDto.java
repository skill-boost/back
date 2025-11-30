package com.example.skillboost.codingtest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemSummaryDto {
    private Long id;
    private String title;
    private String difficulty;
    private String tags;
}