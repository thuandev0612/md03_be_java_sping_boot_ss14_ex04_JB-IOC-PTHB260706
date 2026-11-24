package com.example.baitapsession14.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {
    private final byte[] secretKey;
    private final long expirationMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Base64.getDecoder().decode(secret);
        if (secretKey.length < 32 || expirationMs <= 0) {
            throw new IllegalArgumentException("JWT cần khóa ít nhất 32 byte và thời gian hết hạn dương");
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(principal.getUsername())
                .claim("role", principal.getUser().getRole())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String validateAndGetUsername(String token) {
        // parseClaimsJws checks both the signature and expiration before returning claims.
        var jwt = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        var claims = jwt.getBody();
        String username = claims.getSubject();
        if (!SignatureAlgorithm.HS256.getValue().equals(jwt.getHeader().getAlgorithm())
                || claims.getExpiration() == null || username == null || username.isBlank()) {
            throw new MalformedJwtException("Token không hợp lệ");
        }
        return username;
    }
}
