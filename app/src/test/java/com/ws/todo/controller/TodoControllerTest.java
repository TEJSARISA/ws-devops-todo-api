package com.ws.todo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.ws.todo.dto.todo.TodoRequest;
import com.ws.todo.dto.todo.TodoResponse;
import com.ws.todo.entity.Priority;
import com.ws.todo.entity.Role;
import com.ws.todo.security.UserPrincipal;
import com.ws.todo.service.TodoService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TodoControllerTest {

    @Test
    void getTodosShouldReturnAuthenticatedUserTodos() {
        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "Dev User",
                "dev@example.com",
                "encoded-password",
                Role.USER);

        TodoService todoService = new TodoService(null, null) {
            @Override
            public List<TodoResponse> getTodos(UserPrincipal currentUser) {
                return List.of(new TodoResponse(
                        UUID.randomUUID(),
                        "Ship release",
                        "Prepare deploy checklist",
                        false,
                        Priority.HIGH,
                        LocalDate.now().plusDays(1),
                        Instant.now(),
                        Instant.now(),
                        currentUser.getId()));
            }
        };

        TodoController controller = new TodoController(todoService);
        ResponseEntity<List<TodoResponse>> response = controller.getTodos(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).title()).isEqualTo("Ship release");
    }

    @Test
    void createTodoShouldReturnCreatedTodo() {
        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "Dev User",
                "dev@example.com",
                "encoded-password",
                Role.USER);
        UUID todoId = UUID.randomUUID();

        TodoService todoService = new TodoService(null, null) {
            @Override
            public TodoResponse createTodo(TodoRequest request, UserPrincipal currentUser) {
                return new TodoResponse(
                        todoId,
                        request.title(),
                        request.description(),
                        false,
                        request.priority(),
                        request.dueDate(),
                        Instant.now(),
                        Instant.now(),
                        currentUser.getId());
            }
        };

        TodoController controller = new TodoController(todoService);
        ResponseEntity<TodoResponse> response = controller.createTodo(
                new TodoRequest(
                        "Review alarms",
                        "Add CloudWatch alerting later",
                        Priority.MEDIUM,
                        LocalDate.now().plusDays(2)),
                principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(todoId);
        assertThat(response.getBody().priority()).isEqualTo(Priority.MEDIUM);
    }
}
