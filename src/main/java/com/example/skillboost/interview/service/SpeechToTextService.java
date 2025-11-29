package com.example.skillboost.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class SpeechToTextService {

    private Model model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${stt.vosk-model-path}")
    private String modelPath;   // ✅ 설정에서 주입

    @PostConstruct
    public void init() {
        try {
            this.model = new Model(modelPath);
            log.info("Vosk STT 모델 로드 완료: {}", modelPath);
        } catch (IOException e) {
            log.error("Vosk 모델 로드 실패", e);
            throw new RuntimeException("Vosk 모델 로드 실패", e);
        }
    }

    public String transcribe(MultipartFile audioFile) {
        if (model == null) throw new IllegalStateException("Vosk 모델 초기화 실패");

        try {
            byte[] data = audioFile.getBytes();

            try (InputStream is = new ByteArrayInputStream(data);
                 Recognizer recognizer = new Recognizer(model, 16000)) {

                byte[] buffer = new byte[4096];
                int n;

                while ((n = is.read(buffer)) >= 0) {
                    recognizer.acceptWaveForm(buffer, n);
                }

                String resultJson = recognizer.getFinalResult();
                JsonNode root = objectMapper.readTree(resultJson);
                return root.path("text").asText("").trim();
            }
        } catch (Exception e) {
            log.error("STT 변환 실패", e);
            throw new RuntimeException(e);
        }
    }
}
