package com.example.skillboost.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor     // JSON 역직렬화용 필수
@AllArgsConstructor    // 생성자 자동 생성
public class InterviewStartRequest {

    // GitHub 레포 주소
    private String repoUrl;
}
