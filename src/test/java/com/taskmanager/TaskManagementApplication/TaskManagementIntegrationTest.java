package com.taskmanager.TaskManagementApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import com.taskmanager.TaskManagementApplication.dto.CreateTaskRequest;
import com.taskmanager.TaskManagementApplication.dto.UpdateTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createTaskAndGetId(String title, LocalDate dueDate) throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title(title)
                .description("Integration test task")
                .dueDate(dueDate)
                .build();

        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void testCreateTaskSuccess() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Integration Task")
                .description("Created via integration test")
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Integration Task"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(10).toString()));
    }

    @Test
    void testCreateTaskValidationFailureTitle() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .dueDate(LocalDate.now().plusDays(5))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTaskValidationFailureDueDateInPast() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Overdue task")
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTaskSuccess() throws Exception {
        String id = createTaskAndGetId("Fetch Me", LocalDate.now().plusDays(3));

        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Fetch Me"));
    }

    @Test
    void testGetTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("does-not-exist")));
    }

    @Test
    void testUpdateTaskSuccess() throws Exception {
        String id = createTaskAndGetId("Update Me", LocalDate.now().plusDays(7));

        UpdateTaskRequest update = UpdateTaskRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testUpdateTaskNotFound() throws Exception {
        UpdateTaskRequest update = UpdateTaskRequest.builder().status(TaskStatus.DONE).build();

        mockMvc.perform(put("/tasks/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTaskSuccess() throws Exception {
        String id = createTaskAndGetId("Delete Me", LocalDate.now().plusDays(2));

        mockMvc.perform(delete("/tasks/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        mockMvc.perform(delete("/tasks/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListTasksSortedByDueDate() throws Exception {
        createTaskAndGetId("Far task", LocalDate.now().plusDays(20));
        createTaskAndGetId("Near task", LocalDate.now().plusDays(2));
        createTaskAndGetId("Mid task", LocalDate.now().plusDays(10));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value("Near task"))
                .andExpect(jsonPath("$[1].title").value("Mid task"))
                .andExpect(jsonPath("$[2].title").value("Far task"));
    }

    @Test
    void testListTasksFilterByStatus() throws Exception {
        String id = createTaskAndGetId("Pending task", LocalDate.now().plusDays(5));

        UpdateTaskRequest update = UpdateTaskRequest.builder().status(TaskStatus.DONE).build();
        mockMvc.perform(put("/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)));

        createTaskAndGetId("Still pending", LocalDate.now().plusDays(8));

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Still pending"));
    }

    @Test
    void testListTasksPagination() throws Exception {
        createTaskAndGetId("Task 1", LocalDate.now().plusDays(1));
        createTaskAndGetId("Task 2", LocalDate.now().plusDays(2));
        createTaskAndGetId("Task 3", LocalDate.now().plusDays(3));

        mockMvc.perform(get("/tasks").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/tasks").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
