---
feature: i18n-support
document_type: tasks
version: 1.0.0
status: approved
updated_at: 2026-09-13
upstream:
  requirements: 1.0.0
  design: 1.0.0
---

# Implementation Task Plan: Internationalization Support (i18n)

This document establishes the implementation task plan, Stacked PR boundaries, acceptance criteria, and mechanical traceability matrix for delivering bilingual internationalization (`ja` and `en`) across the bulletin board application.

---

## 1. Executive Stacked PR Overview

| PR # | Target Branch | Phase / Purpose | Key Components | Dependencies | Merge Order |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR 1** | `feat/i18n-phase1-backend-localization` | Backend Message Localization & RFC 9457 Content Negotiation | `COMP-201`, `COMP-202`, `COMP-203`, `COMP-204` | `docs/i18n-support-spec` | 1 |
| **PR 2** | `feat/i18n-phase2-frontend-foundation` | Frontend i18n Tooling, Locale Service & Navigation | `COMP-101`, `COMP-102`, `COMP-105` | `feat/i18n-phase1-backend-localization` | 2 |
| **PR 3** | `feat/i18n-phase3-views-and-e2e` | Localized UI Views, XLIFF Bundles & Playwright E2E Suites | `COMP-103`, `COMP-104` | `feat/i18n-phase2-frontend-foundation` | 3 |

---

## 2. Mechanical Traceability Matrix

Every active requirement and design component is accounted for with zero gaps:

| Requirement ID | Component ID | Implementation Task | Target PR | Status |
| :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | `COMP-101`, `COMP-103`, `COMP-104`, `COMP-202` | `TASK-001`, `TASK-004`, `TASK-006`, `TASK-008`, `TASK-009`, `TASK-010`, `TASK-011` | PR 1, PR 2, PR 3 | Pending |
| **REQ-002** | `COMP-102`, `COMP-201` | `TASK-002`, `TASK-004`, `TASK-005`, `TASK-011` | PR 1, PR 2, PR 3 | Pending |
| **REQ-003** | `COMP-101` | `TASK-006`, `TASK-011` | PR 2, PR 3 | Pending |
| **REQ-004** | `COMP-102` | `TASK-005`, `TASK-011` | PR 2, PR 3 | Pending |
| **REQ-005** | `COMP-102` | `TASK-005`, `TASK-011` | PR 2, PR 3 | Pending |
| **REQ-006** | `COMP-102`, `COMP-103` | `TASK-005`, `TASK-008`, `TASK-011` | PR 2, PR 3 | Pending |
| **REQ-007** | `COMP-103` | `TASK-008`, `TASK-011` | PR 3 | Pending |
| **REQ-008** | `COMP-104`, `COMP-203`, `COMP-204` | `TASK-003`, `TASK-009`, `TASK-011` | PR 1, PR 3 | Pending |
| **REQ-009** | `COMP-105`, `COMP-201`, `COMP-202`, `COMP-203`, `COMP-204` | `TASK-001`, `TASK-002`, `TASK-003`, `TASK-007`, `TASK-011` | PR 1, PR 2, PR 3 | Pending |
| **REQ-010** | `COMP-201`, `COMP-202`, `COMP-203`, `COMP-204` | `TASK-001`, `TASK-002`, `TASK-003`, `TASK-011` | PR 1, PR 3 | Pending |
| **REQ-011** | `COMP-103`, `COMP-104` | `TASK-008`, `TASK-009`, `TASK-011` | PR 3 | Pending |

---

## 3. Stacked PR Task Specifications & Progress Tracker

Implementation agents execute tasks sequentially using the **Atomic Commit Loop**:
1. Pick the first unchecked task (`- [ ]`).
2. Implement code and passing unit/integration tests following strict TDD.
3. Mark task and criteria checkboxes as completed (`- [x]`).
4. Execute `committing-changes` to commit code and updated `tasks.md` atomically.
5. In case of unexpected interruption, resume immediately from the first unchecked task.

---

### PR 1: Backend Message Localization & RFC 9457 Content Negotiation
- **Branch**: `feat/i18n-phase1-backend-localization`
- **Merge Target**: `docs/i18n-support-spec`

#### Tasks
- [x] **TASK-001**: Implement `MessageLocalizationService` and create resource bundles
  - **Component & Requirements**: `COMP-202`, `REQ-001`, `REQ-009`, `REQ-010`
  - **Target Files**:
    - `backend/src/main/resources/messages.properties`
    - `backend/src/main/resources/messages_ja.properties`
    - `backend/src/main/java/com/example/bulletinboard/service/MessageLocalizationService.java`
    - `backend/src/main/java/com/example/bulletinboard/service/DefaultMessageLocalizationService.java`
    - `backend/src/test/java/com/example/bulletinboard/service/MessageLocalizationServiceTest.java`
  - **Acceptance Criteria**:
    - [x] `messages.properties` (English) and `messages_ja.properties` (Japanese) define identical key hierarchies matching design Section 3.3.
    - [x] `MessageLocalizationService` resolves messages by code and `Locale` with fallback to English.
    - [x] Missing keys safely return default message without throwing unhandled exceptions.
    - [x] Unit tests verify interpolation, Japanese lookup, and English fallback.
  - **Commit Message**: `feat(backend): implement MessageLocalizationService and resource bundles`

- [x] **TASK-002**: Implement `LocaleResolver` for HTTP `Accept-Language` header parsing
  - **Component & Requirements**: `COMP-201`, `REQ-002`, `REQ-009`, `REQ-010`
  - **Target Files**:
    - `backend/src/main/java/com/example/bulletinboard/util/LocaleResolver.java`
    - `backend/src/main/java/com/example/bulletinboard/util/HttpLocaleResolver.java`
    - `backend/src/test/java/com/example/bulletinboard/util/LocaleResolverTest.java`
  - **Acceptance Criteria**:
    - [x] Resolves `ja` and `ja-*` tags to `Locale.JAPANESE`.
    - [x] Resolves `en` and `en-*` tags to `Locale.ENGLISH`.
    - [x] Absent, empty, wildcard, or unsupported language tags resolve to default `Locale.ENGLISH`.
    - [x] Unit tests verify parameterized header parsing (`q`-values, case insensitivity, empty strings).
  - **Commit Message**: `feat(backend): implement HttpLocaleResolver for Accept-Language negotiation`

- [x] **TASK-003**: Localize RFC 9457 exception handlers and validation envelopes
  - **Component & Requirements**: `COMP-203`, `COMP-204`, `REQ-008`, `REQ-009`, `REQ-010`
  - **Target Files**:
    - `backend/src/main/java/com/example/bulletinboard/exception/ValidationExceptionHandler.java`
    - `backend/src/main/java/com/example/bulletinboard/exception/GlobalExceptionHandler.java`
    - `backend/src/main/java/com/example/bulletinboard/exception/IllegalArgumentExceptionHandler.java`
    - `backend/src/test/java/com/example/bulletinboard/controller/PostControllerTest.java`
    - `backend/src/test/java/com/example/bulletinboard/exception/ExceptionHandlerTest.java`
  - **Acceptance Criteria**:
    - [x] `ValidationExceptionHandler` produces Problem Details with localized `title`, `detail`, and `invalid_params[].reason` matching client locale.
    - [x] `GlobalExceptionHandler` and `IllegalArgumentExceptionHandler` produce localized envelopes.
    - [x] Integration tests verify `POST /api/posts` returns Japanese Problem Details when `Accept-Language: ja` is sent, and English when `Accept-Language: en` or missing.
    - [x] `invalid_params` is guaranteed non-null empty array `[]` when zero violations exist.
  - **Commit Message**: `feat(backend): localize RFC 9457 exception handlers with Accept-Language support`

---

### PR 2: Frontend Localization Foundation & Locale Management
- **Branch**: `feat/i18n-phase2-frontend-foundation`
- **Merge Target**: `feat/i18n-phase1-backend-localization`

#### Tasks
- [x] **TASK-004**: Configure `@angular/localize` and multi-locale build architecture
  - **Component & Requirements**: `COMP-101`, `COMP-102`, `REQ-001`, `REQ-002`
  - **Target Files**:
    - `frontend/package.json`
    - `frontend/angular.json`
    - `frontend/src/locale/messages.ja.xlf`
  - **Acceptance Criteria**:
    - [x] `@angular/localize` added to project dependencies.
    - [x] `angular.json` configured with `i18n` sourceLocale `en` and locale `ja` pointing to `src/locale/messages.ja.xlf`.
    - [x] Build targets configure base Hrefs (`/en/` and `/ja/`).
    - [x] Production build succeeds generating multi-locale output distributions.
  - **Commit Message**: `build(frontend): configure @angular/localize and multi-locale build targets`

- [x] **TASK-005**: Implement `LocaleService` for storage, discovery, and navigation
  - **Component & Requirements**: `COMP-102`, `REQ-002`, `REQ-004`, `REQ-005`, `REQ-006`
  - **Target Files**:
    - `frontend/src/app/services/locale.service.ts`
    - `frontend/src/app/services/locale.service.spec.ts`
  - **Acceptance Criteria**:
    - [x] `getActiveLocale()` resolves active locale from document base Href or location path.
    - [x] `getStoredLocale()` reads and validates `bb_locale` from `localStorage`.
    - [x] `resolveInitialLocale()` evaluates `localStorage`, falling back to `navigator.language.startsWith('ja') ? 'ja' : 'en'`.
    - [x] `setLocale(target)` stores preference in `localStorage` and navigates location to `'/' + target + '/'`.
    - [x] Vitest unit tests achieve 100% branch coverage with mocked storage and window location.
  - **Commit Message**: `feat(frontend): implement LocaleService for discovery, persistence, and navigation`

- [x] **TASK-006**: Implement `LanguageSwitchComponent` in toolbar
  - **Component & Requirements**: `COMP-101`, `REQ-001`, `REQ-003`
  - **Target Files**:
    - `frontend/src/app/components/language-switch/language-switch.component.ts`
    - `frontend/src/app/components/language-switch/language-switch.component.html`
    - `frontend/src/app/components/language-switch/language-switch.component.scss`
    - `frontend/src/app/components/language-switch/language-switch.component.spec.ts`
    - `frontend/src/app/app.ts`
    - `frontend/src/app/app.html`
    - `frontend/src/app/app.spec.ts`
  - **Acceptance Criteria**:
    - [x] Renders language toggle button in toolbar displaying current locale (`EN` or `JA`).
    - [x] Activating switch invokes `LocaleService.setLocale()` with target locale.
    - [x] Button includes localized accessible label (`aria-label`).
    - [x] Unit tests verify click interactions and service delegation.
  - **Commit Message**: `feat(frontend): implement LanguageSwitchComponent in navigation toolbar`

- [x] **TASK-007**: Inject `Accept-Language` header in `PostApiService`
  - **Component & Requirements**: `COMP-105`, `REQ-009`
  - **Target Files**:
    - `frontend/src/app/services/post-api.service.ts`
    - `frontend/src/app/services/post-api.service.spec.ts`
  - **Acceptance Criteria**:
    - [x] All HTTP GET and POST requests attach `Accept-Language: {locale}` matching `LocaleService.getActiveLocale()`.
    - [x] Vitest unit tests verify `HttpTestingController` inspects outgoing request headers for both `'en'` and `'ja'`.
  - **Commit Message**: `feat(frontend): inject Accept-Language header in PostApiService calls`

---

### PR 3: Frontend View Localization & Playwright E2E Verification
- **Branch**: `feat/i18n-phase3-views-and-e2e`
- **Merge Target**: `feat/i18n-phase2-frontend-foundation`

#### Tasks
- [x] **TASK-008**: Localize `PostFeedComponent` and configure locale-aware date formatting
  - **Component & Requirements**: `COMP-103`, `REQ-001`, `REQ-006`, `REQ-007`, `REQ-011`
  - **Target Files**:
    - `frontend/src/app/components/post-feed/post-feed.component.html`
    - `frontend/src/app/components/post-feed/post-feed.component.ts`
    - `frontend/src/app/components/post-feed/post-feed.component.spec.ts`
  - **Acceptance Criteria**:
    - [x] Feed template text (empty placeholder, error banner, retry button, paginator labels) annotated with `i18n` attributes.
    - [x] Post creation timestamp formatted with `DatePipe` using `yyyy/MM/dd HH:mm:ss` under `ja` and `MMM d, y, h:mm:ss a` under `en`.
    - [x] User post content (`name`, `title`, `message`) preserved in original form without translation.
    - [x] Unit tests verify feed rendering under both locales.
  - **Commit Message**: `feat(frontend): localize PostFeedComponent and configure DatePipe locale formatting`

- [x] **TASK-009**: Localize `PostFormComponent` labels, placeholders, errors, and toast messages
  - **Component & Requirements**: `COMP-104`, `REQ-001`, `REQ-002`, `REQ-006`, `REQ-007`
  - **Target Files**:
    - `frontend/src/app/components/post-form/post-form.component.html`
    - `frontend/src/app/components/post-form/post-form.component.ts`
    - `frontend/src/app/components/post-form/post-form.component.spec.ts`
  - **Acceptance Criteria**:
    - [x] Form labels, input placeholders, inline validation errors, and submit button annotated with `i18n` attributes.
    - [x] Snackbar notifications for success and failure use localized text.
    - [x] Localized RFC 9457 `detail` displayed in error snackbar.
    - [x] Unit tests verify form interactions and validation errors in English and Japanese.
  - **Commit Message**: `feat(frontend): localize PostFormComponent inputs, errors, and snackbars`

- [ ] **TASK-010**: Extract and populate XLIFF translation bundles
  - **Component & Requirements**: `COMP-101`, `COMP-103`, `COMP-104`, `REQ-001`
  - **Target Files**:
    - `frontend/src/locale/messages.ja.xlf`
    - `frontend/src/locale/messages.xlf`
  - **Acceptance Criteria**:
    - [ ] Run `ng extract-i18n` to generate source translation catalog.
    - [ ] Complete Japanese translations populated in `messages.ja.xlf` covering all template `i18n` IDs.
    - [ ] Production build (`npm run build`) cleanly succeeds for both `en` and `ja` distributions without missing translation warnings.
  - **Commit Message**: `i18n(frontend): populate Japanese XLIFF translation catalog`

- [ ] **TASK-011**: Build Playwright E2E automated test suite for bilingual verification
  - **Component & Requirements**: `COMP-101`, `COMP-102`, `COMP-103`, `COMP-104`, `COMP-105`, `REQ-001` through `REQ-011`
  - **Target Files**:
    - `frontend/e2e/i18n-language-switch.spec.ts`
  - **Acceptance Criteria**:
    - [ ] Scenario 1: Initial access verifies language detection (browser language matching).
    - [ ] Scenario 2: Toolbar language toggle switches UI, changes timestamps format, and persists `bb_locale` in `localStorage`.
    - [ ] Scenario 3: Form validation errors and submission feedback display in the active language.
    - [ ] Scenario 4: User-contributed posts are displayed unaltered in both locales.
    - [ ] All Playwright E2E tests pass cleanly in headless Chromium.
  - **Commit Message**: `test(e2e): add Playwright automated test suite for bilingual i18n workflows`

---

## 4. Lifecycle & Reset Protocol

- **On Initial Creation**: All tasks are initialized as `- [ ]`.
- **On Specification Revision**:
  - If all tasks above are completed (`- [x]`), this plan is archived/reset and replaced with a clean list of new tasks for the revision diff.
  - If tasks are partially completed, active tasks are updated in place with realigned dependencies.
