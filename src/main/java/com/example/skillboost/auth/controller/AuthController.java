package com.example.skillboost.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@Tag(name = "깃허브 인증 (Authentication)", description = "소셜 로그인 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "GitHub 로그인 URL 반환",
            description = "프론트엔드에서 이 주소로 GET 요청을 보내면, 사용자가 접속해야 할 GitHub 로그인 페이지 URL을 반환합니다.")
    @GetMapping("/github-login-url")
    public Map<String, String> getGithubLoginUrl() {
        String loginUrl = "/oauth2/authorization/github";

        return Map.of("url", loginUrl);
    }
}