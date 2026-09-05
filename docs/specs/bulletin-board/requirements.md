---
feature: bulletin-board
document_type: requirements
version: 1.0.0
status: draft
updated_at: 2026-09-05
---

# Requirements Specification: Bulletin Board Application

## 1. Context & Motivation

### 1.1 Problem Statement
To evaluate and validate the end-to-end capabilities of the `swe-workflow` plugin, an evaluation testbed application is required. The testbed must demonstrate full-stack software development workflows encompassing specification authoring, Design by Contract (DbC), automated test execution, and modern component implementation across Java/Micronaut, MySQL, and Angular.

### 1.2 Business & Technical Goals
- Deliver a reliable, functional Bulletin Board System (BBS) that allows users to post messages and view existing posts in reverse-chronological order.
- Provide a robust reference implementation to verify automated plugin tasks, stack verification, and strict Spec-Driven Development (SDD) processes.
- Ensure clean architectural separation between a containerized MySQL database, a Micronaut Data JDBC backend, and an Angular Material frontend.

### 1.3 Target Personas & Stakeholders
- **Plugin Evaluator (Developer / Tester)**: Evaluates the plugin capabilities by triggering automated tasks, verifying code quality, running test suites, and interacting with the BBS through a web browser.
- **BBS User (End User)**: Accesses the web interface to read past messages and submit new messages anonymously without an authentication barrier.

---

## 2. User Scenarios & Use Cases

### 2.1 Use Case 1: Browse Message List with Pagination
- **Actor**: BBS User / Plugin Evaluator
- **Preconditions**: The application backend and MySQL database are running and reachable.
- **Trigger**: The user accesses the web application or navigates page controls.
- **Basic Flow**:
  1. The user navigates to the bulletin board home page.
  2. The frontend sends an HTTP GET request to `/api/messages?page=0&size=50`.
  3. The backend retrieves the requested page of messages ordered by creation timestamp in descending order (`createdAt DESC`).
  4. The backend calculates pagination metadata (`page`, `size`, `totalElements`, `totalPages`).
  5. The backend returns HTTP status `200 OK` with the paginated payload.
  6. The frontend renders the messages from the top of the screen and displays pagination controls (e.g., current page, next/previous buttons).
- **Alternative Flows**:
  - *Empty State*: If no messages exist in the database, the backend returns an empty list with `totalElements: 0`, and the frontend displays an informational message indicating that no messages are present.
  - *Page Navigation*: When the user selects another page number via pagination controls, the frontend requests the selected page index and re-renders the list.
- **Exception Flows**:
  - *Server / Network Error*: If the backend or database is unreachable, the backend or network layer returns an error response, and the frontend displays a clear error banner.
- **Postconditions**: The requested page of messages is visible to the user in descending chronological order.

### 2.2 Use Case 2: Post a New Message
- **Actor**: BBS User / Plugin Evaluator
- **Preconditions**: The bulletin board home page is loaded, and the input form is displayed fixed at the bottom of the viewport.
- **Trigger**: The user fills in the form fields and clicks the "Submit" button.
- **Basic Flow**:
  1. The user enters Name, optional Email, Title, and Message body into the fixed bottom form.
  2. The user clicks the "Submit" button.
  3. The frontend validates the input fields locally.
  4. The frontend sends an HTTP POST request to `/api/messages` with the payload `{ name, email, title, message }`.
  5. The backend validates all field constraints (lengths, mandatory non-blank checks, valid email format).
  6. The backend assigns the current server timestamp (`createdAt`), persists the new message in the MySQL database, and returns HTTP status `201 Created` with the saved message.
  7. The frontend clears all form input fields.
  8. The frontend re-fetches the first page of messages (`page=0`) to display the new message at the very top.
- **Alternative Flows**:
  - *Omitted Email*: The user leaves the email field blank. The system processes the submission with `email` set to null and displays the posted message without an email link.
- **Exception Flows**:
  - *Client Validation Failure*: If any field violates constraints prior to submission, the frontend disables the submit button or displays inline error messages and halts transmission.
  - *Server Validation Failure*: If an invalid payload reaches the backend, the backend rejects the request with HTTP status `400 Bad Request` containing structured error details, and the frontend highlights the erroneous fields without wiping user input.
  - *Server / DB Fault*: If database persistence fails, the backend returns HTTP status `500 Internal Server Error`, and the frontend displays an error alert while preserving the user's input in the form.
- **Postconditions**: The new message is durably persisted in MySQL and visible at the top of the message list.

---

## 3. Visual Modeling

```mermaid
sequenceDiagram
    autonumber
    actor User as BBS User / Evaluator
    participant UI as Angular Frontend
    participant API as Micronaut Backend
    participant DB as MySQL Database

    %% Browse messages flow
    User->>UI: Open application
    activate UI
    UI->>API: GET /api/messages?page=0&size=50
    activate API
    API->>DB: SELECT * FROM messages ORDER BY created_at DESC LIMIT 50 OFFSET 0
    activate DB
    DB-->>API: Result set & count
    deactivate DB
    API-->>UI: 200 OK { content: [...], totalElements, totalPages, page, size }
    deactivate API
    UI-->>User: Render message list (top) & sticky input form (bottom)
    deactivate UI

    %% Post message flow
    User->>UI: Enter Name, Email, Title, Message & click Submit
    activate UI
    UI->>UI: Validate form fields
    alt Validation Failed
        UI-->>User: Display inline validation errors
    else Validation Passed
        UI->>API: POST /api/messages { name, email, title, message }
        activate API
        API->>API: Validate constraints (Bean Validation)
        alt Invalid Payload
            API-->>UI: 400 Bad Request { errors: [...] }
            deactivate API
            UI-->>User: Display validation error message
        else Valid Payload
            API->>DB: INSERT INTO messages (...) VALUES (...)
            activate DB
            DB-->>API: Success (Generated ID, Timestamp)
            deactivate DB
            API-->>UI: 201 Created { id, name, email, title, message, createdAt }
            deactivate API
            UI->>UI: Clear input form
            UI->>API: GET /api/messages?page=0&size=50 (Refresh list)
            activate API
            API->>DB: Query latest messages
            activate DB
            DB-->>API: Latest messages
            deactivate DB
            API-->>UI: 200 OK (Updated list)
            deactivate API
            UI-->>User: Render updated message list with new post at top
        end
    end
    deactivate UI
```

---

## 4. Functional Requirements

All functional requirements are defined using standard EARS patterns and uppercase RFC 2119 / RFC 8174 keywords.

| Requirement ID | EARS Pattern Type | Specification Statement (RFC 2119 / 8174) | Verification Method |
| :--- | :--- | :--- | :--- |
| **REQ-001** | Ubiquitous | The system MUST display a message submission input form permanently fixed to the bottom of the viewport (sticky footer) on the main bulletin board screen. | Automated E2E Test / UI Inspection |
| **REQ-002** | Ubiquitous | The system MUST display bulletin board messages in descending chronological order by creation timestamp (`createdAt DESC`) in the upper scrollable area above the fixed input form. | Automated Integration Test |
| **REQ-003** | Event-driven | When a user requests messages or navigates pages, the system MUST retrieve and return messages paginated with exactly 50 items per page by default (using zero-based page indexing), accompanied by pagination metadata containing `page`, `size`, `totalElements`, and `totalPages`. | Automated Integration Test |
| **REQ-004** | Event-driven | When a user submits a valid message containing a non-blank Name (1 to 50 characters), an optional Email (0 to 100 characters, RFC-compliant format if present), a non-blank Title (1 to 100 characters), and a non-blank Message body (1 to 1,000 characters), the system MUST record the message in the database with a server-assigned creation timestamp and return HTTP status `201 Created`. | Automated Integration Test |
| **REQ-005** | Event-driven | When a message is successfully submitted and saved, the system MUST clear all input fields in the fixed bottom form and reload the message list to display the newly submitted message at the top of the first page. | Automated E2E Test / Integration Test |
| **REQ-006** | Unwanted Behavior | If a user attempts to submit a message with missing required fields (blank Name, blank Title, or blank Message body) or fields exceeding maximum length constraints (Name > 50 characters, Email > 100 characters, Title > 100 characters, Message > 1,000 characters), then the system MUST reject the submission, return HTTP status `400 Bad Request` with field-level validation errors, and MUST NOT persist the record in the database. | Automated Unit / Integration Test |
| **REQ-007** | Unwanted Behavior | If a user provides an Email that does not conform to valid standard email address formatting (RFC 5322), then the system MUST reject the submission, return HTTP status `400 Bad Request`, and MUST NOT persist the record in the database. | Automated Unit / Integration Test |
| **REQ-008** | Optional Feature | Where a message record contains a non-empty Email address, the system MUST display the email address alongside the author's name in the message card; where the Email address is null or omitted, the system MUST display only the author's name without an empty email artifact. | Automated UI Component Test |
| **REQ-009** | Unwanted Behavior | If a database connection error or unhandled runtime fault occurs during message retrieval or persistence, then the system MUST return HTTP status `500 Internal Server Error`, log the internal error details securely, and MUST NOT expose internal stack traces or database connection credentials to the client. | Automated Fault Injection Test |

---

## 5. Non-Functional Requirements

- **NFR-PERF-001 (Performance)**: The backend API MUST process and respond to message query and creation requests within 500 milliseconds under standard local development execution conditions.
- **NFR-SEC-001 (Security & Sanitization)**: The system MUST sanitize or escape all user-supplied text inputs before rendering in the DOM to prevent Cross-Site Scripting (XSS), and the backend MUST use parameterized SQL statements via Micronaut Data JDBC to eliminate SQL Injection risks.
- **NFR-COMP-001 (Runtime & Dependency Baseline)**: The system MUST execute on Java 21 LTS, MySQL 8.4 LTS, Micronaut Framework 4.x, and Angular latest stable release with Angular Material.
- **NFR-REL-001 (Transaction & Reliability)**: The system MUST manage message creation within an atomic database transaction and fail safely (Fail-Closed) if any persistence step encounters an error.
- **NFR-DEV-001 (Containerization & Local Setup)**: The system MUST provide a `docker-compose.yml` file enabling one-command provisioning of the local MySQL database with pre-configured credentials and persistent volume storage.

---

## 6. Out of Scope

The following capabilities are explicitly excluded from this specification:
- User authentication and authorization (no user accounts, passwords, login sessions, or role-based access control).
- Message editing and message deletion operations.
- File or multimedia attachments (text-only submissions).
- Threaded conversation structures (no replies, tree threads, or quote replies).
- Social interactions (no likes, upvotes, downvotes, or emoji reactions).
