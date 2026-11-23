# Fix stable RepoLens bench snapshot

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2429 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2429_bench_snapshot_stable` |
| Gate 1 status | passed |

## Objetivo

Every GitHub Pages build rewrites the tracked file `github-page/lib/repolens-bench-snapshot.json` with a fresh `generated_at` timestamp, leaving every checkout dirty even when no underlying bench data changed. This fix makes the generator keep the existing `generated_at` when the rest of the snapshot is byte-identical, so git sees no change.

## What Changes

`workspace/ci/generate-repolens-bench.py` now compares the data it would write (excluding `generated_at`) against the existing snapshot file. When the data matches, the script reuses the file's current `generated_at` value, producing a byte-identical output. When the data genuinely differs, a new `generated_at` is written as before. A corresponding test verifies both paths.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The bench snapshot generator must not modify the tracked file when only `generated_at` would differ | CU76 | New |
| When bench data actually changes, the generator must write a fresh `generated_at` | CU76 | New |

## Capabilities

### New Capabilities

- `ci-repolens-bench-snapshot`: The `generate-repolens-bench.py` script produces a stable snapshot that does not change the tracked file when only the timestamp would differ.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `workspace/ci/generate-repolens-bench.py`
- `workspace/tests/test_generate_repolens_bench.py`
- `docs/openspec/changes/fix-2429-bench-snapshot-stable/**`
- `docs/openspec/specs/ci-repolens-bench-snapshot/spec.md`

### Architecture review

No architectural change. This is a targeted bug fix in a CI helper script following the existing pattern.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | n/a - not user visible |

## Out of Scope

Moving the snapshot to a git-ignored path or making regeneration opt-in via a separate command — those are alternative designs not pursued here.
