package com.example.skillboost.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private Key key;

    @Value("${jwt.secret-key}")
    private String secretKeyBase64;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @PostConstruct
    protected void init() {
        // Base64로 인코딩된 secret key를 디코딩하여 Key 객체 생성
        byte[] keyBytes = Base64.getDecoder().decode(this.secretKeyBase64);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Provider 초기화 완료");
    }

    /**
     * JWT 토큰 생성
     */
    public String createToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + this.expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료되었습니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("JWT 서명이 올바르지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰에서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();

        User principal = new User(
                email,
                "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}