package com.jcatena.travelbackend.auth;

import com.jcatena.travelbackend.common.exceptions.NotFoundException;
import com.jcatena.travelbackend.user.User;
import com.jcatena.travelbackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }

        String email = auth.getName();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
