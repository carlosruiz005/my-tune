<!--
SYNC IMPACT REPORT
==================
Version Change: Initial → 1.0.0
Change Type: MAJOR (Initial constitution establishment)

Principles Defined:
  1. Code Quality First - Enforces consistent, maintainable code across all platforms
  2. Test-Driven Development (NON-NEGOTIABLE) - Mandatory TDD with platform-specific test coverage requirements
  3. Cross-Platform UX Consistency - Ensures uniform user experience while respecting platform conventions
  4. Performance Standards (NON-NEGOTIABLE) - Strict performance requirements for mobile platforms

Sections Added:
  - Platform-Specific Requirements (Android, iOS, WebApp constraints)
  - Quality Gates (automated checks before deployment)
  - Governance (amendment procedures and compliance validation)

Templates Status:
  ✅ plan-template.md - Constitution Check section aligns with all four principles
  ✅ spec-template.md - User scenarios support cross-platform testing requirements
  ✅ tasks-template.md - Task structure supports platform-specific and shared tasks

Follow-up Actions:
  - README.md not found - consider creating project documentation
  - No runtime guidance files detected - all principles self-contained
  - Command templates not found in expected location - no updates required

Date: 2025-12-05
-->

# My Tune Constitution

## Core Principles

### I. Code Quality First

All code MUST meet the following non-negotiable quality standards across all platforms (Android, iOS, WebApp):

- **Static Analysis**: Code MUST pass platform-specific linters without warnings
  - Android: ktlint/detekt for Kotlin, lint for Android resources
  - iOS: SwiftLint with strict configuration
  - WebApp: ESLint with TypeScript strict mode
- **Code Reviews**: ALL changes require peer review with explicit approval from at least one senior developer
- **Documentation**: Public APIs MUST have comprehensive documentation including purpose, parameters, return values, and usage examples
- **Complexity Limits**: Functions MUST NOT exceed cyclomatic complexity of 10; any violation requires architectural justification
- **Dependency Management**: Direct dependencies MUST be justified, regularly updated, and security-scanned
- **DRY Principle**: Shared logic MUST be extracted into platform-agnostic modules; platform-specific implementations allowed only for UI and native APIs

**Rationale**: Mobile apps require long-term maintainability across multiple platforms and OS versions. Code quality prevents technical debt accumulation and ensures consistent behavior across platforms.

### II. Test-Driven Development (NON-NEGOTIABLE)

TDD is mandatory for ALL features across ALL platforms with the following requirements:

- **Red-Green-Refactor**: Tests MUST be written first → verified failing → implementation → tests pass → refactor
- **User Approval Gate**: Test scenarios MUST be reviewed and approved by stakeholders before implementation begins
- **Minimum Coverage Requirements**:
  - Unit Tests: 80% code coverage for business logic (platform-agnostic modules)
  - Integration Tests: 70% coverage for API interactions and data persistence
  - UI Tests: Critical user flows MUST have automated UI tests on each platform
  - Snapshot Tests: All reusable UI components MUST have snapshot/screenshot tests
- **Platform-Specific Testing**:
  - Android: JUnit + Espresso + Robolectric; AndroidX Test for instrumented tests
  - iOS: XCTest + XCUITest; Quick/Nimble for BDD-style tests
  - WebApp: Jest + React Testing Library + Playwright/Cypress for E2E
- **Test Independence**: Each test MUST run independently; no shared mutable state between tests
- **Performance Tests**: Load time, animation frame rates, and memory usage MUST be validated through automated performance tests

**Rationale**: Mobile app failures directly impact user experience and app store ratings. TDD ensures reliability, catches platform-specific bugs early, and provides regression protection during OS updates.

### III. Cross-Platform UX Consistency

User experience MUST be consistent across platforms while respecting platform conventions:

- **Design System**: ALL platforms MUST implement the same design tokens (colors, typography, spacing, corner radii)
- **Feature Parity**: Core features MUST be available on all platforms unless technically impossible
- **Platform Conventions**: UI MUST follow platform-specific guidelines:
  - Android: Material Design 3 components and navigation patterns
  - iOS: Human Interface Guidelines including SF Symbols and native navigation
  - WebApp: Responsive design with mobile-first approach; follows WCAG 2.1 AA accessibility standards
- **Accessibility**: ALL interactive elements MUST support:
  - Screen readers (TalkBack, VoiceOver, NVDA/JAWS)
  - Minimum touch target size: 44x44 dp/pt
  - Sufficient color contrast ratios (4.5:1 for text)
  - Keyboard navigation for web
- **Localization**: ALL user-facing text MUST be externalized and support RTL layouts
- **Offline-First**: Core functionality MUST work offline with clear sync status indicators
- **Error Handling**: Error messages MUST be user-friendly, actionable, and consistent across platforms

**Rationale**: Users expect consistent functionality while feeling native to their platform. This builds trust and reduces learning curve when users switch between devices.

### IV. Performance Standards (NON-NEGOTIABLE)

ALL platforms MUST meet these performance requirements:

- **App Launch Time**:
  - Android: Cold start < 2 seconds on mid-range devices (Snapdragon 6 series equivalent)
  - iOS: Cold start < 1.5 seconds on devices from last 3 years
  - WebApp: First Contentful Paint < 1.5 seconds on 3G connection
- **Frame Rate**: UI animations MUST maintain 60fps (16.67ms per frame); no dropped frames during transitions
- **Memory Constraints**:
  - Android: < 150MB memory footprint on mid-range devices
  - iOS: < 100MB memory footprint; no memory warnings during normal usage
  - WebApp: < 50MB JavaScript bundle size after compression
- **Network Efficiency**:
  - API responses < 500ms for p95 latency
  - Image optimization: WebP/AVIF with lazy loading
  - Request batching for multiple API calls
  - Proper HTTP caching headers implementation
- **Battery Impact**: Background processing MUST use platform-appropriate APIs (WorkManager, BGTaskScheduler, Service Workers)
- **Storage**: Local storage MUST be optimized; periodic cleanup of cached data
- **Monitoring**: ALL apps MUST integrate analytics and crash reporting (Firebase Analytics/Crashlytics or equivalent)

**Performance Testing Requirements**:
- Automated performance tests MUST run in CI/CD pipeline
- Performance regressions > 10% MUST block deployment
- Monthly performance audits comparing against previous baseline

**Rationale**: Mobile users have low tolerance for poor performance. Battery drain, slow responses, and crashes directly impact user retention and app store ratings.

## Platform-Specific Requirements

### Android Requirements
- **Minimum SDK**: API 24 (Android 7.0) with targetSdk at latest stable
- **Architecture**: MVVM or MVI with Kotlin Coroutines and Flow
- **Dependency Injection**: Hilt (Dagger) for dependency management
- **Build System**: Gradle with Kotlin DSL; build variants for dev/staging/production
- **Security**: ProGuard/R8 obfuscation enabled; certificate pinning for API calls

### iOS Requirements
- **Minimum Version**: iOS 14.0+ with support for latest iOS
- **Architecture**: MVVM or Swift Composable Architecture
- **Language**: Swift 5.9+; no Objective-C for new code
- **UI Framework**: SwiftUI preferred; UIKit for complex custom components
- **Security**: Keychain for sensitive data; App Transport Security enforced

### WebApp Requirements
- **Browsers**: Chrome/Edge (last 2 versions), Firefox (last 2), Safari (last 2)
- **Framework**: React 18+ with TypeScript 5.0+ in strict mode
- **State Management**: React Context + Hooks or Redux Toolkit for complex state
- **Build**: Vite or Webpack with code splitting and tree shaking
- **Progressive Web App**: Service worker for offline support; installable

## Quality Gates

Before ANY deployment to production, the following MUST pass:

1. **Automated Checks**:
   - All unit, integration, and UI tests pass (zero failures)
   - Code coverage meets minimum thresholds (80% unit, 70% integration)
   - Static analysis passes with zero high-severity issues
   - Security vulnerability scan passes (no critical/high CVEs)
   - Performance benchmarks within acceptable thresholds

2. **Manual Verification**:
   - QA team sign-off on core user flows
   - Accessibility audit completed
   - Localization verified for supported languages
   - App store compliance check (privacy labels, metadata, screenshots)

3. **Platform-Specific Gates**:
   - Android: APK size < 50MB (before Play Asset Delivery); no release-blocking lint errors
   - iOS: IPA size < 50MB (before on-demand resources); TestFlight beta period of at least 48 hours
   - WebApp: Lighthouse score > 90 for Performance, Accessibility, Best Practices

## Governance

This constitution supersedes all other development practices and guidelines. All team members MUST comply with these principles.

**Amendment Procedure**:
- Proposed changes MUST be documented with clear rationale
- Changes require approval from technical lead and product owner
- MAJOR version bump: Removal or redefinition of core principles
- MINOR version bump: Addition of new principles or substantial expansions
- PATCH version bump: Clarifications, wording improvements, non-semantic refinements

**Compliance Validation**:
- All PRs MUST include constitution compliance checklist in description
- Code reviews MUST verify adherence to applicable principles
- Monthly audits to ensure ongoing compliance across all platforms
- Violations MUST be documented with remediation plan and timeline

**Versioning Policy**: This constitution follows semantic versioning (MAJOR.MINOR.PATCH). All changes MUST update the version number and amendment date. Historical versions maintained for reference.

**Version**: 1.0.0 | **Ratified**: 2025-12-05 | **Last Amended**: 2025-12-05
