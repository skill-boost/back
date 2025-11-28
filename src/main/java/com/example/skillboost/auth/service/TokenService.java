package com.example.skillboost.auth.service;

import com.example.skillboost.auth.JwtProvider;
import com.example.skillboost.domain.RefreshToken;
import com.example.skillboost.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    /**
     * 1. Refresh Token 저장
     */
    @Transactional
    public void saveRefreshToken(String userId, String token) {
        RefreshToken refreshToken = new RefreshToken(token, userId);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 2. 토큰 재발급 (Refresh Token Rotation)
     * - 기존 토큰이 유효한지 확인
     * - Redis에 존재하는지 확인
     * - 기존 토큰 삭제 (Rotation)
     * - 새 토큰 발급 및 저장
     */
    @Transactional
    public String[] rotateTokens(String oldRefreshToken) {
        if (!jwtProvider.validateToken(oldRefreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        RefreshToken tokenEntity = refreshTokenRepository.findById(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("이미 사용되었거나 존재하지 않는 Refresh Token입니다. 다시 로그인하세요."));

        refreshTokenRepository.delete(tokenEntity);

        String userId = tokenEntity.getUserId();

        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        saveRefreshToken(userId, newRefreshToken);

        log.info("토큰 Rotation 성공, [ User: {} ]", userId);
        return new String[]{newAccessToken, newRefreshToken};
    }

    /**
     * 3. 로그아웃 (Redis에서 삭제)
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findById(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
        log.info("로그아웃 처리 완료 (Redis 삭제)");
    }
}