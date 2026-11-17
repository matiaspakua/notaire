# frontend-vitest-coverage-floor Specification

## Purpose

Establish a deliberate, documented raise-only Vitest coverage floor for the
Notaire frontend so CI reflects intentional quality policy (mirroring backend
JaCoCo), and record why branch coverage historically fell under 6%. Source:
\#976; CU76.

## Requirements

### Requirement: Root cause of the historical Vitest branch failure is documented

Permanent quality documentation MUST state whether the original 6% branch
threshold was aspirational headroom that coverage later undercut, and what
restored coverage above the floor on current `main`.

#### Scenario: Root cause is written in permanent docs

- **WHEN** a contributor reads the updated code-quality or frontend testing
  documentation after this change
- **THEN** it explains the historical <6% branch failure and the remediation
  approach (tests added over time and/or threshold policy), not only a silent
  config edit

### Requirement: Vitest thresholds are a raise-only ratchet below measured coverage

`frontend/vitest.config.ts` global coverage thresholds MUST be set to a
deliberate floor strictly below freshly measured coverage at implement time,
with comfortable headroom, and MUST NOT be lowered without a documented
exception. After the change, `npx vitest run --coverage` MUST pass.

#### Scenario: Coverage run passes with raised floors

- **WHEN** `cd frontend && npx vitest run --coverage` runs after thresholds are
  updated
- **THEN** the command exits 0 and reported branches/statements/functions/lines
  are ≥ the configured thresholds

#### Scenario: New floors sit below measured coverage

- **WHEN** implementers compare configured thresholds to the coverage summary
  from the same run
- **THEN** each threshold metric is less than the measured percentage for that
  metric (headroom preserved)

### Requirement: Frontend CI Vitest job stays green

The `Unit Tests (Vitest)` job in Frontend CI MUST pass on the change’s PR and
remain passable on `main` after merge.

#### Scenario: Frontend CI Vitest job succeeds

- **WHEN** Frontend CI runs on the PR that lands this change
- **THEN** the Vitest-with-coverage job concludes successfully

### Requirement: Raise-only policy is documented like JaCoCo

`.claude/rules/code-quality.md` (and/or the frontend testing guide) MUST
document the Vitest coverage floor numbers and the raise-only rule analogous to
the backend JaCoCo ratchet.

#### Scenario: Policy docs mention Vitest raise-only floors

- **WHEN** the permanent quality rules are inspected after this change
- **THEN** they state the frontend Vitest floor metrics and that floors are
  raise-only unless an explicit documented exception exists

### Requirement: Vitest statement floor matches the 2026-11-17 measurement

`frontend/vitest.config.ts` MUST enforce coverage thresholds of at least 87% statements, 79% branches, 85% functions and 88% lines. Each threshold MUST stay at least two percentage points under the coverage measured for this change (statements 89.15, branches 81.43, functions 87.99, lines 90.46). `CONSTITUTION.md`, `docs/300-development/303-testing/TEST-PLAN.md` and `docs/300-development/303-testing/README.md` MUST state those same four enforced floors. The Owner target remains greater than 90% and MUST stay labeled separately from the floors.

#### Scenario: Guard rejects a statement floor below the ratchet

- **WHEN** the documented floors in `vitest-coverage-thresholds.test.ts` are compared with `frontend/vitest.config.ts`
- **THEN** the configured statement threshold is at least 87 and the other three thresholds are at least 79, 85 and 88

#### Scenario: Permanent docs quote the enforced floors

- **WHEN** a reader opens the Constitution coverage table, TEST-PLAN section 4, or the testing README coverage table
- **THEN** each document lists statements 87, branches 79, functions 85 and lines 88, and still names the Owner target as greater than 90%
