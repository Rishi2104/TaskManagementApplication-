package com.taskmanager.TaskManagementApplication.controller;

import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import com.taskmanager.TaskManagementApplication.dto.CreateTaskRequest;
import com.taskmanager.TaskManagementApplication.dto.TaskResponse;
import com.taskmanager.TaskManagementApplication.dto.UpdateTaskRequest;
import com.taskmanager.TaskManagementApplication.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task updated = taskService.updateTask(id, request);
        return ResponseEntity.ok(TaskResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size) {

        List<Task> tasks = taskService.listTasks(status, page, size);
        List<TaskResponse> response = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
