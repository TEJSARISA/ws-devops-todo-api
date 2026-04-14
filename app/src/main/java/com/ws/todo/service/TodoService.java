package com.ws.todo.service;

import com.ws.todo.dto.todo.TodoRequest;
import com.ws.todo.dto.todo.TodoResponse;
import com.ws.todo.entity.Todo;
import com.ws.todo.entity.User;
import com.ws.todo.exception.ResourceNotFoundException;
import com.ws.todo.repository.TodoRepository;
import com.ws.todo.repository.UserRepository;
import com.ws.todo.security.UserPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodos(UserPrincipal currentUser) {
        List<Todo> todos = currentUser.isAdmin()
                ? todoRepository.findAllByOrderByCreatedAtDesc()
                : todoRepository.findAllByUser_IdOrderByCreatedAtDesc(currentUser.getId());

        return todos.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodo(UUID id, UserPrincipal currentUser) {
        return mapToResponse(getAccessibleTodo(id, currentUser));
    }

    @Transactional
    public TodoResponse createTodo(TodoRequest request, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        Todo todo = new Todo();
        todo.setTitle(request.title().trim());
        todo.setDescription(normalize(request.description()));
        todo.setPriority(request.priority());
        todo.setDueDate(request.dueDate());
        todo.setUser(user);

        Todo savedTodo = todoRepository.save(todo);
        return mapToResponse(savedTodo);
    }

    @Transactional
    public TodoResponse updateTodo(UUID id, TodoRequest request, UserPrincipal currentUser) {
        Todo todo = getAccessibleTodo(id, currentUser);
        todo.setTitle(request.title().trim());
        todo.setDescription(normalize(request.description()));
        todo.setPriority(request.priority());
        todo.setDueDate(request.dueDate());

        Todo updatedTodo = todoRepository.save(todo);
        return mapToResponse(updatedTodo);
    }

    @Transactional
    public void deleteTodo(UUID id, UserPrincipal currentUser) {
        Todo todo = getAccessibleTodo(id, currentUser);
        todoRepository.delete(todo);
    }

    @Transactional
    public TodoResponse completeTodo(UUID id, UserPrincipal currentUser) {
        Todo todo = getAccessibleTodo(id, currentUser);
        todo.setCompleted(true);
        Todo updatedTodo = todoRepository.save(todo);
        return mapToResponse(updatedTodo);
    }

    private Todo getAccessibleTodo(UUID id, UserPrincipal currentUser) {
        return currentUser.isAdmin()
                ? todoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Todo not found"))
                : todoRepository.findByIdAndUser_Id(id, currentUser.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
    }

    private TodoResponse mapToResponse(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getPriority(),
                todo.getDueDate(),
                todo.getCreatedAt(),
                todo.getUpdatedAt(),
                todo.getUser().getId());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
