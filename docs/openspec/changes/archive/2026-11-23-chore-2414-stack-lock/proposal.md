# Stack lock and .env copy for preflight server-backed suites

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2414 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/2414_preflight_stack_lock` |
| Gate 1 status | pending |

## Objetivo

Three stack problems cause preflight failures in worktrees: (1) concurrent runs share the same Docker containers and interfere, (2) worktrees lack `.env` so the backend crash-loops, and (3) the smoke test leaks containers on failure. This change adds an exclusive lock, copies `.env` from the main checkout, and ensures `docker compose down` always runs.

## What Changes

1. **Exclusive lock** — `workspace/sdlc/stack_lock.sh` provides `acquire` and `release` commands using atomic `mkdir`. `preflight.sh` wraps the server-backed section with this lock.
2. **`.env` copy** — Before starting the stack, if `$REPO_ROOT/.env` is missing, copy it from the main checkout (found via `git rev-parse --git-common-dir`).
3. **Docker smoke cleanup** — The smoke test always runs `docker compose down`, even when `up` fails, using a trap.

## Reglas de negocio

No business rules. This is tooling infrastructure under CU76.

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|

## Capabilities

### New Capabilities

None — tooling only.

### Modified Capabilities

None — `skip_specs: true` in `.openspec.yaml`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace/sdlc` | yes | New `stack_lock.sh`; `preflight.sh` changes |
| `workspace/tests` | yes | New `test_stack_lock.py` |
| `testing/e2e` | yes | README.md update |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: preflight now copies `.env` from main checkout if missing
- Dependencies: none

### Write set

- `workspace/sdlc/preflight.sh`
- `workspace/sdlc/stack_lock.sh`
- `workspace/tests/test_stack_lock.py`
- `workspace/tests/test_preflight_affected.py` (pages deps assertion)
- `testing/e2e/README.md`
- `docs/openspec/changes/chore-2414-stack-lock/`

### Architecture review

No architectural change. Pure SDLC tooling improvement.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `testing/e2e/README.md` | Note about the lock and .env copy |
| `CHANGELOG.md` | n/a - not user visible |

## Out of Scope

- Not changing the stack start script itself (`workspace/stack/start.sh`).
- Not adding flock support — macOS lacks it; `mkdir` is the portable choice.
