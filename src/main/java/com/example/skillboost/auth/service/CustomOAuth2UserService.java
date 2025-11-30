package com.example.skillboost.auth.service;

import com.example.skillboost.domain.User;
import com.example.skillboost.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        // GitHub에서 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("GitHub OAuth2 사용자 정보: {}", attributes);

        // GitHub 사용자 정보 추출
        String email = (String) attributes.get("email");
        String githubId = String.valueOf(attributes.get("id"));
        String username = (String) attributes.get("login");

        // 이메일이 비공개인 경우 임시 이메일 생성
        if (email == null || email.isEmpty()) {
            email = githubId + "@github.temp";
            log.warn("이메일 비공개 사용자 - 임시 이메일 생성: {}", email);
        }

        // 사용자 저장 또는 업데이트
        final String finalEmail = email;
        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    log.info("기존 사용자 업데이트: {}", finalEmail);
                    existing.setGithubId(githubId);
                    existing.setUsername(username);
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("새로운 사용자 생성: {}", finalEmail);
                    return User.builder()
                            .email(finalEmail)
                            .username(username)
                            .githubId(githubId)
                            .provider("github")
                            .build();
                });

        userRepository.save(user);
        log.info("사용자 정보 저장 완료: {} (GitHub ID: {})", user.getEmail(), user.getGithubId());

        // OAuth2User 객체 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "id"
        );
    }
}