package com.ws.todo.repository;

import com.ws.todo.entity.Todo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Todo> findById(UUID id);

    @EntityGraph(attributePaths = "user")
    List<Todo> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "user")
    List<Todo> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<Todo> findByIdAndUser_Id(UUID id, UUID userId);
}
