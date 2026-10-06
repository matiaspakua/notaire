# docs

**Purpose:** Business and architecture knowledge base: requirements, use cases, SAD, ADRs, manuals.

**Verify:** `bash docs/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- Numbered sections `100-business` ... `300-development`; ADR index

## Seams (what this module reads from outside)

- Flyway migrations and controllers (read by the ERD, data dictionary and CU-API generators)

## Must not

- Hold code or generated reports

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
