package com.example.skillboost.interview.model;

import com.example.skillboost.interview.dto.InterviewQuestionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor      // 세션 저장 시 역직렬화 대비
@AllArgsConstructor
@Builder
public class InterviewSession implements Serializable {

    private String sessionId;                 // 세션 고유 ID
    private String repoUrl;                   // 레포 주소
    private LocalDateTime createdAt;          // 세션 생성 시간
    private List<InterviewQuestionDto> questions; // 질문 리스트

    public static InterviewSession create(String sessionId, String repoUrl, List<InterviewQuestionDto> questions) {
        return InterviewSession.builder()
                .sessionId(sessionId)
                .repoUrl(repoUrl)
                .questions(questions)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
