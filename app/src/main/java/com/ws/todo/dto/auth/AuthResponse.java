package com.ws.todo.dto.auth;

import com.ws.todo.dto.common.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user) {
}
