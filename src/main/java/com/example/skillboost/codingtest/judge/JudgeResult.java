package com.example.skillboost.codingtest.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResult {
    // Judge0 표준 상태 코드 (3: Accepted, 4: Wrong Answer, 5: Time Limit, 6: Compilation Error, 11: Runtime Error)
    private int statusId;

    private String stdout;      // 표준 출력 결과
    private String stderr;      // 에러 메시지
    private String message;     // 설명
    private double time;        // 실행 시간
    private long memory;        // 메모리 사용량

    public static JudgeResult accepted(String output, double time) {
        return JudgeResult.builder()
                .statusId(3) // Accepted
                .stdout(output)
                .time(time)
                .message("Accepted")
                .build();
    }

    public static JudgeResult wrongAnswer(String output, double time) {
        return JudgeResult.builder()
                .statusId(4) // Wrong Answer
                .stdout(output)
                .time(time)
                .message("Wrong Answer")
                .build();
    }

    public static JudgeResult compileError(String errorMessage) {
        return JudgeResult.builder()
                .statusId(6) // Compilation Error
                .stderr(errorMessage)
                .message("Compilation Error")
                .build();
    }

    public static JudgeResult runtimeError(String errorMessage) {
        return JudgeResult.builder()
                .statusId(11) // Runtime Error
                .stderr(errorMessage)
                .message("Runtime Error")
                .build();
    }
}