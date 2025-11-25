package com.jcatena.travelbackend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
