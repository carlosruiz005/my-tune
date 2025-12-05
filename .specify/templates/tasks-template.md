---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. Tests are OPTIONAL - only include them if explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.
  
  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/
  
  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment
  
  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

**CONSTITUTION COMPLIANCE**: This phase implements Code Quality First and Quality Gates principles

- [ ] T001 Create project structure per implementation plan (platform-specific: android/, ios/, webapp/)
- [ ] T002 Initialize platform projects with required dependencies and minimum SDK/OS versions
- [ ] T003 [P] Configure platform-specific linters (ktlint/detekt, SwiftLint, ESLint+TypeScript)
- [ ] T004 [P] Setup code formatters and pre-commit hooks
- [ ] T005 [P] Configure CI/CD pipeline with quality gates (tests, coverage, static analysis)
- [ ] T006 [P] Setup crash reporting and analytics (Firebase or equivalent)
- [ ] T007 [P] Initialize test frameworks (JUnit+Espresso, XCTest, Jest+Testing Library)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

**CONSTITUTION COMPLIANCE**: Implements Cross-Platform UX Consistency and Performance Standards

Examples of foundational tasks (adjust based on your project):

- [ ] T008 Setup shared design system tokens (colors, typography, spacing) accessible to all platforms
- [ ] T009 [P] Create platform-specific theme implementations (Material3, iOS native, web CSS-in-JS)
- [ ] T010 [P] Implement offline-first data layer with sync capabilities
- [ ] T011 Setup database schema and migrations framework
- [ ] T012 [P] Implement authentication/authorization framework with secure storage (Keychain/KeyStore)
- [ ] T013 [P] Setup API client with network monitoring and retry logic
- [ ] T014 Create base models/entities that all stories depend on
- [ ] T015 Configure error handling and user-friendly error messaging infrastructure
- [ ] T016 Setup localization framework with RTL support
- [ ] T017 [P] Implement accessibility base components with proper ARIA/semantic labels
- [ ] T018 Configure performance monitoring (app launch time, memory, frame rate tracking)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

**Platform Coverage**: [✓] Android [✓] iOS [✓] WebApp

### Tests for User Story 1 (MANDATORY per TDD principle) ⚠️

> **CONSTITUTION REQUIREMENT: Write these tests FIRST, get user approval, ensure they FAIL before implementation**
> Target Coverage: 80% unit, 70% integration, critical UI flows automated

- [ ] T019 [P] [US1] Write unit tests for business logic (shared module) - aim for 80% coverage
- [ ] T020 [P] [US1] Android: Write unit tests with JUnit/Robolectric in android/src/test/
- [ ] T021 [P] [US1] iOS: Write unit tests with XCTest in ios/Tests/
- [ ] T022 [P] [US1] WebApp: Write unit tests with Jest in webapp/src/__tests__/
- [ ] T023 [P] [US1] Contract test for [endpoint] - verify API contract compliance
- [ ] T024 [P] [US1] Integration test for data persistence and sync
- [ ] T025 [P] [US1] Android: Espresso UI test for critical user flow
- [ ] T026 [P] [US1] iOS: XCUITest for critical user flow
- [ ] T027 [P] [US1] WebApp: Playwright/Cypress E2E test for critical user flow
- [ ] T028 [P] [US1] Accessibility tests (screen reader, keyboard navigation, contrast)
- [ ] T029 [P] [US1] Performance tests (verify launch time, frame rate, memory constraints)
- [ ] T030 [US1] Verify all tests FAIL (red phase of TDD) - get stakeholder approval

### Implementation for User Story 1

> **CONSTITUTION REQUIREMENT: Tests MUST pass after implementation (green phase)**
> Follow Code Quality First: Document APIs, stay under complexity limit 10, justify dependencies

- [ ] T031 [P] [US1] Shared: Create [Entity1] model in shared/models/ with comprehensive documentation
- [ ] T032 [P] [US1] Shared: Implement [Service] business logic in shared/services/
- [ ] T033 [P] [US1] Android: Implement ViewModel and UI with Material3 components in android/src/main/
- [ ] T034 [P] [US1] iOS: Implement ViewModel and SwiftUI views following HIG in ios/Sources/
- [ ] T035 [P] [US1] WebApp: Implement React components with TypeScript in webapp/src/components/
- [ ] T036 [US1] Implement offline sync logic respecting platform lifecycle
- [ ] T037 [US1] Add user-friendly error handling with localized messages
- [ ] T038 [P] [US1] Add structured logging with appropriate privacy labels
- [ ] T039 [P] [US1] Implement accessibility features (labels, hints, touch targets >44dp/pt)
- [ ] T040 [US1] Verify all tests PASS (green phase of TDD)
- [ ] T041 [US1] Refactor if needed while keeping tests green
- [ ] T042 [US1] Run code coverage check (target: 80% unit, 70% integration)
- [ ] T043 [US1] Run static analysis (zero warnings/errors required)
- [ ] T044 [P] [US1] Android: Verify lint passes, APK size acceptable
- [ ] T045 [P] [US1] iOS: Verify SwiftLint passes, IPA size acceptable
- [ ] T046 [P] [US1] WebApp: Run Lighthouse audit (target score >90)
- [ ] T047 [US1] Performance validation: launch time, memory usage, frame rate
- [ ] T048 [US1] Manual QA verification across all platforms

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (OPTIONAL - only if tests requested) ⚠️

- [ ] T018 [P] [US2] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T019 [P] [US2] Integration test for [user journey] in tests/integration/test_[name].py

### Implementation for User Story 2

- [ ] T020 [P] [US2] Create [Entity] model in src/models/[entity].py
- [ ] T021 [US2] Implement [Service] in src/services/[service].py
- [ ] T022 [US2] Implement [endpoint/feature] in src/[location]/[file].py
- [ ] T023 [US2] Integrate with User Story 1 components (if needed)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (OPTIONAL - only if tests requested) ⚠️

- [ ] T024 [P] [US3] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T025 [P] [US3] Integration test for [user journey] in tests/integration/test_[name].py

### Implementation for User Story 3

- [ ] T026 [P] [US3] Create [Entity] model in src/models/[entity].py
- [ ] T027 [US3] Implement [Service] in src/services/[service].py
- [ ] T028 [US3] Implement [endpoint/feature] in src/[location]/[file].py

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation updates in docs/
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimization across all stories
- [ ] TXXX [P] Additional unit tests (if requested) in tests/unit/
- [ ] TXXX Security hardening
- [ ] TXXX Run quickstart.md validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"

# Launch all models for User Story 1 together:
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
