package com.taskmanager.TaskManagementApplication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .build();
    }
}
