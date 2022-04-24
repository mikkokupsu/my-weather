package com.myweather.backend.util;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    // Secret key is the same for the lifetime of the service.
    private static final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private JwtUtil() {
        // NOOP
    }
    
    /**
     * Create a JWT access token that is valid for an hour.
     * @param username Username to set as subject.
     * @param claims Claims added to token payload.
     * @return Access token (without "Bearer " prefix).
     */
    public static String createAccessToken(final String username, final Map<String, Object> claims) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                    .setExpiration(Date.from(now.plusHours(1).toInstant(ZoneOffset.UTC)))
                    .signWith(secretKey).compact();
    }

    /**
     * Validate access token and return claims from the token.
     * @param accessToken Access token to be validated.
     * @return Claims from the token.
     */
    public static Claims validateAccessToken(final String accessToken) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
        return claims.getBody();
    }
}
