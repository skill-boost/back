package com.example.skillboost.codingtest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionRequestDto {

    private Long problemId;

    // 프론트에서 보내는 JSON 키: "sourceCode"
    @JsonProperty("sourceCode")
    private String code;

    private String language;

    private Long userId;
}
