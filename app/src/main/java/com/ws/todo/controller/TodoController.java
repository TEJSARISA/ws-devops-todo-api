package com.ws.todo.controller;

import com.ws.todo.dto.todo.TodoRequest;
import com.ws.todo.dto.todo.TodoResponse;
import com.ws.todo.security.UserPrincipal;
import com.ws.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todos", description = "CRUD endpoints for To-Do management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    @Operation(summary = "Get all todos for the authenticated user")
    public ResponseEntity<List<TodoResponse>> getTodos(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(todoService.getTodos(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a todo by id")
    public ResponseEntity<TodoResponse> getTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(todoService.getTodo(id, currentUser));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new todo")
    public ResponseEntity<TodoResponse> createTodo(
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(request, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a todo")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable UUID id,
            @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(todoService.updateTodo(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a todo")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        todoService.deleteTodo(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark a todo as completed")
    public ResponseEntity<TodoResponse> completeTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(todoService.completeTodo(id, currentUser));
    }
}
