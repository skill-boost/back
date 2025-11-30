package com.example.skillboost.auth.handler;

import com.example.skillboost.auth.JwtProvider;
import com.example.skillboost.auth.service.TokenService;
import com.example.skillboost.domain.User;
import com.example.skillboost.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2 인증 성공!");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        if (email == null || email.isEmpty()) {
            String githubId = String.valueOf(oAuth2User.getAttributes().get("id"));
            email = githubId + "@github.temp";
            log.warn("이메일 비공개 사용자 - 임시 이메일 사용: {}", email);
        }

        final String finalEmail = email;

        User user = userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", finalEmail);
                    return new RuntimeException("User not found: " + finalEmail);
                });

        String accessToken = jwtProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        tokenService.saveRefreshToken(user.getEmail(), refreshToken);

        log.info("JWT 토큰 생성 및 Redis 저장 완료: {}", user.getEmail());

        String frontendBaseUrl = "http://localhost:3000";  // 배포 시에는 Vercel 주소로 변경

        // 프론트로 리다이렉트 (Vite dev 서버 기준)
        String redirectUrl = frontendBaseUrl
                + "/oauth/github/callback"
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(
                user.getUsername() != null ? user.getUsername() : "",
                StandardCharsets.UTF_8
        );

        log.info("프론트로 리다이렉트: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
        // JSON 응답 생성
        // Map<String, Object> responseData = new HashMap<>();
        // responseData.put("success", true);
        // responseData.put("accessToken", accessToken);
        // responseData.put("refreshToken", refreshToken); // 프론트엔드에서 저장해야 함
        // responseData.put("email", user.getEmail());
        // responseData.put("username", user.getUsername());

        // 클라이언트에 JWT 응답
        // response.setContentType("application/json;charset=UTF-8");
        // response.setStatus(HttpServletResponse.SC_OK);
        // response.getWriter().write(objectMapper.writeValueAsString(responseData));

        // 실제 서비스 배포 시, 사용자를 다시 웹사이트 메인 화면으로 돌려보내기 위해 사용
        // response.sendRedirect("http://localhost:3000/oauth2/redirect?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
    }
}