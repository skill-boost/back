package com.example.skillboost.auth.handler;


import com.example.skillboost.auth.JwtProvider;
import com.example.skillboost.domain.User;
import com.example.skillboost.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        if (email == null) {
            // GitHub 일부 계정은 이메일 비공개일 수 있음
            email = oAuth2User.getAttributes().get("id") + "@github.temp";
        }

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  JWT 생성
        String token = jwtProvider.createToken(user.getEmail());

        //  클라이언트에 JWT 응답 (Redirect or JSON 응답)
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"token\": \"" + token + "\"}");
    }
}