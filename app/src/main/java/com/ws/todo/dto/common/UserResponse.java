package com.ws.todo.dto.common;

import com.ws.todo.entity.Role;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role) {
}
