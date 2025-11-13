package com.example.skillboost.auth;

import com.example.skillboost.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;


@Component
public class JwtProvider {

    private  Key key;
    @Value("${jwt.secret-key}")
    private String secretKeyBase64;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public JwtProvider() {
    }

    void init() {
        // application.yml의 jwt.secret-key 값과 동일해야 함
        byte[] keyBytes = Base64.getDecoder().decode(this.secretKeyBase64);        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.key = Keys.hmacShaKeyFor(keyBytes);    }

    //  JWT 생성
    public String createToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + this.expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //  JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 만료됨");
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT 토큰이 유효하지 않음");
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        username, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
                );
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

}
