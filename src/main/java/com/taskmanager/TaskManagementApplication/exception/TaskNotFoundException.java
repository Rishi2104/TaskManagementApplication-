package com.taskmanager.TaskManagementApplication.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String id) {
        super("Task not found with id: " + id);
    }
}
