## ADDED Requirements

### Requirement: Vitest statement floor matches the 2026-11-17 measurement

`frontend/vitest.config.ts` MUST enforce coverage thresholds of at least 87% statements, 79% branches, 85% functions and 88% lines. Each threshold MUST stay at least two percentage points under the coverage measured for this change (statements 89.15, branches 81.43, functions 87.99, lines 90.46). `CONSTITUTION.md`, `docs/300-development/303-testing/TEST-PLAN.md` and `docs/300-development/303-testing/README.md` MUST state those same four enforced floors. The Owner target remains greater than 90% and MUST stay labeled separately from the floors.

#### Scenario: Guard rejects a statement floor below the ratchet

- **WHEN** the documented floors in `vitest-coverage-thresholds.test.ts` are compared with `frontend/vitest.config.ts`
- **THEN** the configured statement threshold is at least 87 and the other three thresholds are at least 79, 85 and 88

#### Scenario: Permanent docs quote the enforced floors

- **WHEN** a reader opens the Constitution coverage table, TEST-PLAN section 4, or the testing README coverage table
- **THEN** each document lists statements 87, branches 79, functions 85 and lines 88, and still names the Owner target as greater than 90%
