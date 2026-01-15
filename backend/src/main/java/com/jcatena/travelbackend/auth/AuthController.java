package com.jcatena.travelbackend.auth;

import com.jcatena.travelbackend.auth.dto.AuthResponse;
import com.jcatena.travelbackend.auth.dto.LoginRequest;
import com.jcatena.travelbackend.user.UserService;
import com.jcatena.travelbackend.user.dto.UserRegisterRequest;
import com.jcatena.travelbackend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("Register payload -> name: {}, email: {}", request.getName(), request.getEmail());
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
