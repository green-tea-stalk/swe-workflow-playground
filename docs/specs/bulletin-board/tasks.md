---
feature: bulletin-board
document_type: tasks
version: 1.0.0
status: approved
updated_at: 2026-09-11
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# Implementation Task Plan: Bulletin Board Application

## 1. Executive Stacked PR Overview

| PR # | Target Branch | Phase / Purpose | Key Components | Dependencies | Merge Order |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/bulletin-board-phase1-backend-persistence` | Backend Scaffolding, Flyway Migrations & Persistence | `COMP-003` | `docs/bulletin-board-spec` | 1 |
| **PR 2** | `feat/bulletin-board-phase2-backend-service-api` | Domain Business Logic & REST Controller (RFC 9457) | `COMP-001`, `COMP-002` | `PR 1` | 2 |
| **PR 3** | `feat/bulletin-board-phase3-frontend-feed` | Angular Scaffolding, Feed View & 50-Item Pagination | `COMP-004` | `PR 2` | 3 |
| **PR 4** | `feat/bulletin-board-phase4-frontend-form-e2e` | Bottom Fixed Form, Feed Wiring & E2E Verification | `COMP-005` | `PR 3` | 4 |

---

## 2. Mechanical Traceability Matrix

Every active requirement and design component is accounted for with zero gaps:

| Requirement ID | Component ID | Implementation Task | Target PR | Status |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-005` | `TASK-007` | PR 4 | Pending |
| **REQ-002** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-006` | PR 1, PR 2, PR 3 | Pending |
| **REQ-003** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-006` | PR 1, PR 2, PR 3 | Pending |
| **REQ-004** | `COMP-004` | `TASK-006` | PR 3 | Pending |
| **REQ-005** | `COMP-004` | `TASK-006` | PR 3 | Pending |
| **REQ-006** | `COMP-004` | `TASK-006` | PR 3 | Pending |
| **REQ-007** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-005` | `TASK-002`, `TASK-003`, `TASK-004`, `TASK-007` | PR 1, PR 2, PR 4 | Pending |
| **REQ-008** | `COMP-001`, `COMP-005` | `TASK-004`, `TASK-007` | PR 2, PR 4 | Pending |
| **REQ-009** | `COMP-001`, `COMP-005` | `TASK-004`, `TASK-007` | PR 2, PR 4 | Pending |
| **REQ-010** | `COMP-001`, `COMP-004`, `COMP-005` | `TASK-004`, `TASK-006`, `TASK-007`, `TASK-008` | PR 2, PR 3, PR 4 | Pending |

---

## 3. Stacked PR Task Specifications & Progress Tracker

Implementation agents execute tasks sequentially using the **Atomic Commit Loop**:
1. Pick the first unchecked task (`- [ ]`).
2. Implement code and passing unit/integration tests.
3. Mark task and criteria checkboxes as completed (`- [x]`).
4. Execute `committing-changes` to commit code and updated `tasks.md` atomically.
5. In case of unexpected interruption, resume immediately from the first unchecked task.

### PR 1: Backend Scaffolding, Flyway Migrations & Persistence
- **Branch**: `feat/bulletin-board-phase1-backend-persistence`
- **Merge Target**: `docs/bulletin-board-spec`

#### Tasks
- [x] **TASK-001**: Initialize Micronaut backend and Flyway migration
  - **Component & Requirements**: `COMP-003`, `REQ-007`
  - **Target Files**: `backend/build.gradle.kts`, `backend/src/main/resources/application.yml`, `backend/src/main/resources/db/migration/V1__create_posts_table.sql`
  - **Acceptance Criteria**:
    - [x] Micronaut 4.x project configured with Java 25 LTS, Gradle Kotlin DSL, and Micronaut Data JDBC dependencies.
    - [x] Flyway migration script creates `posts` table with `BIGINT AUTO_INCREMENT`, UTF-8 encoding, and `idx_posts_created_at_desc` index.
    - [x] Local and containerized MySQL database connection configurations verified.
  - **Commit Message**: `chore(backend): initialize Micronaut project and posts table migration`

- [ ] **TASK-002**: Implement PostEntity, DTO records, and PostRepository
  - **Component & Requirements**: `COMP-003`, `REQ-002`, `REQ-003`, `REQ-007`
  - **Target Files**: `backend/src/main/java/**/PostEntity.java`, `backend/src/main/java/**/dto/*.java`, `backend/src/main/java/**/PostRepository.java`, `backend/src/test/java/**/PostRepositoryTest.java`
  - **Acceptance Criteria**:
    - [ ] `PostEntity` mapped to `posts` table with generated ID and creation timestamp.
    - [ ] `CreatePostRequest`, `PostResponse`, and `PagedPostResponse` record DTOs defined with JSON Schema constraints.
    - [ ] `PagedPostResponse` enforces `minItems: 0 (guaranteed [] on empty)` for `items`.
    - [ ] `PostRepository` extends Micronaut Data `PageableRepository` with order descending by `created_at`.
    - [ ] Integration tests verify repository persistence and reverse-chronological pagination against Testcontainers MySQL.
  - **Commit Message**: `feat(backend): implement COMP-003 PostEntity DTOs and PostRepository`

---

### PR 2: Domain Business Logic & REST Controller (RFC 9457)
- **Branch**: `feat/bulletin-board-phase2-backend-service-api`
- **Merge Target**: `feat/bulletin-board-phase1-backend-persistence`

#### Tasks
- [ ] **TASK-003**: Implement PostService domain logic and validation
  - **Component & Requirements**: `COMP-002`, `REQ-002`, `REQ-003`, `REQ-007`
  - **Target Files**: `backend/src/main/java/**/PostService.java`, `backend/src/test/java/**/PostServiceTest.java`
  - **Acceptance Criteria**:
    - [ ] `PostService.getPagedPosts(page, size)` returns `PagedPostResponse` with empty list guarantee on 0 records.
    - [ ] `PostService.createPost(command)` validates non-blank strings, assigns UTC timestamp, and invokes repository save.
    - [ ] Unit tests verify preconditions, postconditions, and invariant contracts with 100% branch coverage.
  - **Commit Message**: `feat(backend): implement COMP-002 PostService domain logic`

- [ ] **TASK-004**: Implement PostController and RFC 9457 error handler
  - **Component & Requirements**: `COMP-001`, `REQ-002`, `REQ-003`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`
  - **Target Files**: `backend/src/main/java/**/PostController.java`, `backend/src/main/java/**/exception/*.java`, `backend/src/test/java/**/PostControllerTest.java`
  - **Acceptance Criteria**:
    - [ ] `GET /api/posts` returns `200 OK` with paginated posts (default: `page=0`, `size=50`).
    - [ ] `POST /api/posts` validates body via Bean Validation, returning `201 Created` with Location header.
    - [ ] Validation failures return `400 Bad Request` with `application/problem+json` RFC 9457 structure and `invalid_params`.
    - [ ] Micronaut HTTP client test verifies full controller interaction.
  - **Commit Message**: `feat(backend): implement COMP-001 PostController and RFC 9457 error handling`

---

### PR 3: Angular Scaffolding, Feed View & 50-Item Pagination
- **Branch**: `feat/bulletin-board-phase3-frontend-feed`
- **Merge Target**: `feat/bulletin-board-phase2-backend-service-api`

#### Tasks
- [ ] **TASK-005**: Initialize Angular project and API client service
  - **Component & Requirements**: `COMP-004`, `REQ-010`
  - **Target Files**: `frontend/package.json`, `frontend/angular.json`, `frontend/src/app/models/post.model.ts`, `frontend/src/app/services/post-api.service.ts`, `frontend/src/app/services/post-api.service.spec.ts`
  - **Acceptance Criteria**:
    - [ ] Angular project initialized with Angular Material installed and configured.
    - [ ] TypeScript interfaces matching `CreatePostRequest`, `PostResponse`, and `PagedPostResponse` defined.
    - [ ] `PostApiService` handles `getPosts(page, size)` and `createPost(payload)` with error propagation.
    - [ ] Unit tests for `PostApiService` with `HttpClientTestingModule` pass.
  - **Commit Message**: `chore(frontend): initialize Angular project and implement PostApiService`

- [ ] **TASK-006**: Implement PostFeedComponent with pagination and empty state
  - **Component & Requirements**: `COMP-004`, `REQ-002`, `REQ-003`, `REQ-004`, `REQ-005`, `REQ-006`, `REQ-010`
  - **Target Files**: `frontend/src/app/components/post-feed/post-feed.component.ts`, `frontend/src/app/components/post-feed/post-feed.component.html`, `frontend/src/app/components/post-feed/post-feed.component.scss`, `frontend/src/app/components/post-feed/post-feed.component.spec.ts`
  - **Acceptance Criteria**:
    - [ ] Feed renders posts in reverse chronological order displaying name, formatted timestamp, title, message, and optional public email.
    - [ ] Integrates `MatPaginator` configured for 50 items per page.
    - [ ] Displays placeholder message when total items is zero (`REQ-004`).
    - [ ] Scrollable feed container styled with dynamic calculation to avoid bottom form overlap.
    - [ ] Component unit tests verify rendering, empty state, and page navigation.
  - **Commit Message**: `feat(frontend): implement COMP-004 PostFeedComponent with pagination`

---

### PR 4: Bottom Fixed Form, Feed Wiring & E2E Verification
- **Branch**: `feat/bulletin-board-phase4-frontend-form-e2e`
- **Merge Target**: `feat/bulletin-board-phase3-frontend-feed`

#### Tasks
- [ ] **TASK-007**: Implement persistent bottom PostFormComponent and feed integration
  - **Component & Requirements**: `COMP-005`, `REQ-001`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`
  - **Target Files**: `frontend/src/app/components/post-form/post-form.component.ts`, `frontend/src/app/components/post-form/post-form.component.html`, `frontend/src/app/components/post-form/post-form.component.scss`, `frontend/src/app/components/post-form/post-form.component.spec.ts`
  - **Acceptance Criteria**:
    - [ ] Form container anchored to bottom of viewport (`position: fixed; bottom: 0; width: 100%`).
    - [ ] Reactive form validates name (1–50), title (1–100), message (1–4000), and optional email.
    - [ ] Field-level error messages displayed without clearing input on validation failure (`REQ-009`).
    - [ ] On successful submission: resets form, refreshes feed to page 0, and opens `MatSnackBar` notification (`REQ-008`).
    - [ ] Component unit tests verify form states, error indicators, and submission callback.
  - **Commit Message**: `feat(frontend): implement COMP-005 PostFormComponent with fixed bottom layout`

- [ ] **TASK-008**: Full-stack integration smoke verification
  - **Component & Requirements**: `COMP-001`, `COMP-004`, `COMP-005`, `REQ-010`
  - **Target Files**: `README.md`, integration smoke test script or workflow verification
  - **Acceptance Criteria**:
    - [ ] End-to-end user flow verified: open application -> view empty state -> post message -> observe success notification and feed refresh with 50-item pagination.
    - [ ] All automated unit and integration tests across backend and frontend pass cleanly.
  - **Commit Message**: `test(e2e): verify end-to-end bulletin board flow and update documentation`

---

## 4. Lifecycle & Reset Protocol

- **On Initial Creation**: All tasks are initialized as `- [ ]`.
- **On Specification Revision**:
  - If all tasks above are completed (`- [x]`), this plan is archived/reset and replaced with a clean list of new tasks for the revision diff.
  - If tasks are partially completed, active tasks are updated in place with realigned dependencies.
