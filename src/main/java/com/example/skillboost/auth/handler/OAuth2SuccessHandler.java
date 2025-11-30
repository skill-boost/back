package com.example.skillboost.auth.handler;

import com.example.skillboost.auth.JwtProvider;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2 인증 성공!");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        // GitHub에서 이메일을 비공개로 설정한 경우 처리
        if (email == null || email.isEmpty()) {
            String githubId = String.valueOf(oAuth2User.getAttributes().get("id"));
            email = githubId + "@github.temp";
            log.warn("이메일 비공개 사용자 - 임시 이메일 사용: {}", email);
        }

        // Lambda에서 사용하기 위한 final 변수
        final String finalEmail = email;

        // 사용자 조회
        User user = userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", finalEmail);
                    return new RuntimeException("User not found: " + finalEmail);
                });

        // JWT 토큰 생성
        String token = jwtProvider.createToken(user.getEmail());
        log.info("JWT 토큰 생성 완료: {}", user.getEmail());

        // JSON 응답 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("token", token);
        responseData.put("email", user.getEmail());
        responseData.put("username", user.getUsername());

        // 클라이언트에 JWT 응답
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(responseData));

        // 프론트엔드로 리다이렉트하려면 아래 주석 해제
        // response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
    }
}