# Keep generated testing inventory off parallel pull requests

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2117 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/2117_testing-inventory-off-prs` |
| Gate 1 status | passed |

## Objetivo

Parallel feature PRs that each add a Vitest (or other) suite collide on the
generated monolith `docs/300-development/303-testing/INVENTORY.md` because
preflight required a committed count table to match a full-tree regen. Stop
committing that shared count table on feature PRs; validate discovery on disk;
keep an on-demand / docs-build report for humans.

## What Changes

- `validate-testing-inventory.py` checks the discovered set (Playwright
  `TS-nnnn` names; readable backend / frontend / Bruno trees) and prints
  counts. It does **not** diff a committed count table.
- `generate-testing-inventory.py` writes a local / docs-build report
  (`INVENTORY.generated.md`, gitignored). Feature PRs do not commit that
  report. Committed `INVENTORY.md` becomes a stable stub (no shared counts).
- github-page `prebuild` regenerates the on-demand report (Pages already
  discovers live counts via `tech-facts.testCounts()`).
- Update `testing/tests/test_testing_inventory.py`, testing README, and one
  lesson line in `FLEET-LESSONS-HEX-1693.md`.
- Delta-modify OpenSpec capability `testing-process-architecture`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Discovery on disk is the source of truth for suite presence and naming | CU76 / #1511 / #2117 | Changed |
| Feature PRs MUST NOT commit a regenerated shared count table | CU76 / #2117 | New |
| Playwright specs MUST keep the `TS-nnnn-` prefix; validator fails otherwise | CU76 / #1511 | Unchanged |
| Humans MAY generate a local/docs-build inventory report; it MAY lag on `main` | CU76 / #2117 | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `testing-process-architecture`: Living inventory validation is discovery-based;
  the committed markdown path stays as a stub / pointer; the full report is
  on-demand / docs-build only.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | no new PR workflow (#2001) |
| `workspace` (harness) | yes | validate + generate scripts |
| `testing` | yes | inventory unittest |
| `docs` | yes | stub INVENTORY, README, fleet lesson, OpenSpec |
| `github-page` | yes | prebuild generates on-demand report |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `.gitignore` for generated report
- Dependencies: none

### Write set

- `workspace/sdlc/validate-testing-inventory.py`
- `workspace/sdlc/generate-testing-inventory.py`
- `testing/tests/test_testing_inventory.py`
- `docs/300-development/303-testing/INVENTORY.md` (stub)
- `docs/300-development/303-testing/README.md`
- `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` (pointer wording)
- `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md`
- `github-page/package.json` (prebuild hook)
- `.gitignore`
- `.aisdlc/migration-inventory.toml` (hotspot note)
- `CHANGELOG.md`
- `docs/openspec/changes/docs-2117-testing-inventory-off-prs/**`
- `docs/openspec/specs/testing-process-architecture/spec.md` (on archive)

### Riesgos

| Risk | Mitigation |
|------|------------|
| Readers see a stub instead of counts on GitHub.com | Stub points to generate + Pages live metrics |
| Accidental commit of generated report | gitignore `INVENTORY.generated.md` |
| Agents still regenerate and commit the stub path | Docs + fleet lesson: never commit counts on feature PRs |

### Esfuerzo estimado

S — scripts + tests + docs; no product runtime.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/INVENTORY.md` | Replace generated monolith with stable stub (no shared counts) |
| `docs/300-development/303-testing/README.md` | On-demand generate; feature PRs do not commit the report |
| `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` | Validator discovers disk; report is local/docs-build |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | One lesson: never keep either side's inventory count line; feature PRs skip the shared report |
| `docs/openspec/specs/testing-process-architecture/spec.md` | Archive delta: discovery-based inventory |
| `.aisdlc/migration-inventory.toml` | Hotspot note reflects #2117 resolution path |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

- No new heavy GitHub workflow on PRs
- No Constitution merge-policy change
- Do not lower `preflight.sh --full`
- No Wilson / portal / Context Lake / `[committer] required=true`
