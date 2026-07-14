package com.taskmanager.TaskManagementApplication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskStatus status;

    @Future(message = "Due date must be a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
