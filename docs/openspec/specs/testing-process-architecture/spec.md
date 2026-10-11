# testing-process-architecture Specification

## Purpose
Define a coherent Master Test Plan, living inventory, and process architecture so
Notaire’s QA Automation stays identifiable, traceable, and fail-fast under the
AI SDLC (CU76).
## Requirements
### Requirement: Living testing inventory

The repository SHALL maintain a generated inventory of Playwright suites and
suite-volume counts under `docs/300-development/303-testing/INVENTORY.md`,
regenerable from disk and validated in local/CI preflight.

#### Scenario: Inventory regenerates from disk

- **WHEN** an agent or developer runs the inventory generator
- **THEN** `INVENTORY.md` lists every `TS-nnnn-*.spec.ts` present under
  `testing/e2e/tests/` and reports current counts for backend unit/integration
  test classes, Vitest files, and Bruno YAML requests

#### Scenario: Stale inventory fails validation

- **WHEN** `INVENTORY.md` does not match the files currently on disk
- **THEN** the inventory validator exits non-zero

#### Scenario: Unprefixed Playwright spec rejected

- **WHEN** a file matching `testing/e2e/tests/*.spec.ts` does not start with
  `TS-` followed by four digits and a hyphen
- **THEN** the inventory validator exits non-zero

#### Scenario: All specs use TS-nnnn prefix

- **WHEN** the inventory validator runs on a compliant tree
- **THEN** every Playwright spec under `testing/e2e/tests/` matches
  `TS-nnnn-*.spec.ts` and validation succeeds

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
- **THEN** backend floors match JaCoCo enforcement (80% line / 65% branch) and
  frontend floors match `vitest.config.ts` raise-only thresholds, with any
  aspirational targets labeled separately from enforced floors

