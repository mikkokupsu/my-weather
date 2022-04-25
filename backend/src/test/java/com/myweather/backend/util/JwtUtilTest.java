package com.myweather.backend.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

public class JwtUtilTest {
    
    @Test
    public void testSignAndParse() throws Exception {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("testclaim", "testvalue");
        
        final String username = "testuser";
        final String token = JwtUtil.createAccessToken(username, claims);

        Claims parsed = JwtUtil.validateAccessToken(token);

        Assertions.assertEquals(username, parsed.getSubject());
        Assertions.assertEquals(claims.get("testclaim"), parsed.get("testclaim"));
    }
}
