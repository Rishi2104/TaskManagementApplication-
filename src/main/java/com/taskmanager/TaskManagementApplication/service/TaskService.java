package com.taskmanager.TaskManagementApplication.service;

import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import com.taskmanager.TaskManagementApplication.dto.CreateTaskRequest;
import com.taskmanager.TaskManagementApplication.dto.UpdateTaskRequest;

import java.util.List;

public interface TaskService {
    Task createTask(CreateTaskRequest request);
    Task getTaskById(String id);
    Task updateTask(String id, UpdateTaskRequest request);
    void deleteTask(String id);
    List<Task> listTasks(TaskStatus status, int page, int size);
}
