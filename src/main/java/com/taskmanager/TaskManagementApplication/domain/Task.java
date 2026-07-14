package com.taskmanager.TaskManagementApplication.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String title;
    private String description;

    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    private LocalDate dueDate;
}
