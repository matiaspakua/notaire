## REMOVED Requirements

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

## ADDED Requirements

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
