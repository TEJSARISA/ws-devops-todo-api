package com.ws.todo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.ws.todo.dto.auth.AuthResponse;
import com.ws.todo.dto.auth.LoginRequest;
import com.ws.todo.dto.auth.RegisterRequest;
import com.ws.todo.dto.common.UserResponse;
import com.ws.todo.entity.Role;
import com.ws.todo.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

    @Test
    void registerShouldReturnCreatedTokenPayload() {
        AuthResponse expectedResponse = new AuthResponse(
                "jwt-token",
                "Bearer",
                3600L,
                new UserResponse(UUID.randomUUID(), "Winston", "winston@example.com", Role.USER));

        AuthService authService = new AuthService(null, null, null, null, "") {
            @Override
            public AuthResponse register(RegisterRequest request) {
                return expectedResponse;
            }

            @Override
            public AuthResponse login(LoginRequest request) {
                throw new UnsupportedOperationException("Not used in this test");
            }
        };

        AuthController controller = new AuthController(authService);

        ResponseEntity<AuthResponse> response = controller.register(
                new RegisterRequest("Winston", "winston@example.com", "Password123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().user().email()).isEqualTo("winston@example.com");
    }
}
