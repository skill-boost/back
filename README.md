# SkillBoost — Backend

코딩테스트·코드리뷰·모의면접을 한 곳에서 제공하는 개발자 취업 역량 강화 플랫폼의 백엔드 서버입니다.

- **기간**: 2025년 2학기 (오픈소스 수업)
- **팀 구성**: 5인
- **저장소**: [Frontend](https://github.com/skill-boost/front)

<br>

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.7, Spring Data JPA, Spring WebFlux |
| Auth | Spring Security, OAuth2 Client (GitHub), JWT (jjwt 0.11.5) |
| Cache | Spring Data Redis |
| Speech | Vosk (음성 인식) |
| DB | MySQL, H2 (테스트) |
| Docs | Springdoc OpenAPI |

<br>

## 개발 환경

| 구분 | 사용 도구 |
| --- | --- |
| Build | Gradle |
| Container | Docker, docker-compose |
| Orchestration | Kubernetes |
| CI/CD | GitHub Actions |
| Dev Tool | Spring Boot DevTools |

<br>

## 아키텍처

```
src/main/java/com/example/skillboost
├── auth          # GitHub OAuth2 로그인, JWT 발급·검증, 리프레시 토큰 로테이션
├── codingtest    # 코딩테스트 문제 제공 및 채점
├── codeReview    # 코드리뷰 요청·조회
└── interview     # 음성 인식 기반 모의면접
```

<br>

## 주요 기능

### 인증

GitHub OAuth2 소셜 로그인 후 JWT를 발급합니다. 리프레시 토큰은 Redis에 저장하고, 재발급 시 기존 토큰을 폐기하는 **로테이션 방식**을 적용해 토큰 탈취 상황에 대응합니다.

### 코딩테스트

문제 목록을 제공하고 제출된 답안을 채점합니다.

### 코드리뷰

작성한 코드에 대한 리뷰를 요청하고 결과를 조회합니다.

### 모의면접

Vosk 음성 인식 엔진으로 답변을 텍스트로 변환합니다. 음성 처리는 응답 지연이 크기 때문에 **WebFlux 기반 논블로킹 처리**로 구성했습니다.

<br>

## API

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/auth/login` | GitHub OAuth2 로그인 URL 발급 |
| `POST` | `/auth/reissue` | 리프레시 토큰 재발급 (로테이션) |
| `GET` | `/codingtest/problems` | 코딩테스트 문제 목록 |
| `POST` | `/codingtest/submit` | 답안 제출 및 채점 |
| `-` | `/codereview` | 코드리뷰 요청·조회 |
| `-` | `/interview` | 모의면접 세션 / 음성 처리 |

애플리케이션 실행 후 `/swagger-ui/index.html`에서 전체 명세를 확인할 수 있습니다.

<br>

## 실행 방법

```bash
# 의존 서비스 기동 (MySQL, Redis)
docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

### 환경 변수

`application.yml` 또는 환경 변수로 아래 값을 설정합니다.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

jwt:
  secret: ${JWT_SECRET}
```

Kubernetes 배포 매니페스트는 `k8s/` 디렉토리에 있습니다.

<br>

## 컨벤션

- **커밋**: `<타입>: <제목> (#이슈번호)` — 예) `setting: app.yaml 수정`
- **브랜치**: 기능 단위 브랜치에서 작업 후 PR 병합

<br>

