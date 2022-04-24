package com.myweather.backend.controller;

import java.util.Map;
import java.util.stream.Collectors;

import com.myweather.backend.controller.model.LoginRequest;
import com.myweather.backend.controller.model.LoginResponse;
import com.myweather.backend.util.JwtUtil;
import com.myweather.exception.Unauthorized;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    LoginResponse login(@RequestBody LoginRequest loginRequest) throws Unauthorized {
        // Sanity check input variables
        if (loginRequest.username == null || "".equals(loginRequest.username) ||
                loginRequest.password == null || "".equals(loginRequest.password)) {
            throw new Unauthorized("LoginRequest was missing username and/or password");
        }

        User user;
        try {
            // Try to authenticate
            logger.debug(() -> String.format("Attempting login for user %s", loginRequest.username));
            Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password));
            user = (User) authenticate.getPrincipal();
        } catch (Exception ex) {
            logger.error(String.format("Failed login for user %s", loginRequest.username), ex);
            throw new Unauthorized(String.format("Failed login for user %s", loginRequest.username));
        }

        logger.debug(() -> String.format("Login successful for user %s", loginRequest.username));

        // Pack user roles and create access token for user
        String roles = String.join(",", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return new LoginResponse(JwtUtil.createAccessToken( user.getUsername(), Map.of("roles", roles)));
    }
}