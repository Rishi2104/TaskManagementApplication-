package com.taskmanager.TaskManagementApplication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TaskStatus status;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
