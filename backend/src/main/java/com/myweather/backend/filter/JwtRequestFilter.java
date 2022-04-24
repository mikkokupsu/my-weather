package com.myweather.backend.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myweather.backend.util.JwtUtil;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // Don't try to parse access token, let security deal with it.
            chain.doFilter(request, response);
            return;
        }

        // "Bearer " => 7 characters (includes whitespace)
        String accessToken = authorizationHeader.substring(7);
        Claims claims;
        try {
            claims = JwtUtil.validateAccessToken(accessToken);
        } catch (Exception ex) {
            // Token is invalid, block access already here
            logger.error(String.format("Failed to parse token %s", accessToken), ex);
            response.sendError(401);
            return;
        }

        // Setup user detail from validated claims
        String username = claims.getSubject();
        String[] roles = claims.get("roles", String.class).split(",");
        List<GrantedAuthority> authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        // Setup user credentials and roles
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // Mark request authenticated
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        
        chain.doFilter(request, response);
    }
}
