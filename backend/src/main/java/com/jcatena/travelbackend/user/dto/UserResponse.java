package com.jcatena.travelbackend.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;

    // AuthResponse.java
    public record AuthResponse(
            String token
    ) {}

}
