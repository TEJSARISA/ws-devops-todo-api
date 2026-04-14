package com.ws.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ws.todo.dto.todo.TodoRequest;
import com.ws.todo.dto.todo.TodoResponse;
import com.ws.todo.entity.Priority;
import com.ws.todo.entity.Role;
import com.ws.todo.entity.Todo;
import com.ws.todo.entity.User;
import com.ws.todo.exception.ResourceNotFoundException;
import com.ws.todo.repository.TodoRepository;
import com.ws.todo.repository.UserRepository;
import com.ws.todo.security.UserPrincipal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private UserPrincipal principal;
    private User user;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        principal = new UserPrincipal(userId, "Test User", "test@example.com", "encoded-password", Role.USER);

        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
    }

    @Test
    void createTodoShouldAssignOwnerAndReturnResponse() {
        TodoRequest request = new TodoRequest(
                "Write tests",
                "Add service coverage",
                Priority.HIGH,
                LocalDate.now().plusDays(1));

        Todo savedTodo = new Todo();
        savedTodo.setTitle(request.title());
        savedTodo.setDescription(request.description());
        savedTodo.setPriority(request.priority());
        savedTodo.setDueDate(request.dueDate());
        savedTodo.setUser(user);
        savedTodo.setCompleted(false);

        given(userRepository.findById(principal.getId())).willReturn(Optional.of(user));
        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        TodoResponse response = todoService.createTodo(request, principal);

        assertThat(response.title()).isEqualTo("Write tests");
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void getTodoShouldThrowWhenTodoDoesNotBelongToUser() {
        UUID todoId = UUID.randomUUID();
        given(todoRepository.findByIdAndUser_Id(eq(todoId), eq(principal.getId()))).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> todoService.getTodo(todoId, principal));
    }
}
