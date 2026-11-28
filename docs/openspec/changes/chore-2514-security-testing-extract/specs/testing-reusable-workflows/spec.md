<!-- Governed by CONSTITUTION.md. CU76, issue #2514. -->

## Purpose

Notaire obtains product-level E2E and database V&V suites from an independent QA
repository via pinned reusable workflows, while Bruno, pg-integration and unit
tests stay in the product.

## ADDED Requirements

### Requirement: E2E and database workflows pin the testing fleet repository

Notaire MUST invoke Playwright E2E and database V&V through reusable workflows
hosted at `matiaspakua/notaire-testing`, pinned by commit SHA, and MUST pass an
`app-ref` (or equivalent) so the suite runs against the product revision under test.

#### Scenario: e2e/database workflows pin notaire-testing

- **WHEN** a developer inspects `.github/workflows/playwright-e2e.yml` and
  `.github/workflows/database-vv.yml`
- **THEN** each caller uses `matiaspakua/notaire-testing/.github/workflows/` at a
  pinned SHA (not a floating branch tip as the only pin)

#### Scenario: local testing tree is stub-only

- **WHEN** a developer lists the product `testing/` path
- **THEN** the suites (`e2e/`, `database/`, `integration/`, …) are absent and a
  README stub points at `matiaspakua/notaire-testing`

### Requirement: Testing seams resolve via NOTAIRE_APP_ROOT and NOTAIRE_TESTING_ROOT

Harness scripts that need the product checkout and the testing suites MUST honour
`NOTAIRE_APP_ROOT` and `NOTAIRE_TESTING_ROOT` (with defaults compatible with a
monorepo or caller checkout layout).

#### Scenario: NOTAIRE_APP_ROOT / NOTAIRE_TESTING_ROOT resolve

- **WHEN** the CU-API matrix validator or coverage/report tooling is invoked with
  those environment variables set
- **THEN** it resolves paths relative to the provided roots rather than hardcoding
  only the historical monorepo layout
