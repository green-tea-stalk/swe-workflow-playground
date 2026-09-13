---
feature: ci-and-repo-automation
document_type: tasks
version: 1.1.0
status: approved
updated_at: 2026-09-13
upstream:
  requirements: 1.1.0
  design: 1.1.0
---

# Implementation Task Plan: CI & Repository Automation

## 1. Executive Stacked PR Overview

| PR # | Target Branch | Phase / Purpose | Key Components | Dependencies | Merge Order |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/ci-phase1-dev-orchestration-and-governance` | Developer Tooling, Governance & Badges | `COMP-004`, `COMP-005` | `docs/ci-and-repo-automation-spec` | Completed (v1.0.0) |
| **PR 2** | `feat/ci-phase2-repo-automations` | Multi-Ecosystem Dependabot & Semantic Release | `COMP-002`, `COMP-003` | `PR 1` | Completed (v1.0.0) |
| **PR 3** | `feat/ci-phase3-ci-pipeline` | Full CI Quality Gate (Parallel Backend/Frontend + Gated E2E) | `COMP-001` | `PR 2` | Completed (v1.0.0) |
| **PR 4** | `feat/ci-phase4-governance-and-version-catalog` | Code Ownership Governance & Gradle Version Catalog | `COMP-006`, `COMP-008` | `main` | 1 (Active) |
| **PR 5** | `feat/ci-phase5-formatters-and-cleanup` | Backend Spotless & Frontend Prettier with In-Place Cleanup | `COMP-006`, `COMP-007` | `PR 4` | 2 (Active) |
| **PR 6** | `feat/ci-phase6-ci-format-gates` | CI Formatting Quality Gates & Developer Guide Updates | `COMP-001`, `COMP-005` | `PR 5` | 3 (Active) |

---

## 2. Mechanical Traceability Matrix

Every requirement (`REQ-001` through `REQ-016`) and design component (`COMP-001` through `COMP-008`) is accounted for with zero gaps:

| Requirement ID | Component ID | Implementation Task | Target PR | Status |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-001` | `TASK-006`, `TASK-012` | PR 3, PR 6 | Completed (PR 3) / Pending (PR 6) |
| **REQ-002** | `COMP-001` | `TASK-007` | PR 3 | Completed |
| **REQ-003** | `COMP-001` | `TASK-007` | PR 3 | Completed |
| **REQ-004** | `COMP-002` | `TASK-004` | PR 2 | Completed |
| **REQ-005** | `COMP-003` | `TASK-005` | PR 2 | Completed |
| **REQ-006** | `COMP-003` | `TASK-005` | PR 2 | Completed |
| **REQ-007** | `COMP-004` | `TASK-001` | PR 1 | Completed |
| **REQ-008** | `COMP-004` | `TASK-001` | PR 1 | Completed |
| **REQ-009** | `COMP-004` | `TASK-002` | PR 1 | Completed |
| **REQ-010** | `COMP-005` | `TASK-003`, `TASK-013` | PR 1, PR 6 | Completed (PR 1) / Pending (PR 6) |
| **REQ-011** | `COMP-005` | `TASK-003` | PR 1 | Completed |
| **REQ-012** | `COMP-006` | `TASK-010` | PR 5 | Pending |
| **REQ-013** | `COMP-007` | `TASK-011` | PR 5 | Pending |
| **REQ-014** | `COMP-008` | `TASK-008` | PR 4 | Completed |
| **REQ-015** | `COMP-006` | `TASK-009` | PR 4 | Completed |
| **REQ-016** | `COMP-001`, `COMP-006`, `COMP-007` | `TASK-010`, `TASK-011`, `TASK-012` | PR 5, PR 6 | Pending |

---

## 3. Stacked PR Task Specifications & Progress Tracker

Implementation agents execute tasks sequentially using the **Atomic Commit Loop**:
1. Pick the first unchecked task (`- [ ]`).
2. Implement code and passing unit/integration tests or configuration verifications.
3. Mark task and criteria checkboxes as completed (`- [x]`).
4. Execute `committing-changes` to commit code and updated `tasks.md` atomically.
5. In case of unexpected interruption, resume immediately from the first unchecked task.

---

### Archived Milestone Tasks (Version 1.0.0 - Completed)

<details>
<summary>Click to view completed v1.0.0 tasks (TASK-001 through TASK-007)</summary>

#### PR 1: Developer Tooling, Governance & Badges (Completed)
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

#### PR 2: Multi-Ecosystem Dependabot & Semantic Release (Completed)
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

#### PR 3: Full CI Quality Gate (Completed)
- [x] **TASK-006**: Implement concurrent backend and frontend CI verification jobs
  - **Component & Requirements**: `COMP-001`, `REQ-001`
  - **Target Files**: `.github/workflows/ci.yml`
  - **Acceptance Criteria**:
    - [x] `.github/workflows/ci.yml` configured to trigger on `push` to `main` and `pull_request` targeting `main`.
    - [x] Concurrency group configured with `cancel-in-progress: true` to prevent redundant runner execution.
    - [x] `backend` job runs on `ubuntu-latest`, sets up Java 25 LTS (Corretto) with Gradle cache, and executes `./gradlew test`.
    - [x] `frontend` job runs on `ubuntu-latest`, sets up Node.js 22 LTS with npm cache, executes `npm ci`, runs `npm test -- --watch=false`, and executes `npm run build`.
    - [x] Backend and frontend jobs execute concurrently without inter-job blocking.
  - **Commit Message**: `ci(workflow): implement parallel backend and frontend verification jobs`

- [x] **TASK-007**: Implement conditional downstream E2E gate and artifact upload
  - **Component & Requirements**: `COMP-001`, `REQ-002`, `REQ-003`
  - **Target Files**: `.github/workflows/ci.yml`
  - **Acceptance Criteria**:
    - [x] `e2e` job configured with explicit dependency `needs: [backend, frontend]`.
    - [x] E2E job starts MySQL container via `docker compose up -d`, starts backend and frontend services in the background, and polls health check endpoints with timeout.
    - [x] Playwright Chromium browser installed via `npx playwright install --with-deps chromium` and executes `npx playwright test`.
    - [x] If either `backend` or `frontend` job fails, `e2e` job is skipped and zero runner resources are consumed for browser tests.
    - [x] On test failure, Playwright traces, failure screenshots, and service logs are uploaded as GitHub Actions artifacts via `actions/upload-artifact@v4`.
  - **Commit Message**: `ci(workflow): add conditional gated e2e verification and diagnostic artifact upload`

</details>

---

### Active Milestone Tasks (Version 1.1.0)

### PR 4: Code Ownership Governance & Gradle Version Catalog
- **Branch**: `feat/ci-phase4-governance-and-version-catalog`
- **Merge Target**: `main`

#### Tasks
- [x] **TASK-008**: Establish repository-wide CODEOWNERS configuration
  - **Component & Requirements**: `COMP-008`, `REQ-014`
  - **Target Files**: `.github/CODEOWNERS`
  - **Acceptance Criteria**:
    - [x] `.github/CODEOWNERS` file created at repository root.
    - [x] Wildcard rule `* @green-tea-stalk` configured to assign default review ownership across all files.
    - [x] Verification check confirms file syntax and GitHub username matching.
  - **Commit Message**: `chore(governance): add repository-wide CODEOWNERS assigning @green-tea-stalk`

- [x] **TASK-009**: Implement Gradle Version Catalog for backend dependencies and plugins
  - **Component & Requirements**: `COMP-006`, `REQ-015`
  - **Target Files**: `backend/gradle/libs.versions.toml`, `backend/build.gradle.kts`
  - **Acceptance Criteria**:
    - [x] `backend/gradle/libs.versions.toml` created with `[versions]`, `[libraries]`, and `[plugins]` sections.
    - [x] All backend plugins (`io.micronaut.application`, `com.gradleup.shadow`, `io.micronaut.aot`) and dependencies cataloged.
    - [x] `backend/build.gradle.kts` refactored to use type-safe `libs.plugins...` and `libs...` accessors with zero hardcoded dependency version strings.
    - [x] Platform environment versions (Java 25 LTS, MySQL 8.4 LTS) remain strictly outside the catalog.
    - [x] Execution of `./gradlew buildEnvironment` and `./gradlew test` succeeds without resolution errors.
  - **Commit Message**: `build(backend): introduce gradle version catalog and migrate build dependencies`

---

### PR 5: Backend Spotless & Frontend Prettier with In-Place Formatting
- **Branch**: `feat/ci-phase5-formatters-and-cleanup`
- **Merge Target**: `feat/ci-phase4-governance-and-version-catalog`

#### Tasks
- [ ] **TASK-010**: Configure Spotless code formatting for Java (Palantir) and Kotlin DSL (ktlint)
  - **Component & Requirements**: `COMP-006`, `REQ-012`, `REQ-016`
  - **Target Files**: `backend/gradle/libs.versions.toml`, `backend/build.gradle.kts`, `backend/src/**/*.java`
  - **Acceptance Criteria**:
    - [ ] Spotless Gradle plugin coordinate added to `backend/gradle/libs.versions.toml` and applied in `backend/build.gradle.kts`.
    - [ ] Spotless configured with `palantirJavaFormat()` for Java sources (`src/**/*.java`, 4 spaces) and `ktlint()` for Kotlin Gradle scripts (`*.gradle.kts`).
    - [ ] Initial formatting executed via `./gradlew spotlessApply` to format all existing Java files and Gradle scripts in-place.
    - [ ] Execution of `./gradlew spotlessCheck` passes with exit code `0`.
    - [ ] Intentionally introducing a style violation causes `./gradlew spotlessCheck` to fail with exit code `1`.
  - **Commit Message**: `style(backend): configure spotless with palantir java format and format codebase`

- [ ] **TASK-011**: Configure Prettier formatting and lifecycle scripts for frontend
  - **Component & Requirements**: `COMP-007`, `REQ-013`, `REQ-016`
  - **Target Files**: `frontend/.prettierrc`, `frontend/.prettierignore`, `frontend/package.json`, `frontend/src/**/*`
  - **Acceptance Criteria**:
    - [ ] `frontend/.prettierrc` created with `tabWidth: 2`, `singleQuote: true`, `semi: true`, `trailingComma: "all"`, `printWidth: 100`.
    - [ ] `frontend/.prettierignore` created ignoring `dist/`, `.angular/`, `node_modules/`, `coverage/`.
    - [ ] `format:check` (`prettier --check .`) and `format` (`prettier --write .`) added to `frontend/package.json` scripts.
    - [ ] Initial formatting executed via `npm run format` across all TypeScript, HTML, SCSS, and JSON files in `frontend/`.
    - [ ] Execution of `npm run format:check` passes with exit code `0`.
    - [ ] Intentionally introducing a formatting discrepancy causes `npm run format:check` to fail with exit code `1`.
  - **Commit Message**: `style(frontend): configure prettier rules and apply in-place formatting`

---

### PR 6: CI Formatting Quality Gates & Developer Guide Updates
- **Branch**: `feat/ci-phase6-ci-format-gates`
- **Merge Target**: `feat/ci-phase5-formatters-and-cleanup`

#### Tasks
- [ ] **TASK-012**: Integrate fail-fast formatting quality gates into CI workflow
  - **Component & Requirements**: `COMP-001`, `REQ-001`, `REQ-016`
  - **Target Files**: `.github/workflows/ci.yml`
  - **Acceptance Criteria**:
    - [ ] In `backend` job, `./gradlew spotlessCheck` step added prior to executing `./gradlew test`.
    - [ ] In `frontend` job, `npm run format:check` step added after `npm ci` and prior to unit tests and build.
    - [ ] On formatting failure in either job, the job immediately terminates with non-zero exit code and bypasses the `e2e` job.
    - [ ] Workflow YAML validated via schema checks and passes syntax linting.
  - **Commit Message**: `ci(workflow): add backend spotless and frontend prettier verification steps`

- [ ] **TASK-013**: Update technical developer documentation and AGENTS.md single source of truth
  - **Component & Requirements**: `COMP-005`, `REQ-010`
  - **Target Files**: `AGENTS.md`, `README.md`, `README.ja.md`
  - **Acceptance Criteria**:
    - [ ] `AGENTS.md` Section 5 updated with formatting check and fix commands (`./gradlew spotlessCheck`, `./gradlew spotlessApply`, `npm run format:check`, `npm run format`).
    - [ ] `AGENTS.md` updated to document the Version Catalog architecture and CODEOWNERS review routing while maintaining single-source policy and unidirectional reference integrity.
    - [ ] All cross-references and anchors verified with zero broken links.
  - **Commit Message**: `docs(app): document formatting commands and version catalog architecture in AGENTS.md`

---

## 4. Lifecycle & Reset Protocol

- **On Initial Creation**: All tasks are initialized as `- [ ]`.
- **On Specification Revision**:
  - Previously completed tasks (`TASK-001` through `TASK-007`) are archived in the completed milestone section.
  - Active tasks for revision `1.1.0` (`TASK-008` through `TASK-013`) are tracked with GFM checkboxes and executed via the Atomic Commit Loop.
