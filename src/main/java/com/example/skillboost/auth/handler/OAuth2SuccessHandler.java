package com.example.skillboost.auth.handler;
import com.example.skillboost.auth.JwtProvider;
import com.example.skillboost.auth.service.TokenService;
import com.example.skillboost.domain.User;
import com.example.skillboost.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    private static final String FRONTEND_REDIRECT_BASE_URL = "https://www.skill-boost.store";
    private static final String FRONTEND_TOKEN_HANDLER_PATH = "/oauth2/redirect";


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2 인증 성공!");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String githubId = String.valueOf(attributes.get("id"));

        if (email == null || email.isEmpty()) {
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

        String targetUrl = UriComponentsBuilder
                .fromUriString(FRONTEND_REDIRECT_BASE_URL + FRONTEND_TOKEN_HANDLER_PATH)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("email", user.getEmail())
                .queryParam("username", user.getUsername())
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}