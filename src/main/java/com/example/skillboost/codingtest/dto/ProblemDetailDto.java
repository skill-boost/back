package com.example.skillboost.codingtest.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProblemDetailDto {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String tags;
    private List<SampleCase> samples;

    @Data
    @Builder
    public static class SampleCase {
        private String inputData;
        private String expectedOutput;
    }
}