package com.example.skillboost.interview.service;

import com.example.skillboost.interview.dto.InterviewAnswerDto;
import com.example.skillboost.interview.dto.InterviewQuestionDto;
import com.example.skillboost.interview.dto.InterviewStartRequest;
import com.example.skillboost.interview.dto.InterviewStartResponse;
import com.example.skillboost.interview.dto.QuestionType;
import com.example.skillboost.interview.model.InterviewSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final int QUESTION_DURATION_SEC = 60;

    // 인메모리 세션 저장소
    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();

    private final GeminiClient geminiClient;
    private final SpeechToTextService speechToTextService;
    private final ObjectMapper objectMapper;

    // 인성 질문 풀
    private static final List<String> BEHAV_QUESTIONS = List.of(
            "가장 최근에 도전적인 일을 경험한 적이 있다면 설명해 주세요.",
            "팀 프로젝트에서 갈등을 겪은 적이 있다면, 어떻게 해결했나요?",
            "본인의 성격 중 강점과 약점을 각각 설명해 주세요.",
            "압박감이 큰 상황에서는 어떻게 스트레스를 관리하나요?",
            "어려운 문제를 만났을 때 해결하기 위해 어떤 접근 방식을 사용하나요?",
            "주변 사람들에게 어떤 사람으로 기억되고 싶나요?",
            "새로운 기술을 배울 때 본인만의 학습 방법이 있나요?",
            "실수했던 경험이 있다면 어떻게 대응했나요?",
            "목표를 설정한 뒤 성취하기 위해 어떤 계획을 세우나요?",
            "여러 작업을 동시에 처리해야 할 때 우선순위는 어떻게 정하나요?",
            "리더 역할을 맡아본 적이 있다면 어떤 방식으로 팀을 이끌었나요?",
            "본인이 맡았던 일 중 가장 책임감 있게 완수한 경험을 말해 주세요.",
            "지속적으로 성장하기 위해 하고 있는 노력은 무엇인가요?",
            "비판적인 피드백을 받았을 때 어떻게 반응하나요?",
            "혼자 일할 때와 팀으로 일할 때 각각 어떤 스타일인가요?",
            "가장 뿌듯했던 성취 경험을 말해 주세요.",
            "예상치 못한 문제가 발생했을 때 대응했던 경험을 이야기해 주세요.",
            "협업 과정에서 소통을 원활하게 하기 위해 어떤 노력을 하나요?",
            "새로운 환경이나 변화에 적응했던 경험을 말해 주세요.",
            "성과를 내지 못한 경험이 있다면 무엇을 배우셨나요?",
            "갈등 상황에서 감정을 다스리는 본인만의 방법이 있나요?",
            "주도적으로 문제를 해결했던 경험을 설명해 주세요.",
            "가장 최근에 배운 기술이나 지식은 무엇이며, 어떻게 활용했나요?",
            "조직이나 팀에 긍정적인 영향을 준 경험이 있다면 설명해 주세요.",
            "본인의 가치관 중 일을 할 때 가장 중요하게 생각하는 것은 무엇인가요?",
            "스스로 부족하다고 느끼는 점은 무엇이고, 어떻게 개선하고 있나요?",
            "업무나 학업에서 동기부여가 필요할 때 어떻게 동기를 찾나요?",
            "복잡한 문제를 단순화해서 해결했던 경험이 있나요?",
            "시간 압박 속에서 빠르게 결정을 내려야 했던 상황을 말해 주세요.",
            "새로운 역할을 맡았을 때 빠르게 적응하기 위해 무엇을 했나요?",
            "목표 달성이 어려워졌을 때 포기하지 않고 노력했던 경험을 말해 주세요.",
            "본인이 경험한 가장 큰 실패는 무엇이고 무엇을 배우셨나요?",
            "팀원과 의견 차이가 있을 때 어떻게 설득하거나 조율하나요?",
            "집중력이 떨어질 때 다시 집중력을 끌어올리는 방법이 있나요?",
            "주변 사람과 신뢰를 쌓기 위해 어떤 노력을 하나요?",
            "업무 효율을 높이기 위해 본인이 자주 사용하는 방식이나 도구가 있나요?",
            "예상보다 일이 오래 걸릴 때 본인의 태도는 어떠한가요?",
            "가장 인상 깊었던 협업 경험을 이야기해 주세요.",
            "기대치보다 낮은 평가를 받았을 때 어떻게 대처했나요?",
            "타인의 입장에서 생각해야 했던 경험을 말해 주세요.",
            "누군가에게 도움을 요청해야 했던 상황이 있다면 설명해 주세요.",
            "팀 분위기가 좋지 않을 때 본인이 기여할 수 있는 부분은 무엇인가요?",
            "맡았던 일을 끝까지 책임지기 위해 어떤 노력을 하나요?",
            "어떤 상황에서 본인의 리더십이 발휘된다고 생각하나요?",
            "가장 마지막으로 읽었던 책이나 들었던 강의는 무엇인가요?",
            "어려운 결정을 내려야 했던 경험을 설명해 주세요.",
            "모르는 것을 인정하고 배우는 태도에 대해 어떻게 생각하나요?",
            "본인의 단점을 보완하기 위해 꾸준히 실천하고 있는 습관이 있나요?",
            "스스로에게 가장 자랑스러운 순간은 언제였나요?",
            "상사에게 부당한 지시를 받았을 때 어떻게 대처하나요?"
    );

    // ---------------------------------------------------
    // 0. 음성 답변 → STT → Answer DTO 생성
    // ---------------------------------------------------
    public InterviewAnswerDto processAnswer(
            String sessionId,
            int questionIndex,          // 프론트에서 0-based 인덱스로 보낸다고 가정
            MultipartFile audioFile
    ) {
        // 1) 세션 찾기
        InterviewSession session = findSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        List<InterviewQuestionDto> questions = session.getQuestions();
        if (questionIndex < 0 || questionIndex >= questions.size()) {
            throw new IllegalArgumentException("잘못된 questionIndex 입니다.");
        }

        InterviewQuestionDto questionDto = questions.get(questionIndex);

        // 2) 🔊 STT: 음성을 텍스트로 변환
        String answerText = speechToTextService.transcribe(audioFile);

        // 3) 프론트에 돌려줄 Answer DTO 생성
        //    - 프론트는 이걸 answers 배열에 모았다가 /feedback 에서 한 번에 보냄
        return InterviewAnswerDto.builder()
                .questionId(questionDto.getId())
                .type(questionDto.getType())
                .question(questionDto.getText())
                .answerText(answerText)
                .durationSec(0)   // TODO: 나중에 원하면 프론트에서 실제 답변 시간 보내서 채워도 됨
                .build();
    }

    // ---------------------------------------------------
    // 1. 면접 시작 + 질문 생성
    // ---------------------------------------------------
    public InterviewStartResponse startInterview(InterviewStartRequest request) {
        String repoUrl = request.getRepoUrl();

        // 1. 기술 질문 3개: Gemini 기반
        List<InterviewQuestionDto> techQuestions = generateTechQuestionsWithGemini(repoUrl);

        // 2. 인성 질문 2개: 기존 50개 중 랜덤
        List<InterviewQuestionDto> behavQuestions = pickRandomBehavQuestions(2);

        // 3. 합치고 섞기
        List<InterviewQuestionDto> all = new ArrayList<>();
        all.addAll(techQuestions);
        all.addAll(behavQuestions);
        Collections.shuffle(all);

        // 4. id를 1~N 으로 재부여
        List<InterviewQuestionDto> numbered = LongStream
                .rangeClosed(1, all.size())
                .mapToObj(i -> new InterviewQuestionDto(
                        i,
                        all.get((int) i - 1).getType(),
                        all.get((int) i - 1).getText()
                ))
                .collect(Collectors.toList());

        // 5. 세션 생성 & 저장
        String sessionId = UUID.randomUUID().toString();
        InterviewSession session = InterviewSession.create(sessionId, repoUrl, numbered);
        sessions.put(sessionId, session);

        return InterviewStartResponse.builder()
                .sessionId(sessionId)
                .durationSec(QUESTION_DURATION_SEC)
                .questions(numbered)
                .build();
    }

    /**
     * Gemini를 사용하여 repoUrl 기반 기술 질문 3개 생성
     * - JSON 배열로만 응답하도록 강제
     */
    private List<InterviewQuestionDto> generateTechQuestionsWithGemini(String repoUrl) {
        String repoName = extractRepoName(repoUrl);

        String prompt = """
        당신은 시니어 백엔드 개발자 면접관입니다.
        아래 GitHub 레포지토리를 기반으로 이 프로젝트를 개발한 지원자에게 물어볼
        기술 면접 질문 3개를 만들어 주세요.

        레포지토리 URL: %s
        레포지토리 이름: %s
        이 프로젝트는 코딩테스트, 코드 리뷰, AI 면접 등 개발자 역량 강화를 위한 웹 서비스라고 가정합니다.

        ❗질문 스타일 제한
        - 각 질문은 **1문장**으로만 작성하세요.
        - 길이는 최대 **80자 이내**로 해 주세요.
        - 불필요한 배경 설명, 예시는 넣지 마세요.
        - "핵심이 무엇인가요?" 같은 추상적인 질문은 피하고,
          "어떤 클래스/레이어에서 무엇을 어떻게 처리했는지"처럼
          **구현·설계를 구체적으로 묻는 질문**으로만 작성하세요.

        질문 주제 예시
        - 아키텍처 구성 방식
        - 모듈 간 의존성, 레이어드 구조
        - 예외 처리, 타임아웃 처리 방식
        - 성능/확장성 고려
        - 테스트 전략, 트랜잭션 처리 등

        출력 형식 (반드시 이 JSON 배열만 출력)
        [
          { "text": "질문 내용1" },
          { "text": "질문 내용2" },
          { "text": "질문 내용3" }
        ]
        """.formatted(repoUrl, repoName);

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

        String cleaned = extractJsonArray(raw).trim();

        if (!cleaned.startsWith("[")) {
            return fallbackTechQuestions(repoName);
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

                result.add(new InterviewQuestionDto(null, QuestionType.TECH, text));
            }

            if (result.isEmpty()) {
                return fallbackTechQuestions(repoName);
            }

            return result.size() > 3 ? result.subList(0, 3) : result;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackTechQuestions(repoName);
        }
    }

    private String extractJsonArray(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();

        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) {
            return trimmed;
        }
        return trimmed.substring(start, end + 1);
    }

    private List<InterviewQuestionDto> fallbackTechQuestions(String repoName) {
        String q1 = String.format("이 레포지토리(%s)의 전체 아키텍처를 간단히 설명해 주세요.", repoName);
        String q2 = String.format("%s 프로젝트에서 주요 모듈(코딩테스트/코드리뷰/AI면접)의 역할과 연결 구조를 설명해 주세요.", repoName);
        String q3 = String.format("%s에서 외부 API(Gemini, 채점 서버 등)를 호출할 때 예외/타임아웃을 어떻게 처리했는지 설명해 주세요.", repoName);

        return List.of(
                new InterviewQuestionDto(null, QuestionType.TECH, q1),
                new InterviewQuestionDto(null, QuestionType.TECH, q2),
                new InterviewQuestionDto(null, QuestionType.TECH, q3)
        );
    }

    private String extractRepoName(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) return "이 프로젝트";
        int slash = repoUrl.lastIndexOf('/');
        if (slash == -1 || slash == repoUrl.length() - 1) return repoUrl;
        return repoUrl.substring(slash + 1);
    }

    private List<InterviewQuestionDto> pickRandomBehavQuestions(int count) {
        List<String> pool = new ArrayList<>(BEHAV_QUESTIONS);
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()))
                .stream()
                .map(text -> new InterviewQuestionDto(null, QuestionType.BEHAV, text))
                .collect(Collectors.toList());
    }

    public Optional<InterviewSession> findSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
