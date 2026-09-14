---
feature: bulletin-board
document_type: tasks
version: 1.1.0
status: approved
updated_at: 2026-09-14
upstream:
  requirements: 1.1.0
  design: 1.1.0
---

# Implementation Task Plan: Bulletin Board Application (v1.1.0 Reply Functionality)

## 1. Executive Stacked PR Overview

| PR # | Target Branch | Phase / Purpose | Key Components | Dependencies | Merge Order |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/bulletin-board-v1.1-backend-persistence-domain` | Backend Reply Persistence & Domain Service Batch Fetching | `COMP-002`, `COMP-003`, `COMP-009` | `docs/bulletin-board-v1.1-spec` | 1 |
| **PR 2** | `feat/bulletin-board-v1.1-backend-reply-api` | Backend Reply REST Controller & RFC 9457 Exception Mapping | `COMP-001` | `PR 1` | 2 |
| **PR 3** | `feat/bulletin-board-v1.1-frontend-reply-ui` | Frontend Models, API Client, Feed Reply View & Form Reply Mode | `COMP-004`, `COMP-005` | `PR 2` | 3 |
| **PR 4** | `feat/bulletin-board-v1.1-e2e-and-i18n` | Multi-Locale Localization & End-to-End Playwright Verification | `COMP-001`, `COMP-004`, `COMP-005` | `PR 3` | 4 |

---

## 2. Mechanical Traceability Matrix

Every active requirement and design component is accounted for with zero gaps:

| Requirement ID | Component ID | Implementation Task | Target PR | Status |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-005` | `TASK-014`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-002** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-010`, `TASK-015` | PR 1, PR 4 | Pending |
| **REQ-003** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-004` | `TASK-010`, `TASK-015` | PR 1, PR 4 | Pending |
| **REQ-004** | `COMP-004` | `TASK-013`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-005** | `COMP-004` | `TASK-013`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-006** | `COMP-004` | `TASK-013`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-007** | `COMP-001`, `COMP-002`, `COMP-003`, `COMP-005` | `TASK-010`, `TASK-014`, `TASK-015` | PR 1, PR 3, PR 4 | Pending |
| **REQ-008** | `COMP-001`, `COMP-005` | `TASK-014`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-009** | `COMP-001`, `COMP-005` | `TASK-011`, `TASK-014`, `TASK-015` | PR 2, PR 3, PR 4 | Pending |
| **REQ-010** | `COMP-001`, `COMP-004`, `COMP-005` | `TASK-011`, `TASK-013`, `TASK-014`, `TASK-015` | PR 2, PR 3, PR 4 | Pending |
| **REQ-011** | `COMP-001`, `COMP-002`, `COMP-004`, `COMP-009` | `TASK-009`, `TASK-010`, `TASK-011`, `TASK-012`, `TASK-013`, `TASK-015` | PR 1, PR 2, PR 3, PR 4 | Pending |
| **REQ-012** | `COMP-004`, `COMP-005` | `TASK-013`, `TASK-014`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-013** | `COMP-005` | `TASK-014`, `TASK-015` | PR 3, PR 4 | Pending |
| **REQ-014** | `COMP-001`, `COMP-002`, `COMP-005`, `COMP-009` | `TASK-009`, `TASK-010`, `TASK-011`, `TASK-012`, `TASK-014`, `TASK-015` | PR 1, PR 2, PR 3, PR 4 | Pending |
| **REQ-015** | `COMP-001`, `COMP-002`, `COMP-005`, `COMP-009` | `TASK-009`, `TASK-010`, `TASK-011`, `TASK-012`, `TASK-014`, `TASK-015` | PR 1, PR 2, PR 3, PR 4 | Pending |

---

## 3. Stacked PR Task Specifications & Progress Tracker

Implementation agents execute tasks sequentially using the **Atomic Commit Loop**:
1. Pick the first unchecked task (`- [ ]`).
2. Implement code and passing unit/integration tests.
3. Mark task and criteria checkboxes as completed (`- [x]`).
4. Execute `committing-changes` to commit code and updated `tasks.md` atomically.
5. In case of unexpected interruption, resume immediately from the first unchecked task.

### PR 1: Backend Reply Persistence & Domain Service Batch Fetching
- **Branch**: `feat/bulletin-board-v1.1-backend-persistence-domain`
- **Merge Target**: `docs/bulletin-board-v1.1-spec`

#### Tasks
- [x] **TASK-009**: Implement Flyway V2 migration, ReplyEntity, and ReplyRepository
  - **Component & Requirements**: `COMP-009`, `REQ-011`, `REQ-014`, `REQ-015`
  - **Target Files**: `backend/src/main/resources/db/migration/V2__create_replies_table.sql`, `backend/src/main/java/**/entity/ReplyEntity.java`, `backend/src/main/java/**/repository/ReplyRepository.java`, `backend/src/test/java/**/ReplyRepositoryTest.java`
  - **Acceptance Criteria**:
    - [x] Flyway migration `V2__create_replies_table.sql` creates `replies` table with foreign key `ON DELETE CASCADE` to `posts(id)`, indices `idx_replies_post_id` and `idx_replies_created_at_asc`.
    - [x] `ReplyEntity` record mapped to `replies` table with generated ID and microsecond timestamp.
    - [x] `ReplyRepository` interface defined extending Micronaut Data JDBC with `findByPostIdOrderByCreatedAtAsc` and `findByPostIdInOrderByCreatedAtAsc`.
    - [x] Repository integration tests against MySQL Testcontainers verify persistence, cascade deletion, and chronological ordering.
  - **Commit Message**: `feat(backend): implement COMP-009 ReplyEntity and ReplyRepository with Flyway V2 migration`

- [x] **TASK-010**: Implement reply DTOs, PostNotFoundException, and PostService batch reply logic
  - **Component & Requirements**: `COMP-002`, `COMP-003`, `REQ-002`, `REQ-003`, `REQ-007`, `REQ-011`, `REQ-014`, `REQ-015`
  - **Target Files**: `backend/src/main/java/**/dto/CreateReplyRequest.java`, `backend/src/main/java/**/dto/ReplyResponse.java`, `backend/src/main/java/**/dto/PostResponse.java`, `backend/src/main/java/**/exception/PostNotFoundException.java`, `backend/src/main/java/**/service/PostService.java`, `backend/src/test/java/**/PostServiceTest.java`
  - **Acceptance Criteria**:
    - [x] `CreateReplyRequest` DTO defined with validation annotations (name 1-50 non-blank, email max 254 optional, message 1-4000 non-blank).
    - [x] `ReplyResponse` DTO defined with ISO 8601 UTC timestamp formatting.
    - [x] `PostResponse` updated with `replies: List<ReplyResponse>` guaranteeing non-null empty list `[]` on zero replies.
    - [x] `PostService.getPagedPosts(page, size)` performs single batch query `ReplyRepository.findByPostIdInOrderByCreatedAtAsc(postIds)` and maps replies in memory avoiding N+1 round trips.
    - [x] `PostService.createReply(postId, command)` checks parent post existence via `PostRepository.findById`, throws `PostNotFoundException` if missing, and persists valid reply with current UTC time.
    - [x] Unit tests verify preconditions, postconditions, empty collection guarantees, and 100% branch coverage.
  - **Commit Message**: `feat(backend): implement COMP-002 PostService reply batch fetching and creation logic`

---

### PR 2: Backend Reply REST Controller & RFC 9457 Exception Mapping
- **Branch**: `feat/bulletin-board-v1.1-backend-reply-api`
- **Merge Target**: `feat/bulletin-board-v1.1-backend-persistence-domain`

#### Tasks
- [x] **TASK-011**: Implement POST /api/posts/{postId}/replies and RFC 9457 error handler
  - **Component & Requirements**: `COMP-001`, `REQ-009`, `REQ-010`, `REQ-011`, `REQ-014`, `REQ-015`
  - **Target Files**: `backend/src/main/java/**/controller/PostController.java`, `backend/src/main/java/**/exception/GlobalExceptionHandler.java`, `backend/src/test/java/**/PostControllerTest.java`
  - **Acceptance Criteria**:
    - [x] `PostController` exposes `POST /api/posts/{postId}/replies` accepting `@Valid CreateReplyRequest` and returning `201 Created` with Location header `/api/posts/{postId}/replies/{id}`.
    - [x] Validation errors on `CreateReplyRequest` return `400 Bad Request` conforming to RFC 9457 with detailed `invalid_params`.
    - [x] `PostNotFoundException` mapped to RFC 9457 `404 Not Found` with problem details type `https://example.com/errors/post-not-found`.
    - [x] Integration tests verify successful reply creation (201), validation failure (400), and missing post error (404).
  - **Commit Message**: `feat(backend): implement COMP-001 reply endpoint and RFC 9457 404 error handler`

---

### PR 3: Frontend Models, API Client, Feed Reply View & Form Reply Mode
- **Branch**: `feat/bulletin-board-v1.1-frontend-reply-ui`
- **Merge Target**: `feat/bulletin-board-v1.1-backend-reply-api`

#### Tasks
- [ ] **TASK-012**: Update TypeScript models and PostApiService for replies
  - **Component & Requirements**: `COMP-004`, `COMP-005`, `REQ-011`, `REQ-014`, `REQ-015`
  - **Target Files**: `frontend/src/app/models/post.model.ts`, `frontend/src/app/services/post-api.service.ts`, `frontend/src/app/services/post-api.service.spec.ts`
  - **Acceptance Criteria**:
    - [ ] `CreateReplyRequest` and `ReplyResponse` interfaces defined in `post.model.ts`.
    - [ ] `PostResponse` updated with `readonly replies: readonly ReplyResponse[]` default non-null.
    - [ ] `PostApiService.createReply(postId: number, request: CreateReplyRequest)` implemented returning `Observable<ReplyResponse>`.
    - [ ] Unit tests for `PostApiService` covering `createReply` and error propagation pass.
  - **Commit Message**: `feat(frontend): add reply models and PostApiService.createReply method`

- [ ] **TASK-013**: Implement reply list view and reply trigger in PostFeedComponent
  - **Component & Requirements**: `COMP-004`, `REQ-004`, `REQ-005`, `REQ-006`, `REQ-010`, `REQ-011`, `REQ-012`
  - **Target Files**: `frontend/src/app/components/post-feed/post-feed.component.ts`, `frontend/src/app/components/post-feed/post-feed.component.html`, `frontend/src/app/components/post-feed/post-feed.component.scss`, `frontend/src/app/components/post-feed/post-feed.component.spec.ts`
  - **Acceptance Criteria**:
    - [ ] Each post card renders associated replies in ascending order of `created_at`, displaying contributor name, timestamp, message, and email.
    - [ ] Empty reply lists render cleanly without errors or unnecessary whitespace.
    - [ ] Dedicated reply action button on each post card emits reply target post to parent/service (`REQ-012`).
    - [ ] Component unit tests verify reply rendering, chronological order, empty replies safety, and reply button click event.
  - **Commit Message**: `feat(frontend): implement COMP-004 reply list rendering and reply action button`

- [ ] **TASK-014**: Implement Reply Mode and submission in PostFormComponent
  - **Component & Requirements**: `COMP-005`, `REQ-001`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`, `REQ-012`, `REQ-013`, `REQ-014`, `REQ-015`
  - **Target Files**: `frontend/src/app/components/post-form/post-form.component.ts`, `frontend/src/app/components/post-form/post-form.component.html`, `frontend/src/app/components/post-form/post-form.component.scss`, `frontend/src/app/components/post-form/post-form.component.spec.ts`, `frontend/src/app/app.ts`, `frontend/src/app/app.html`
  - **Acceptance Criteria**:
    - [ ] Component tracks `replyTarget` state; when set, displays target post identifier badge and cancel button (`REQ-012`, `REQ-013`).
    - [ ] In Reply Mode, title input is hidden and title validation constraints are deactivated (`REQ-013`).
    - [ ] Cancel button clears `replyTarget` and restores standard mode with title field (`REQ-013`).
    - [ ] On submit in Reply Mode: calls `createReply`, resets form, restores standard mode, refreshes feed, and shows success snackbar (`REQ-014`).
    - [ ] On error or validation failure, preserves entered form text without clearing (`REQ-009`, `REQ-015`).
    - [ ] Component unit tests verify Reply Mode transition, validation, cancel, submission, and error preservation.
  - **Commit Message**: `feat(frontend): implement COMP-005 PostFormComponent reply mode and submission wiring`

---

### PR 4: Multi-Locale Localization & End-to-End Playwright Verification
- **Branch**: `feat/bulletin-board-v1.1-e2e-and-i18n`
- **Merge Target**: `feat/bulletin-board-v1.1-frontend-reply-ui`

#### Tasks
- [ ] **TASK-015**: Implement multi-locale messages and full-stack E2E Playwright verification
  - **Component & Requirements**: `COMP-001`, `COMP-004`, `COMP-005`, `REQ-001` through `REQ-015`
  - **Target Files**: `frontend/src/locale/messages.xlf`, `frontend/src/locale/messages.ja.xlf`, `backend/src/main/resources/messages.properties`, `backend/src/main/resources/messages_ja.properties`, `frontend/e2e/bulletin-board.spec.ts`
  - **Acceptance Criteria**:
    - [ ] All new user-facing strings (reply button, reply mode badge, cancel action, success messages, validation messages) translated in English and Japanese.
    - [ ] Playwright E2E tests cover complete user scenarios: browse posts with replies, submit a new post, click reply, cancel reply mode, submit a valid reply, and verify reply renders at the end of the post's reply thread.
    - [ ] All backend test suites (`./gradlew test`) and frontend test suites (`npm test -- --watch=false`, `npm run e2e`) pass cleanly.
  - **Commit Message**: `test(e2e): add reply end-to-end Playwright tests and multi-locale translations`

---

## 4. Lifecycle & Reset Protocol

- **On Initial Creation**: All tasks are initialized as `- [ ]`.
- **On Specification Revision**:
  - If all tasks above are completed (`- [x]`), this plan is archived/reset and replaced with a clean list of new tasks for the revision diff.
  - If tasks are partially completed, active tasks are updated in place with realigned dependencies.
