package com.example.skillboost.auth.controller;

import com.example.skillboost.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증 (Authentication)", description = "로그인, 토큰 재발급, 로그아웃")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    @Operation(summary = "GitHub 로그인 URL 반환",
            description = "프론트엔드에서 이 주소로 GET 요청을 보내면, 사용자가 접속해야 할 GitHub 로그인 페이지 URL을 반환합니다.")
    @GetMapping("/github-login-url")
    public Map<String, String> getGithubLoginUrl() {
        return Map.of("url", "/oauth2/authorization/github");
    }


    @Operation(summary = "토큰 재발급 (RTR)", description = "Refresh Token을 헤더에 담아 보내면 새로운 Access/Refresh Token을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> reissue(@RequestHeader("RefreshToken") String refreshToken) {
        String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        String[] newTokens = tokenService.rotateTokens(token);

        return ResponseEntity.ok(Map.of(
                "accessToken", newTokens[0],
                "refreshToken", newTokens[1]
        ));
    }

    @Operation(summary = "로그아웃", description = "Redis에서 Refresh Token을 삭제하여 더 이상 사용할 수 없게 만듭니다.")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("RefreshToken") String refreshToken) {
        String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;

        tokenService.logout(token);

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}