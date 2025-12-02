package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor          // JSON 역직렬화 대비용
@AllArgsConstructor
@Builder                    // startInterview()에서 builder로 만들기 좋아짐
public class InterviewStartResponse {

    // 세션 고유 ID (STT / 답변 제출 시 반드시 필요)
    private String sessionId;

    // 질문당 제한 시간(초) - 기본 60초
    private int durationSec;

    // AI 생성 기술 질문 + 인성 질문 총 5개
    private List<InterviewQuestionDto> questions;
}
