# docs

**Purpose:** Business and architecture knowledge base: requirements, use cases, SAD, ADRs, manuals.

**Verify:** `bash docs/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- Numbered sections `100-business` ... `300-development`; ADR index

## Seams (what this module reads from outside)

- A running PostgreSQL schema created by Flyway, read through `psql` by `docs/tools/generate_erd.py` and `generate_data_dictionary.py` (seam `database-schema`)

## Must not

- Hold product code or generated CI reports (the generators and guards in `tools/` and `tests/` only describe the system)

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
