# testing-process-architecture Specification

## Purpose
Define a coherent Master Test Plan, living inventory, and process architecture so
Notaire’s QA Automation stays identifiable, traceable, and fail-fast under the
AI SDLC (CU76).

## Requirements

### Requirement: Master Test Plan and process architecture

The project SHALL publish a Master Test Plan and a process-architecture document
that together define test levels, artifact IDs, owners, CI triggers, and evidence.

#### Scenario: MTP declares levels and ID scheme

- **WHEN** a reader opens `docs/300-development/303-testing/TEST-PLAN.md`
- **THEN** the document identifies itself as the Master Test Plan, lists all
  test levels (unit through security/performance), and defines the ID scheme
  CU → TC → TS/Bruno/JUnit → Issue

#### Scenario: Process architecture document exists

- **WHEN** a reader opens `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md`
- **THEN** the document describes process boxes (prepare, define, execute,
  evidence), suite owners, and which CI workflows gate each level

#### Scenario: Preflight runs inventory validation

- **WHEN** `bash workspace/sdlc/preflight.sh` runs its process checks
- **THEN** it invokes the testing inventory validator

#### Scenario: Coverage floors documented match tools

- **WHEN** Constitution §7 and the Master Test Plan state coverage floors
- **THEN** backend floors match JaCoCo enforcement (85% line / 70% branch) and
  frontend floors match `vitest.config.ts` raise-only thresholds, with the Owner
  **>90%** target (#2046) labeled separately from enforced floors

### Requirement: Discovery-based testing inventory

The repository SHALL treat disk discovery as the source of truth for Playwright
suite naming and suite-volume counts. Local/CI preflight SHALL validate the
discovered set. A full markdown report MAY be generated on demand or during the
docs build; feature pull requests MUST NOT commit a shared count table that
parallel branches would collide on.

#### Scenario: Inventory regenerates from disk

- **WHEN** an agent or developer runs the inventory generator
- **THEN** the generated report lists every `TS-nnnn-*.spec.ts` present under
  `testing/e2e/tests/` and reports current counts for backend unit/integration
  test classes, Vitest files, and Bruno YAML requests

#### Scenario: Validator checks discovery not a committed count table

- **WHEN** the inventory validator runs on a compliant tree
- **THEN** it succeeds based on discovered files (readable backend, frontend, and
  Bruno trees plus Playwright `TS-nnnn` names), prints the counts, and does not
  fail solely because a committed markdown snapshot lags the tree

#### Scenario: Unprefixed Playwright spec rejected

- **WHEN** a file matching `testing/e2e/tests/*.spec.ts` does not start with
  `TS-` followed by four digits and a hyphen
- **THEN** the inventory validator exits non-zero

#### Scenario: All specs use TS-nnnn prefix

- **WHEN** the inventory validator runs on a compliant tree
- **THEN** every Playwright spec under `testing/e2e/tests/` matches
  `TS-nnnn-*.spec.ts` and validation succeeds

#### Scenario: Feature PRs do not share a committed count line

- **WHEN** two branches each add a different Vitest file
- **THEN** merging them does not require both sides to edit the same committed
  inventory count line
