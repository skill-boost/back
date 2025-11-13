package com.example.skillboost.auth.service;

import com.example.skillboost.repository.UserRepository;
import com.example.skillboost.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String githubId = String.valueOf(attributes.get("id"));
        String username = (String) attributes.get("login");

        if (email == null) {
            email = githubId + "@github.temp";
        }

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setGithubId(githubId);
                    existing.setUsername(username);
                    return existing;
                })
                .orElse(User.builder()
                        .email(email)
                        .username(username)
                        .githubId(githubId)
                        .provider("github")
                        .build());

        userRepository.save(user);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );
    }
}
