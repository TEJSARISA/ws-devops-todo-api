package com.ws.todo.dto.todo;

import com.ws.todo.entity.Priority;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TodoResponse(
        UUID id,
        String title,
        String description,
        boolean completed,
        Priority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt,
        UUID userId) {
}
