package com.ws.todo.service;

import com.ws.todo.dto.auth.AuthResponse;
import com.ws.todo.dto.auth.LoginRequest;
import com.ws.todo.dto.auth.RegisterRequest;
import com.ws.todo.dto.common.UserResponse;
import com.ws.todo.entity.Role;
import com.ws.todo.entity.User;
import com.ws.todo.exception.DuplicateResourceException;
import com.ws.todo.exception.ResourceNotFoundException;
import com.ws.todo.repository.UserRepository;
import com.ws.todo.security.JwtService;
import com.ws.todo.security.UserPrincipal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final Set<String> adminEmails;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${app.security.admin-emails:admin@ws.local}") String adminEmails) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .filter(email -> !email.isBlank())
                .collect(Collectors.toSet());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(resolveRole(normalizedEmail));

        User savedUser = userRepository.save(user);
        return buildAuthResponse(UserPrincipal.from(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        return buildAuthResponse(UserPrincipal.from(user));
    }

    private Role resolveRole(String email) {
        return adminEmails.contains(email) ? Role.ADMIN : Role.USER;
    }

    private AuthResponse buildAuthResponse(UserPrincipal userPrincipal) {
        String token = jwtService.generateToken(userPrincipal);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                new UserResponse(
                        userPrincipal.getId(),
                        userPrincipal.getName(),
                        userPrincipal.getUsername(),
                        userPrincipal.getRole()));
    }
}
