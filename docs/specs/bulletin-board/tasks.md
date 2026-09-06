---
feature: bulletin-board
document_type: tasks
version: 1.0.0
status: draft
updated_at: 2026-09-05
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# Implementation Task Plan: Bulletin Board Application

## 1. Executive Stacked PR Overview

| PR # | Feature Branch | Target Branch | Phase / Purpose | Key Components | Dependencies | Merge Order |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/bulletin-board-phase1-backend-persistence` | `main` | Infrastructure & Persistence | `COMP-003` | None (`main`) | 1 |
| **PR 2** | `feat/bulletin-board-phase2-backend-api` | `feat/bulletin-board-phase1-backend-persistence` | Domain Logic & REST API Layer | `COMP-001`, `COMP-002` | PR 1 | 2 |
| **PR 3** | `feat/bulletin-board-phase3-frontend-ui` | `feat/bulletin-board-phase2-backend-api` | Frontend Angular UI & Integration | `COMP-004`, `COMP-005`, `COMP-006`, `COMP-007` | PR 2 | 3 |

---

## 2. Mechanical Traceability Matrix

Every active requirement from `requirements.md` and design component from `design.md` is accounted for with zero gaps:

| Requirement ID | Component ID | Implementation Task | Target PR | Status |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-004`, `COMP-005` | `TASK-009`, `TASK-010` | PR 3 | Pending |
| **REQ-002** | `COMP-002`, `COMP-003`, `COMP-004`, `COMP-006` | `TASK-003`, `TASK-004`, `TASK-008`, `TASK-010` | PR 1, PR 2, PR 3 | Pending |
| **REQ-003** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-006`, `COMP-007` | `TASK-003`, `TASK-004`, `TASK-006`, `TASK-007`, `TASK-008` | PR 1, PR 2, PR 3 | Pending |
| **REQ-004** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-005`, `COMP-007` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-006`, `TASK-007`, `TASK-009` | PR 1, PR 2, PR 3 | Pending |
| **REQ-005** | `COMP-004`, `COMP-005` | `TASK-009`, `TASK-010` | PR 3 | Pending |
| **REQ-006** | `COMP-001`, `COMP-005` | `TASK-005`, `TASK-006`, `TASK-009` | PR 2, PR 3 | Pending |
| **REQ-007** | `COMP-001`, `COMP-005` | `TASK-005`, `TASK-006`, `TASK-009` | PR 2, PR 3 | Pending |
| **REQ-008** | `COMP-006` | `TASK-008` | PR 3 | Pending |
| **REQ-009** | `COMP-001`, `COMP-002`, `COMP-007` | `TASK-004`, `TASK-005`, `TASK-007` | PR 2, PR 3 | Pending |

---

## 3. Stacked PR Task Specifications & Progress Tracker

Implementation agents execute tasks sequentially using the **Atomic Commit Loop**:
1. Pick the first unchecked task (`- [ ]`).
2. Implement code and passing unit/integration tests conforming to DbC contracts.
3. Mark task and criteria checkboxes as completed (`- [x]`).
4. Execute `committing-changes` to commit code and updated `tasks.md` atomically.
5. In case of unexpected interruption, resume execution immediately from the first unchecked task.

---

### PR 1: Infrastructure & Backend Core Persistence
- **Branch**: `feat/bulletin-board-phase1-backend-persistence`
- **Merge Target**: `main`

#### Tasks
- [x] **TASK-001**: Provision Docker Compose MySQL environment and Gradle Micronaut workspace
  - **Component & Requirements**: `COMP-003`, `NFR-DEV-001`, `NFR-COMP-001`
  - **Target Files**: `docker-compose.yml`, `backend/build.gradle`, `backend/settings.gradle`, `backend/gradlew`, `backend/src/main/resources/application.yml`
  - **Acceptance Criteria**:
    - [x] `docker-compose.yml` configures MySQL 8.4 LTS on port 3306 with health check and persistent volume.
    - [x] Backend Gradle project builds cleanly with Micronaut 4.x, Micronaut Data JDBC, MySQL Connector/J, and Flyway dependencies.
    - [x] Basic configuration loads without classpath errors.
  - **Commit Message**: `chore(infra): setup docker mysql and backend gradle workspace`

- [x] **TASK-002**: Implement Flyway schema migration and MessageEntity mapping
  - **Component & Requirements**: `COMP-003`, `REQ-004`, `NFR-REL-001`
  - **Target Files**: `backend/src/main/resources/db/migration/V1__create_messages_table.sql`, `backend/src/main/java/com/example/bulletinboard/entity/MessageEntity.java`, `backend/src/test/java/com/example/bulletinboard/entity/MessageEntityTest.java`
  - **Acceptance Criteria**:
    - [x] SQL DDL creates `messages` table with primary key `id` (BIGINT AUTO_INCREMENT), `name` (VARCHAR 50 NOT NULL), `email` (VARCHAR 100 NULL), `title` (VARCHAR 100 NOT NULL), `message` (VARCHAR 1000 NOT NULL), and `created_at` (TIMESTAMP NOT NULL) with descending index on `created_at`.
    - [x] `MessageEntity` mapped using Micronaut Data `@MappedEntity("messages")` with `@Id` and `@GeneratedValue`.
    - [x] Entity unit tests verify immutability, field getters, and equals/hashCode contracts.
  - **Commit Message**: `feat(backend): add flyway migration and message entity`

- [x] **TASK-003**: Implement MessageRepository with pagination query and integration tests
  - **Component & Requirements**: `COMP-003`, `REQ-002`, `REQ-003`, `REQ-004`
  - **Target Files**: `backend/src/main/java/com/example/bulletinboard/repository/MessageRepository.java`, `backend/src/test/java/com/example/bulletinboard/repository/MessageRepositoryTest.java`
  - **Acceptance Criteria**:
    - [x] `MessageRepository` extends `PageableRepository<MessageEntity, Long>` with `@JdbcRepository(dialect = Dialect.MYSQL)`.
    - [x] Preconditions and postconditions verified: `save` generates ID and timestamp; `findAll(Pageable)` returns records ordered by `created_at DESC`.
    - [x] Repository integration tests against MySQL (or test H2/Testcontainers) verify pagination and reverse-chronological ordering.
  - **Commit Message**: `feat(backend): implement COMP-003 message repository`

---

### PR 2: Domain Logic & REST API Layer
- **Branch**: `feat/bulletin-board-phase2-backend-api`
- **Merge Target**: `feat/bulletin-board-phase1-backend-persistence`

#### Tasks
- [x] **TASK-004**: Implement MessageService domain coordination and transactional logic
  - **Component & Requirements**: `COMP-002`, `REQ-002`, `REQ-003`, `REQ-004`, `REQ-009`, `NFR-REL-001`
  - **Target Files**: `backend/src/main/java/com/example/bulletinboard/service/MessageService.java`, `backend/src/main/java/com/example/bulletinboard/dto/MessageCreateRequest.java`, `backend/src/main/java/com/example/bulletinboard/dto/MessageResponse.java`, `backend/src/main/java/com/example/bulletinboard/dto/PageResponse.java`, `backend/src/test/java/com/example/bulletinboard/service/MessageServiceTest.java`
  - **Acceptance Criteria**:
    - [x] DTO records defined: `MessageCreateRequest`, `MessageResponse`, `PageResponse<T>`.
    - [x] `createMessage` executes within transactional boundary, trims inputs, maps to entity, and guarantees postcondition response.
    - [x] `findMessages` returns `PageResponse<MessageResponse>` ordered by creation timestamp descending.
    - [x] Unit tests achieve 100% branch coverage for happy path and database error propagation.
  - **Commit Message**: `feat(backend): implement COMP-002 message service and DTOs`

- [x] **TASK-005**: Implement RFC 9457 problem details handlers and exception hierarchy
  - **Component & Requirements**: `COMP-001`, `REQ-006`, `REQ-007`, `REQ-009`
  - **Target Files**: `backend/src/main/java/com/example/bulletinboard/exception/BulletinBoardException.java`, `backend/src/main/java/com/example/bulletinboard/exception/ProblemDetails.java`, `backend/src/main/java/com/example/bulletinboard/exception/ValidationExceptionHandler.java`, `backend/src/main/java/com/example/bulletinboard/exception/GlobalExceptionHandler.java`, `backend/src/test/java/com/example/bulletinboard/exception/ExceptionHandlerTest.java`
  - **Acceptance Criteria**:
    - [x] `ProblemDetails` model conforms to RFC 9457 (`type`, `title`, `status`, `detail`, `instance`, `invalid_params`).
    - [x] `ValidationExceptionHandler` transforms Micronaut `ConstraintViolationException` into HTTP 400 Problem Details with structured field errors.
    - [x] `GlobalExceptionHandler` logs root causes and returns HTTP 500 Problem Details without leaking stack traces.
    - [x] Unit tests verify JSON error payloads across validation failures and unexpected exceptions.
  - **Commit Message**: `feat(backend): add RFC 9457 problem details exception handlers`

- [x] **TASK-006**: Implement MessageController REST endpoints and CORS configuration
  - **Component & Requirements**: `COMP-001`, `REQ-003`, `REQ-004`, `REQ-006`, `REQ-007`, `NFR-PERF-001`, `NFR-SEC-001`
  - **Target Files**: `backend/src/main/java/com/example/bulletinboard/controller/MessageController.java`, `backend/src/test/java/com/example/bulletinboard/controller/MessageControllerTest.java`
  - **Acceptance Criteria**:
    - [x] `GET /api/messages` handles `page` (default: 0) and `size` (default: 50), returning `200 OK` with `PageResponse`.
    - [x] `POST /api/messages` validates request body via `@Valid`, returning `201 Created` with `MessageResponse`.
    - [x] CORS enabled for `http://localhost:4200` with permitted methods (`GET`, `POST`, `OPTIONS`).
    - [x] Controller integration tests verify HTTP status codes, request validation rejection, and successful response serialization.
  - **Commit Message**: `feat(backend): implement COMP-001 message controller and endpoints`

---

### PR 3: Frontend Angular UI & Integration
- **Branch**: `feat/bulletin-board-phase3-frontend-ui`
- **Merge Target**: `feat/bulletin-board-phase2-backend-api`

#### Tasks
- [x] **TASK-007**: Initialize Angular workspace, Angular Material theme, and MessageApiClient
  - **Component & Requirements**: `COMP-007`, `REQ-003`, `REQ-004`, `REQ-009`, `NFR-COMP-001`
  - **Target Files**: `frontend/package.json`, `frontend/angular.json`, `frontend/src/app/models/message.model.ts`, `frontend/src/app/services/message-api.service.ts`, `frontend/src/app/services/message-api.service.spec.ts`
  - **Acceptance Criteria**:
    - [x] Angular project initialized with Standalone Components and Angular Material components (`MatCard`, `MatButton`, `MatFormField`, `MatInput`, `MatPaginator`).
    - [x] TypeScript interfaces defined for `MessageCreateRequest`, `MessageResponse`, `PageResponse<T>`, and `ProblemDetails`.
    - [x] `MessageApiClient` (COMP-007) implements `getMessages` and `postMessage` using `HttpClient`.
    - [x] Service unit tests with `HttpTestingController` verify query parameters, payloads, and error transformation.
  - **Commit Message**: `feat(frontend): initialize angular workspace and COMP-007 api client`

- [x] **TASK-008**: Implement MessageListComponent with feed display, paginator, and email formatting
  - **Component & Requirements**: `COMP-006`, `REQ-002`, `REQ-003`, `REQ-008`, `NFR-SEC-001`
  - **Target Files**: `frontend/src/app/components/message-list/message-list.component.ts`, `frontend/src/app/components/message-list/message-list.component.html`, `frontend/src/app/components/message-list/message-list.component.scss`, `frontend/src/app/components/message-list/message-list.component.spec.ts`
  - **Acceptance Criteria**:
    - [x] Component renders messages in exact order received via `@Input() messages` signal.
    - [x] Author email rendered as `mailto:` link if present; cleanly omitted if null/empty without empty artifacts.
    - [x] `MatPaginator` displays `totalElements`, `pageSize = 50`, and emits zero-based page change events upstream.
    - [x] Component unit tests verify feed rendering, email presence/absence, and pagination event emission.
  - **Commit Message**: `feat(frontend): implement COMP-006 message list and paginator`

- [x] **TASK-009**: Implement MessageFormComponent with sticky footer layout and reactive validation
  - **Component & Requirements**: `COMP-005`, `REQ-001`, `REQ-004`, `REQ-006`, `REQ-007`
  - **Target Files**: `frontend/src/app/components/message-form/message-form.component.ts`, `frontend/src/app/components/message-form/message-form.component.html`, `frontend/src/app/components/message-form/message-form.component.scss`, `frontend/src/app/components/message-form/message-form.component.spec.ts`
  - **Acceptance Criteria**:
    - [x] Reactive form enforces constraints: Name (required, 1-50 chars), Email (optional, email pattern, max 100), Title (required, 1-100 chars), Message (required, 1-1000 chars).
    - [x] Form prevents submission if invalid; displays field-specific error hints.
    - [x] Component CSS applies sticky/fixed footer styling (`position: sticky`, `bottom: 0`, `z-index: 100`).
    - [x] Component unit tests verify validation rules, submit emission, and `resetForm()` behavior.
  - **Commit Message**: `feat(frontend): implement COMP-005 sticky message form`

- [ ] **TASK-010**: Implement BulletinBoardComponent root container coordinating state, auto-refresh, and independent scrolling
  - **Component & Requirements**: `COMP-004`, `REQ-001`, `REQ-002`, `REQ-005`
  - **Target Files**: `frontend/src/app/components/bulletin-board/bulletin-board.component.ts`, `frontend/src/app/components/bulletin-board/bulletin-board.component.html`, `frontend/src/app/components/bulletin-board/bulletin-board.component.scss`, `frontend/src/app/components/bulletin-board/bulletin-board.component.spec.ts`, `frontend/src/app/app.component.ts`
  - **Acceptance Criteria**:
    - [ ] Root container orchestrates `MessageListComponent` in an upper scrollable area (`overflow-y: auto`) and `MessageFormComponent` fixed at the bottom.
    - [ ] On initialization, automatically loads first page (`page=0`).
    - [ ] On successful message submission, resets the form and reloads the first page to surface the new message at the top.
    - [ ] Component integration tests verify end-to-end component interaction, reload trigger, and error banner display.
  - **Commit Message**: `feat(frontend): implement COMP-004 bulletin board root container`

---

## 4. Lifecycle & Reset Protocol

- **On Initial Creation**: All tasks are initialized as `- [ ]`.
- **On Specification Revision**:
  - If all tasks above are completed (`- [x]`), this plan is archived/reset and replaced with a clean list of new tasks for the revision diff.
  - If tasks are partially completed, active tasks are updated in place with realigned dependencies.

