# Pin Bruno CLI ≥4.2.0 in Cursor Cloud install

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1121 |
| Use Case | RNF / process: Engineering Constitution AI agent SDLC (CONSTITUTION.md) — Cursor Cloud AI SDLC fleet process tooling |
| Branch | `cursor/chore-bruno-cli-install-pin-69d3` |
| Gate 1 status | passed |

## Objetivo

Cloud Agent VMs boot without `bru` / `@usebruno/cli` on PATH after
`.cursor/install.sh`, so OpenCollection API validation (≥4.x) fails or falls back
to unpinned `npx`. Pin Bruno CLI 4.2.0 in the cloud install script and document it
in the environment checklist (same RNF / fleet process tooling as #1121).

This chore does **not** close #1121 (Closes-keyword docs; largely shipped). Related
only for Gate 1 Issue + Use Case linkage.

## What Changes

- Install `@usebruno/cli@4.2.0` globally in `.cursor/install.sh` after OpenSpec.
- Symlink `bru` onto `/usr/local/bin` (npm-global bin, else package entry).
- Document Bruno CLI ≥4.2.0 in
  `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` toolchain /
  PATH tables.
- Add this OpenSpec change folder so Process Checks pass without `sdlc-exception`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Cloud install SHALL put Bruno CLI ≥4.2.0 (`bru`) on PATH | #1121 RNF / CONSTITUTION cloud fleet process tooling | New |
| Environment checklist SHALL list Bruno CLI ≥4.2.0 as required toolchain | #1121 ENVIRONMENT-CHECKLIST accept criteria lineage | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `bruno-cli-cloud-install`: pin and PATH wiring for Bruno in `.cursor/install.sh`;
  checklist documentation.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Process Checks via this change folder |
| `.cursor/install.sh` | yes | Pin `@usebruno/cli@4.2.0` + `bru` symlink |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | Toolchain + PATH for Bruno |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: npm global `@usebruno/cli@4.2.0` on Cloud Agent VMs only

### Architecture review

No application-architecture change. No ADR. Cloud install / process tooling only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Bruno CLI ≥4.2.0 in toolchain table, PATH list, install idempotency note |
| `CHANGELOG.md` | n/a — not user-visible |
