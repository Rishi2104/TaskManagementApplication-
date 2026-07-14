package com.taskmanager.TaskManagementApplication.service;

import com.taskmanager.TaskManagementApplication.domain.Task;
import com.taskmanager.TaskManagementApplication.domain.TaskStatus;
import com.taskmanager.TaskManagementApplication.dto.CreateTaskRequest;
import com.taskmanager.TaskManagementApplication.dto.UpdateTaskRequest;
import com.taskmanager.TaskManagementApplication.exception.TaskNotFoundException;
import com.taskmanager.TaskManagementApplication.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id("task-123")
                .title("Write unit tests")
                .description("TDD ftw")
                .status(TaskStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    void testCreateTask() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Write unit tests")
                .description("TDD ftw")
                .dueDate(LocalDate.now().plusDays(5))
                .build();

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Write unit tests");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTaskWithStatus() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("In progress task")
                .dueDate(LocalDate.now().plusDays(3))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(request);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void testGetTaskById() {
        when(taskRepository.findById("task-123")).thenReturn(Optional.of(sampleTask));

        Task result = taskService.getTaskById("task-123");

        assertThat(result).isEqualTo(sampleTask);
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById("bad-id"))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    @Test
    void testUpdateTask() {
        when(taskRepository.findById("task-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .status(TaskStatus.DONE)
                .build();

        Task updated = taskService.updateTask("task-123", request);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updated.getTitle()).isEqualTo("Write unit tests");
    }

    @Test
    void testUpdateTaskNotFound() {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask("missing", new UpdateTaskRequest()))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void testDeleteTask() {
        when(taskRepository.existsById("task-123")).thenReturn(true);

        taskService.deleteTask("task-123");

        verify(taskRepository, times(1)).deleteById("task-123");
    }

    @Test
    void testDeleteTaskNotFound() {
        when(taskRepository.existsById("ghost")).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask("ghost"))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void testListTasksSortedByDueDate() {
        Task t1 = Task.builder().id("1").title("A").status(TaskStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(10)).build();
        Task t2 = Task.builder().id("2").title("B").status(TaskStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(2)).build();
        Task t3 = Task.builder().id("3").title("C").status(TaskStatus.DONE)
                .dueDate(LocalDate.now().plusDays(5)).build();

        when(taskRepository.findAll()).thenReturn(List.of(t1, t2, t3));

        List<Task> result = taskService.listTasks(null, 0, 0);

        assertThat(result).extracting(Task::getId).containsExactly("2", "3", "1");
    }

    @Test
    void testListTasksFilteredByStatus() {
        Task pending = Task.builder().id("p1").title("Pending").status(TaskStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(3)).build();

        when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(List.of(pending));

        List<Task> result = taskService.listTasks(TaskStatus.PENDING, 0, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void testListTasksPaginated() {
        List<Task> allTasks = List.of(
                Task.builder().id("1").title("A").status(TaskStatus.PENDING).dueDate(LocalDate.now().plusDays(1)).build(),
                Task.builder().id("2").title("B").status(TaskStatus.PENDING).dueDate(LocalDate.now().plusDays(2)).build(),
                Task.builder().id("3").title("C").status(TaskStatus.PENDING).dueDate(LocalDate.now().plusDays(3)).build()
        );
        when(taskRepository.findAll()).thenReturn(allTasks);

        List<Task> page0 = taskService.listTasks(null, 0, 2);
        List<Task> page1 = taskService.listTasks(null, 1, 2);

        assertThat(page0).extracting(Task::getId).containsExactly("1", "2");
        assertThat(page1).extracting(Task::getId).containsExactly("3");
    }
}
