package com.jcatena.travelbackend.user;

import com.jcatena.travelbackend.user.dto.UserLoginRequest;
import com.jcatena.travelbackend.user.dto.UserRegisterRequest;
import com.jcatena.travelbackend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse register(UserRegisterRequest request) {

        // 1. Comprobar si ya existe el email
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // 2. Hashear contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 3. Crear usuario
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .build();

        User saved = userRepository.save(user);

        // 4. Devolver DTO sin contraseña
        return toResponse(saved);
    }

    public UserResponse login(UserLoginRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
