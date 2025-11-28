FROM gradle:8.8-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 활용
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies || true

# 소스 코드 복사
COPY . .

# 빌드
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

FROM amazoncorretto:21

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
