package com.taskmanager.TaskManagementApplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import com.taskmanager.TaskManagementApplication.dto.CreateTaskRequest;
import com.taskmanager.TaskManagementApplication.dto.UpdateTaskRequest;
import com.taskmanager.TaskManagementApplication.exception.GlobalExceptionHandler;
import com.taskmanager.TaskManagementApplication.exception.TaskNotFoundException;
import com.taskmanager.TaskManagementApplication.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TaskController.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private Task buildSampleTask() {
        return Task.builder()
                .id("abc-123")
                .title("Sample Task")
                .description("Some description")
                .status(TaskStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(7))
                .build();
    }

    @Test
    void testCreateTask() throws Exception {
        Task task = buildSampleTask();
        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(task);

        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Sample Task")
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc-123"))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testCreateTaskValidationFailureTitle() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .dueDate(LocalDate.now().plusDays(5))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    void testCreateTaskValidationFailureDueDate() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Some task")
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.dueDate").exists());
    }

    @Test
    void testGetTaskById() throws Exception {
        when(taskService.getTaskById("abc-123")).thenReturn(buildSampleTask());

        mockMvc.perform(get("/tasks/abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc-123"))
                .andExpect(jsonPath("$.title").value("Sample Task"));
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        when(taskService.getTaskById("missing")).thenThrow(new TaskNotFoundException("missing"));

        mockMvc.perform(get("/tasks/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: missing"));
    }

    @Test
    void testUpdateTask() throws Exception {
        Task updated = buildSampleTask();
        updated.setStatus(TaskStatus.DONE);
        when(taskService.updateTask(eq("abc-123"), any(UpdateTaskRequest.class))).thenReturn(updated);

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .status(TaskStatus.DONE)
                .build();

        mockMvc.perform(put("/tasks/abc-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void testUpdateTaskNotFound() throws Exception {
        when(taskService.updateTask(eq("ghost"), any())).thenThrow(new TaskNotFoundException("ghost"));

        mockMvc.perform(put("/tasks/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTaskRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask("abc-123");

        mockMvc.perform(delete("/tasks/abc-123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException("xyz")).when(taskService).deleteTask("xyz");

        mockMvc.perform(delete("/tasks/xyz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListTasks() throws Exception {
        when(taskService.listTasks(null, 0, 0)).thenReturn(List.of(buildSampleTask()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("abc-123"));
    }

    @Test
    void testListTasksFiltered() throws Exception {
        when(taskService.listTasks(eq(TaskStatus.PENDING), anyInt(), anyInt()))
                .thenReturn(List.of(buildSampleTask()));

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(taskService).listTasks(eq(TaskStatus.PENDING), anyInt(), anyInt());
    }
}
