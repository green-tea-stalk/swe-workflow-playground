---
feature: ci-and-repo-automation
document_type: tasks
version: 1.0.0
status: approved
updated_at: 2026-09-13
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# Implementation Task Plan: CI & Repository Automation

## 1. Executive Stacked PR Overview

| PR # | Target Branch | Phase / Purpose | Key Components | Dependencies | Merge Order |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/ci-phase1-dev-orchestration-and-governance` | Developer Tooling, Governance & Badges | `COMP-004`, `COMP-005` | `docs/ci-and-repo-automation-spec` | 1 |
| **PR 2** | `feat/ci-phase2-repo-automations` | Multi-Ecosystem Dependabot & Semantic Release | `COMP-002`, `COMP-003` | `PR 1` | 2 |
| **PR 3** | `feat/ci-phase3-ci-pipeline` | Full CI Quality Gate (Parallel Backend/Frontend + Gated E2E) | `COMP-001` | `PR 2` | 3 |

---

## 2. Mechanical Traceability Matrix

Every requirement (`REQ-001` through `REQ-011`) and design component (`COMP-001` through `COMP-005`) is accounted for with zero gaps:

| Requirement ID | Component ID | Implementation Task | Target PR | Status |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-001` | `TASK-006` | PR 3 | Pending |
| **REQ-002** | `COMP-001` | `TASK-007` | PR 3 | Pending |
| **REQ-003** | `COMP-001` | `TASK-007` | PR 3 | Pending |
| **REQ-004** | `COMP-002` | `TASK-004` | PR 2 | Pending |
| **REQ-005** | `COMP-003` | `TASK-005` | PR 2 | Pending |
| **REQ-006** | `COMP-003` | `TASK-005` | PR 2 | Pending |
| **REQ-007** | `COMP-004` | `TASK-001` | PR 1 | Pending |
| **REQ-008** | `COMP-004` | `TASK-001` | PR 1 | Pending |
| **REQ-009** | `COMP-004` | `TASK-002` | PR 1 | Pending |
| **REQ-010** | `COMP-005` | `TASK-003` | PR 1 | Pending |
| **REQ-011** | `COMP-005` | `TASK-003` | PR 1 | Pending |

---

## 3. Stacked PR Task Specifications & Progress Tracker

Implementation agents execute tasks sequentially using the **Atomic Commit Loop**:
1. Pick the first unchecked task (`- [ ]`).
2. Implement code and passing unit/integration tests or configuration verifications.
3. Mark task and criteria checkboxes as completed (`- [x]`).
4. Execute `committing-changes` to commit code and updated `tasks.md` atomically.
5. In case of unexpected interruption, resume immediately from the first unchecked task.

### PR 1: Developer Tooling, Governance & Badges
- **Branch**: `feat/ci-phase1-dev-orchestration-and-governance`
- **Merge Target**: `docs/ci-and-repo-automation-spec`

#### Tasks
- [x] **TASK-001**: Configure root npm package and development scripts
  - **Component & Requirements**: `COMP-004`, `REQ-007`, `REQ-008`
  - **Target Files**: `package.json`, `package-lock.json`
  - **Acceptance Criteria**:
    - [x] Root `package.json` created with `private: true` and name `"swe-workflow-playground"`.
    - [x] `concurrently` and `wait-on` installed as devDependencies.
    - [x] `npm run dev` script orchestrates `docker compose up -d` for MySQL, then concurrently runs backend (`./gradlew run`) and frontend (`npm run start:ja`).
    - [x] `npm run dev:ja` and `npm run dev:en` scripts provided for explicit locale target development.
    - [x] Database helper scripts (`db:up`, `db:down`) and root test aggregation scripts (`test`, `test:backend`, `test:frontend`, `test:e2e`) configured.
  - **Commit Message**: `feat(app): configure root npm workspace and consolidated dev scripts`

- [x] **TASK-002**: Implement process lifecycle, error handling, and termination traps
  - **Component & Requirements**: `COMP-004`, `REQ-009`
  - **Target Files**: `package.json`
  - **Acceptance Criteria**:
    - [x] `concurrently` configured with `--kill-others` (`-k`) to ensure termination of all sibling processes on error or exit.
    - [x] Interleaved output formatting configured with colored, identifiable process prefixes (`[backend]` and `[frontend]`).
    - [x] Verification test confirms sending `SIGINT` cleanly halts Java and Node child processes without orphaned process leaks.
  - **Commit Message**: `feat(app): configure clean process lifecycle and signal trapping`

- [x] **TASK-003**: Establish canonical MIT license, documentation badges, and developer guide updates
  - **Component & Requirements**: `COMP-005`, `REQ-010`, `REQ-011`
  - **Target Files**: `LICENSE`, `README.md`, `README.ja.md`, `AGENTS.md`
  - **Acceptance Criteria**:
    - [x] Canonical `LICENSE` file created at repository root containing standard MIT license text (2026).
    - [x] Badges configured in exact required order in `README.md` and `README.ja.md`: (1) Latest Release, (2) CI Status, (3) release-please Status, (4) Dependabot Status, (5) MIT License.
    - [x] `AGENTS.md` Quick Start section updated with `npm run dev` workflow while strictly preserving single-source technical guidelines and unidirectional reference integrity.
    - [x] Automated markdown link check confirms zero broken links or anchor drift across documentation files.
  - **Commit Message**: `docs(app): add MIT license, status badges, and consolidated quickstart instructions`

---

### PR 2: Multi-Ecosystem Dependabot & Semantic Release
- **Branch**: `feat/ci-phase2-repo-automations`
- **Merge Target**: `feat/ci-phase1-dev-orchestration-and-governance`

#### Tasks
- [x] **TASK-004**: Configure multi-ecosystem Dependabot automation
  - **Component & Requirements**: `COMP-002`, `REQ-004`
  - **Target Files**: `.github/dependabot.yml`
  - **Acceptance Criteria**:
    - [x] `.github/dependabot.yml` created conforming strictly to schema version 2.
    - [x] Exactly four package ecosystems configured: `gradle` (`/backend`), `npm` (`/frontend`), `github-actions` (`/`), and `docker` (`/`).
    - [x] All four ecosystems synchronized to weekly Monday schedule (`interval: "weekly"`, `day: "monday"`).
    - [x] Dependabot configuration syntax validated via schema linter.
  - **Commit Message**: `ci(dependabot): configure multi-ecosystem weekly dependency automation`

- [x] **TASK-005**: Configure semantic release-please workflow and manifests
  - **Component & Requirements**: `COMP-003`, `REQ-005`, `REQ-006`
  - **Target Files**: `.github/workflows/release-please.yml`, `.github/release-please-config.json`, `.release-please-manifest.json`
  - **Acceptance Criteria**:
    - [x] `.github/workflows/release-please.yml` configured to trigger on push to `main` with `contents: write` and `pull-requests: write` permissions.
    - [x] `.github/release-please-config.json` configured with `release-type: "simple"` for root package `"."`.
    - [x] `.release-please-manifest.json` initialized with current repository version milestone (`"0.1.0"`).
    - [x] Workflow YAML and configuration JSON schemas verified via static linting.
  - **Commit Message**: `ci(release): configure google release-please semantic versioning and changelog automation`

---

### PR 3: Full CI Quality Gate (Parallel Backend/Frontend + Gated E2E)
- **Branch**: `feat/ci-phase3-ci-pipeline`
- **Merge Target**: `feat/ci-phase2-repo-automations`

#### Tasks
- [ ] **TASK-006**: Implement concurrent backend and frontend CI verification jobs
  - **Component & Requirements**: `COMP-001`, `REQ-001`
  - **Target Files**: `.github/workflows/ci.yml`
  - **Acceptance Criteria**:
    - [ ] `.github/workflows/ci.yml` configured to trigger on `push` to `main` and `pull_request` targeting `main`.
    - [ ] Concurrency group configured with `cancel-in-progress: true` to prevent redundant runner execution.
    - [ ] `backend` job runs on `ubuntu-latest`, sets up Java 25 LTS (Corretto) with Gradle cache, and executes `./gradlew test` (including Testcontainers MySQL integration).
    - [ ] `frontend` job runs on `ubuntu-latest`, sets up Node.js 22 LTS with npm cache, executes `npm ci`, runs `npm test -- --watch=false` (Vitest), and executes `npm run build`.
    - [ ] Backend and frontend jobs execute concurrently without inter-job blocking.
  - **Commit Message**: `ci(workflow): implement parallel backend and frontend verification jobs`

- [ ] **TASK-007**: Implement conditional downstream E2E gate and artifact upload
  - **Component & Requirements**: `COMP-001`, `REQ-002`, `REQ-003`
  - **Target Files**: `.github/workflows/ci.yml`
  - **Acceptance Criteria**:
    - [ ] `e2e` job configured with explicit dependency `needs: [backend, frontend]`.
    - [ ] E2E job starts MySQL container via `docker compose up -d`, starts backend and frontend services in the background, and polls health check endpoints with timeout.
    - [ ] Playwright Chromium browser installed via `npx playwright install --with-deps chromium` and executes `npx playwright test`.
    - [ ] If either `backend` or `frontend` job fails, `e2e` job is skipped and zero runner resources are consumed for browser tests.
    - [ ] On test failure, Playwright traces, failure screenshots, and service logs are uploaded as GitHub Actions artifacts via `actions/upload-artifact@v4`.
    - [ ] Workflow YAML validated via actionlint or equivalent workflow validation.
  - **Commit Message**: `ci(workflow): add conditional gated e2e verification and diagnostic artifact upload`

---

## 4. Lifecycle & Reset Protocol

- **On Initial Creation**: All tasks are initialized as `- [ ]`.
- **On Specification Revision**:
  - If all tasks above are completed (`- [x]`), this plan is archived/reset and replaced with a clean list of new tasks for the revision diff.
  - If tasks are partially completed, active tasks are updated in place with realigned dependencies.
