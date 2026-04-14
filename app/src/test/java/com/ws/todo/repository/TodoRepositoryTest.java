package com.ws.todo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ws.todo.entity.Priority;
import com.ws.todo.entity.Role;
import com.ws.todo.entity.Todo;
import com.ws.todo.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByIdAndUserIdShouldReturnOwnedTodo() {
        User user = new User();
        user.setName("Repo User");
        user.setEmail("repo@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        Todo todo = new Todo();
        todo.setTitle("Repository coverage");
        todo.setDescription("Verify custom JPA queries");
        todo.setPriority(Priority.HIGH);
        todo.setDueDate(LocalDate.now().plusDays(5));
        todo.setUser(savedUser);
        Todo savedTodo = todoRepository.save(todo);

        Optional<Todo> foundTodo = todoRepository.findByIdAndUser_Id(savedTodo.getId(), savedUser.getId());
        List<Todo> todos = todoRepository.findAllByUser_IdOrderByCreatedAtDesc(savedUser.getId());

        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("Repository coverage");
        assertThat(todos).hasSize(1);
        assertThat(todos.get(0).getId()).isEqualTo(savedTodo.getId());
    }

    @Test
    void findByIdAndUserIdShouldNotReturnAnotherUsersTodo() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner.setPassword("encoded-password");
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        User outsider = new User();
        outsider.setName("Outsider");
        outsider.setEmail("outsider@example.com");
        outsider.setPassword("encoded-password");
        outsider.setRole(Role.USER);
        outsider = userRepository.save(outsider);

        Todo todo = new Todo();
        todo.setTitle("Restricted item");
        todo.setPriority(Priority.LOW);
        todo.setUser(owner);
        todoRepository.save(todo);

        Optional<Todo> foundTodo = todoRepository.findByIdAndUser_Id(todo.getId(), outsider.getId());

        assertThat(foundTodo).isEmpty();
        assertThat(owner.getId()).isNotEqualTo(outsider.getId());
    }
}
