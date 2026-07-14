package com.taskmanager.TaskManagementApplication.repository;

import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAll();
    List<Task> findByStatus(TaskStatus status);
    void deleteById(String id);
    boolean existsById(String id);
}
