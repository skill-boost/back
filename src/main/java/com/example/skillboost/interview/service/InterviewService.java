package com.example.skillboost.interview.service;

import com.example.skillboost.codeReview.GithubFile;
import com.example.skillboost.codeReview.service.GithubService;
import com.example.skillboost.interview.dto.*;
import com.example.skillboost.interview.model.InterviewSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;


@Profile({"local", "prod"})
@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final int QUESTION_DURATION_SEC = 60;

    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();

    private final GeminiClient geminiClient;
    private final SpeechToTextService speechToTextService;
    private final ObjectMapper objectMapper;
    private final GithubService githubService;   // 🔥 GitHub 읽기 서비스

    // ---------------------------------------------------------
    // 음성 답변 처리
    // ---------------------------------------------------------
    public InterviewAnswerDto processAnswer(String sessionId, int questionIndex, MultipartFile audioFile) {
        InterviewSession session = findSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        List<InterviewQuestionDto> questions = session.getQuestions();
        if (questionIndex < 0 || questionIndex >= questions.size()) {
            throw new IllegalArgumentException("잘못된 questionIndex 입니다.");
        }

        InterviewQuestionDto questionDto = questions.get(questionIndex);

        String answerText = speechToTextService.transcribe(audioFile);

        return InterviewAnswerDto.builder()
                .questionId(questionDto.getId())
                .type(questionDto.getType())
                .question(questionDto.getText())
                .answerText(answerText)
                .durationSec(0)
                .build();
    }

    // ---------------------------------------------------------
    // 면접 시작
    // ---------------------------------------------------------
    public InterviewStartResponse startInterview(InterviewStartRequest request) {
        String repoUrl = request.getRepoUrl();

        List<InterviewQuestionDto> techQuestions = generateTechQuestionsWithGemini(repoUrl);
        List<InterviewQuestionDto> behavQuestions = pickRandomBehavQuestions(2); // 🔥 자동 생성된 인성 질문

        List<InterviewQuestionDto> all = new ArrayList<>();
        all.addAll(techQuestions);
        all.addAll(behavQuestions);
        Collections.shuffle(all);

        List<InterviewQuestionDto> numbered = LongStream
                .rangeClosed(1, all.size())
                .mapToObj(i -> new InterviewQuestionDto(
                        i,
                        all.get((int) i - 1).getType(),
                        all.get((int) i - 1).getText()
                )).collect(Collectors.toList());

        String sessionId = UUID.randomUUID().toString();
        InterviewSession session = InterviewSession.create(sessionId, repoUrl, numbered);
        sessions.put(sessionId, session);

        return InterviewStartResponse.builder()
                .sessionId(sessionId)
                .durationSec(QUESTION_DURATION_SEC)
                .questions(numbered)
                .build();
    }

    // ---------------------------------------------------------
    // 🔥 GitHub 레포 기반 기술 질문 생성
    // ---------------------------------------------------------
    private List<InterviewQuestionDto> generateTechQuestionsWithGemini(String repoUrl) {
        String repoName = extractRepoName(repoUrl);

        // 1) GitHub 파일 읽기
        List<GithubFile> files;
        try {
            files = githubService.fetchRepoCode(repoUrl, "main");
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackTechQuestions(repoName);
        }

        if (files == null || files.isEmpty()) {
            return fallbackTechQuestions(repoName);
        }

        // 2) 파일 내용을 하나의 큰 텍스트로 합침
        StringBuilder repoText = new StringBuilder();
        for (GithubFile f : files) {
            repoText.append("### FILE: ").append(f.getPath()).append("\n");
            repoText.append(f.getContent()).append("\n\n");
        }

        // 3) Gemini 프롬프트 생성
        String prompt = """
            당신은 시니어 백엔드 개발자 면접관입니다.
            아래는 지원자의 GitHub 레포지토리 전체 코드입니다.
            이 내용을 기반으로 기술 면접 질문 3개를 생성하세요.

            --- Repository Code Start ---
            %s
            --- Repository Code End ---

            질문 규칙:
            - 각 질문은 1문장
            - 80자 이내
            - 이 코드의 구조/설계/모듈/DTO/서비스/컨트롤러 기반
            - 추상적인 질문 금지
            - JSON 배열만 출력

            출력 형식:
            [
              { "text": "질문1" },
              { "text": "질문2" },
              { "text": "질문3" }
            ]
            """.formatted(repoText.toString());

        // 4) Gemini 호출
        String raw;
        try {
            raw = geminiClient.generateText(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackTechQuestions(repoName);
        }

        if (raw == null || raw.isBlank()) {
            return fallbackTechQuestions(repoName);
        }

        // 5) JSON 배열 추출
        String cleaned = extractJsonArray(raw).trim();
        if (!cleaned.startsWith("[")) {
            return fallbackTechQuestions(repoName);
        }

        // 6) 파싱
        try {
            List<Map<String, Object>> list = objectMapper.readValue(
                    cleaned,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            List<InterviewQuestionDto> result = new ArrayList<>();
            for (Map<String, Object> item : list) {
                Object textObj = item.get("text");
                if (textObj == null) continue;

                String text = String.valueOf(textObj).trim();
                if (text.isEmpty()) continue;

                result.add(new InterviewQuestionDto(null, QuestionType.TECH, text));
            }

            return result.size() >= 3 ? result.subList(0, 3) : fallbackTechQuestions(repoName);

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackTechQuestions(repoName);
        }
    }

    // ---------------------------------------------------------
    // 기술면접 fallback
    // ---------------------------------------------------------
    private List<InterviewQuestionDto> fallbackTechQuestions(String repoName) {
        return List.of(
                new InterviewQuestionDto(null, QuestionType.TECH,
                        repoName + " 프로젝트의 전체 아키텍처를 설명해주세요."),
                new InterviewQuestionDto(null, QuestionType.TECH,
                        repoName + " 레포의 주요 모듈 설계 의도를 설명해주세요."),
                new InterviewQuestionDto(null, QuestionType.TECH,
                        "외부 API 호출 시 예외/타임아웃 처리 방식을 설명해주세요.")
        );
    }

    // ---------------------------------------------------------
    // 🔥 Gemini 기반 인성 질문 자동 생성
    // ---------------------------------------------------------
    private List<InterviewQuestionDto> pickRandomBehavQuestions(int count) {

        String prompt = """
            당신은 인성 면접 전문 면접관입니다.
            아래 조건에 따라 인성 면접 질문을 JSON 배열 형태로 생성하세요.

            조건:
            - 심층적이지만 과도하게 추상적이지 않은 질문
            - 1문장, 60자 이내
            - 지원자의 성격·협업 능력·책임감·문제 해결 능력 중심
            - JSON 배열로만 출력

            출력 예시:
            [
              { "text": "협업 과정에서 갈등을 해결했던 경험을 말해주세요." },
              { "text": "압박이 있을 때 자신의 감정을 어떻게 관리하나요?" }
            ]

            질문 개수: %d개
            """.formatted(count);

        String raw;
        try {
            raw = geminiClient.generateText(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackBehavQuestions(count);
        }

        if (raw == null || raw.isBlank()) {
            return fallbackBehavQuestions(count);
        }

        String cleaned = extractJsonArray(raw).trim();
        if (!cleaned.startsWith("[")) {
            return fallbackBehavQuestions(count);
        }

        try {
            List<Map<String, Object>> list = objectMapper.readValue(
                    cleaned,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            List<InterviewQuestionDto> result = new ArrayList<>();
            for (Map<String, Object> item : list) {
                Object textObj = item.get("text");
                if (textObj == null) continue;

                String text = String.valueOf(textObj).trim();
                if (text.isEmpty()) continue;

                result.add(new InterviewQuestionDto(null, QuestionType.BEHAV, text));
            }

            if (result.size() < count) return fallbackBehavQuestions(count);
            return result.subList(0, count);

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackBehavQuestions(count);
        }
    }

    // ---------------------------------------------------------
    // 인성 fallback
    // ---------------------------------------------------------
    private List<InterviewQuestionDto> fallbackBehavQuestions(int count) {
        List<String> defaults = List.of(
                "협업 과정에서 갈등을 해결했던 경험을 설명해주세요.",
                "압박이 큰 상황에서 감정을 관리하는 방법을 말해주세요.",
                "가장 최근에 성장했다고 느낀 경험을 말해주세요.",
                "실수했을 때 어떻게 대응했는지 말해주세요.",
                "목표 달성을 위해 본인이 했던 노력을 설명해주세요."
        );

        Collections.shuffle(defaults);

        return defaults.subList(0, Math.min(count, defaults.size()))
                .stream()
                .map(text -> new InterviewQuestionDto(null, QuestionType.BEHAV, text))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // 기타 유틸
    // ---------------------------------------------------------
    private String extractJsonArray(String raw) {
        if (raw == null) return "";
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) return raw;
        return raw.substring(start, end + 1);
    }

    private String extractRepoName(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) return "이 프로젝트";
        int slash = repoUrl.lastIndexOf('/');
        if (slash == -1 || slash == repoUrl.length() - 1) return repoUrl;
        return repoUrl.substring(slash + 1);
    }

    public Optional<InterviewSession> findSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
