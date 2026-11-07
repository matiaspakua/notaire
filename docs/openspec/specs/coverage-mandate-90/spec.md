# coverage-mandate-90 Specification

## Purpose
TBD - created by archiving change chore-2046-coverage-mandate-and-sdlc-lessons. Update Purpose after archive.
## Requirements
### Requirement: Coverage Owner target is greater than 90% with raise-only floors

The project SHALL document an Owner quality **target** of greater than 90% coverage
across the test pyramid (unit, integration, API, E2E UI, security, performance where
measured). Enforced JaCoCo and Vitest **floors** SHALL ratchet upward and SHALL NOT be
lowered without an ADR. Floors may lag the target; documentation SHALL label target vs
floor separately.

#### Scenario: TEST-PLAN labels Owner target separately from floors

- **GIVEN** `docs/300-development/303-testing/TEST-PLAN.md` coverage table
- **WHEN** an agent or human reads enforced vs Owner-target columns
- **THEN** the Owner target is documented as **>90%** and enforced JaCoCo / Vitest
  floors are listed from their source-of-truth tool configs without claiming the
  floors already equal 90%

#### Scenario: Child tips track pyramid layers under the umbrella

- **GIVEN** umbrella issue #2046
- **WHEN** Vitest or Bruno/Playwright/security/perf gaps need work
- **THEN** child Issues exist (e.g. #2047, #2048) rather than closing #2046 early

