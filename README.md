# Task Management API

A RESTful Task Management backend built with **Java 17** and **Spring Boot 3**, following **Domain-Driven Design (DDD)** principles and developed using **Test-Driven Development (TDD)**.

---

## Features

- CRUD operations for tasks via REST API
- In-memory data store (no database required)
- Input validation with meaningful error messages
- Sorting tasks by `due_date` (ascending)
- **Bonus**: Filter tasks by `status` (PENDING, IN_PROGRESS, DONE)
- **Bonus**: Pagination support on `GET /tasks`
- **Bonus**: Future date validation for `due_date`
- Unit tests (service + controller layer with mocks)
- Integration tests (full Spring context, end-to-end)

---

## Project Structure

```
src/
└── main/java/com/taskmanager/TaskManagementApplication/
    ├── domain/
    │   ├── Task.java            ← Core domain entity
    │   └── TaskStatus.java      ← Enum: PENDING, IN_PROGRESS, DONE
    ├── dto/
    │   ├── CreateTaskRequest.java
    │   ├── UpdateTaskRequest.java
    │   └── TaskResponse.java
    ├── repository/
    │   ├── TaskRepository.java         ← Interface
    │   └── InMemoryTaskRepository.java ← ConcurrentHashMap implementation
    ├── service/
    │   ├── TaskService.java            ← Interface
    │   └── TaskServiceImpl.java        ← Business logic
    ├── controller/
    │   └── TaskController.java         ← REST endpoints
    ├── exception/
    │   ├── TaskNotFoundException.java
    │   └── GlobalExceptionHandler.java
    └── TaskManagementApplication.java  ← Spring Boot entry point

src/
└── test/java/com/taskmanager/TaskManagementApplication/
    ├── service/TaskServiceImplTest.java    ← Unit tests (mocked repo)
    ├── controller/TaskControllerTest.java  ← Unit tests (mocked service, MockMvc)
    └── TaskManagementIntegrationTest.java  ← Integration tests (full context)
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |

> **Note:** You do not need to install anything else. Spring Boot and all dependencies are downloaded automatically by Maven.

---

## How to Run

### 1. Clone / Extract the project

```bash
# If cloning from GitHub:
git clone <repo-url>
cd TaskManagementApplication
```

### 2. Build the project

```bash
./mvnw clean install
```

On Windows (Command Prompt):
```cmd
mvnw.cmd clean install
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

On Windows:
```cmd
mvnw.cmd spring-boot:run
```

The server starts on **http://localhost:8080**.

---

## Running Tests

```bash
./mvnw test
```

This runs both unit tests and integration tests.

---

## API Reference

### Base URL: `http://localhost:8080`

---

### Create Task
**POST** `/tasks`

**Request Body:**
```json
{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "status": "PENDING",
  "dueDate": "2027-01-15"
}
```
- `title` — **required**
- `dueDate` — **required**, must be a future date (format: `yyyy-MM-dd`)
- `description` — optional
- `status` — optional, defaults to `PENDING`

**Response: 201 Created**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "status": "PENDING",
  "dueDate": "2027-01-15"
}
```

---

### Get Task
**GET** `/tasks/{id}`

**Response: 200 OK** or **404 Not Found**

---

### Update Task
**PUT** `/tasks/{id}`

All fields are optional — only provided fields are updated.

```json
{
  "status": "IN_PROGRESS",
  "title": "Updated title"
}
```

**Response: 200 OK** or **404 Not Found**

---

### Delete Task
**DELETE** `/tasks/{id}`

**Response: 204 No Content** or **404 Not Found**

---

### List All Tasks
**GET** `/tasks`

| Query Param | Type | Description |
|-------------|------|-------------|
| `status` | `PENDING` \| `IN_PROGRESS` \| `DONE` | Filter by status |
| `page` | integer (default: 0) | Zero-based page index |
| `size` | integer (default: 0) | Page size (0 = no pagination) |

Tasks are always returned **sorted by `dueDate` ascending**.

**Examples:**
```
GET /tasks
GET /tasks?status=PENDING
GET /tasks?page=0&size=10
GET /tasks?status=IN_PROGRESS&page=0&size=5
```

**Response: 200 OK**
```json
[
  {
    "id": "...",
    "title": "...",
    "status": "PENDING",
    "dueDate": "2027-01-10"
  }
]
```

---

## Error Responses

All errors follow a consistent JSON format:

```json
{
  "timestamp": "2026-07-14T19:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: abc-123"
}
```

Validation errors (400) include per-field details:

```json
{
  "timestamp": "2026-07-14T19:30:00",
  "status": 400,
  "error": "Validation failed",
  "details": {
    "title": "Title is required",
    "dueDate": "Due date must be a future date"
  }
}
```

---

## Design Decisions

- **DDD layers**: Domain entity is framework-agnostic. DTOs handle API contracts. Service interface allows easy swapping of implementations.
- **In-memory store**: `ConcurrentHashMap` ensures thread safety under concurrent requests.
- **TDD**: Tests were written alongside code. Unit tests mock dependencies; integration tests use the real application context.
- **Pagination**: Zero-based page index, consistent with most REST APIs. Size 0 disables pagination.
