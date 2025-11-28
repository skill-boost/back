package com.example.skillboost.codingtest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프론트에서 /api/coding/submissions 로 보내는 요청 DTO
 */
@Data
@NoArgsConstructor
public class SubmissionRequestDto {

    // 문제 ID
    private Long problemId;

    // 프론트 JSON 키: "sourceCode" -> 여기로 매핑
    @JsonProperty("sourceCode")
    private String code;

    // 사용 언어 (python / java / cpp ...)
    private String language;

    // 유저 ID (없으면 1로 기본값 줄 수도 있음)
    private Long userId;
}
