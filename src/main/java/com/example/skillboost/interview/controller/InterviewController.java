package com.example.skillboost.interview.controller;

import com.example.skillboost.interview.dto.InterviewFeedbackRequest;
import com.example.skillboost.interview.dto.InterviewFeedbackResponse;
import com.example.skillboost.interview.dto.InterviewStartRequest;
import com.example.skillboost.interview.dto.InterviewStartResponse;
import com.example.skillboost.interview.service.InterviewFeedbackService;
import com.example.skillboost.interview.service.InterviewService;
import com.example.skillboost.interview.service.SpeechToTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewFeedbackService feedbackService;
    private final SpeechToTextService speechToTextService;

    // 1) 면접 시작 + 질문 생성
    @PostMapping("/start")
    public ResponseEntity<InterviewStartResponse> start(@RequestBody InterviewStartRequest request) {
        InterviewStartResponse response = interviewService.startInterview(request);
        return ResponseEntity.ok(response);
    }

    // 2) (텍스트 기반) 전체 답변 평가
    @PostMapping("/feedback")
    public ResponseEntity<InterviewFeedbackResponse> feedback(
            @RequestBody InterviewFeedbackRequest request
    ) {
        InterviewFeedbackResponse response = feedbackService.createFeedback(request);
        return ResponseEntity.ok(response);
    }

    // 3) 🔊 음성 → 텍스트(STT)만 담당
    @PostMapping("/stt")
    public ResponseEntity<Map<String, String>> stt(
            @RequestPart("audio") MultipartFile audioFile
    ) {
        String text = speechToTextService.transcribe(audioFile);
        return ResponseEntity.ok(Map.of("text", text));
    }
}
