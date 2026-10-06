# testing

**Purpose:** Black-box QA suites: Playwright E2E, integration smoke and database V&V.

**Verify:** `bash testing/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- `testing/scripts/run.sh <suite>` and one spec per Use Case scenario (`TS-nnnn`)

## Seams (what this module reads from outside)

- A running stack (frontend and backend URLs), the Flyway migrations for the database suite

## Must not

- Import application code; it observes the system over HTTP and SQL only

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
