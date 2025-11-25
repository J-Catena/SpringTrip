package com.jcatena.travelbackend.user;

import com.jcatena.travelbackend.user.dto.UserRegisterRequest;
import com.jcatena.travelbackend.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }
}
