package com.taskmanager.TaskManagementApplication.service;

import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import com.taskmanager.TaskManagementApplication.dto.CreateTaskRequest;
import com.taskmanager.TaskManagementApplication.dto.UpdateTaskRequest;
import com.taskmanager.TaskManagementApplication.exception.TaskNotFoundException;
import com.taskmanager.TaskManagementApplication.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public Task createTask(CreateTaskRequest request) {
        Task task = Task.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING)
                .dueDate(request.getDueDate())
                .build();

        return taskRepository.save(task);
    }

    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    public Task updateTask(String id, UpdateTaskRequest request) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(request.getDueDate());
        }

        return taskRepository.save(existing);
    }

    @Override
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<Task> listTasks(TaskStatus status, int page, int size) {
        List<Task> tasks;

        if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        tasks = tasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        if (size > 0) {
            int fromIndex = page * size;
            if (fromIndex >= tasks.size()) {
                return List.of();
            }
            int toIndex = Math.min(fromIndex + size, tasks.size());
            return tasks.subList(fromIndex, toIndex);
        }

        return tasks;
    }
}
