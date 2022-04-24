package com.myweather.backend.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

    @JsonProperty("username")
    public final String username;

    @JsonProperty("password")
    public final String password;

    LoginRequest(final String username, final String password) {
        this.username = username;
        this.password = password;
    }
}
