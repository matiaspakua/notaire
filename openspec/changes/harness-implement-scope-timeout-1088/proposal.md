# Implement scope admits planned files; the worker timeout really fires

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1088 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/1088_harness_implement_scope_timeout` |
| Gate 1 status | passed |

## Objetivo

Two harness defects stalled #1063 at the implement phase for 90 minutes:

- `phase_implement` allows only `.localai/`, the change's OpenSpec folder and
  the source roots. #1063's approved spec plans edits to `CONSTITUTION.md` and
  `.claude/rules/code-quality.md`, and its tests assert them. The scope guard
  reverted those edits on every attempt, so the green gate could never pass.
- The worker timeout (`perl -e 'alarm N; exec codex …'`) did not stop
  `codex exec`. It ran 90 minutes with `WORKER_TIMEOUT=3600` until the
  foreman killed it by hand.

## What Changes

- New `local-ai/sdlc/bin/scope.py`. `implement` prints the implement-phase
  scope regex. It keeps today's scope and adds, as exact paths, every existing
  file named in triage `## Files to Edit` and in traceability
  `## Planned Files`.
- New `local-ai/sdlc/bin/watchdog.py SECONDS CMD…`. It runs the worker in its
  own process group. On timeout it sends TERM to the group, then KILL after a
  grace period, and exits 124. It also forwards TERM/INT it receives to the
  group, so stopping the foreman stops the worker.
- `foreman.sh`: `phase_implement` builds its scope with `scope.py`, and
  `run_worker` uses `watchdog.py` instead of the perl alarm.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The implement phase may edit every file the approved spec plans | CONSTITUTION Gate 1 (spec approved before code) | Changed |
| A worker run never outlives `WORKER_TIMEOUT` | AI-SDLC.md | Made explicit (now enforced) |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: adds the implement scope and the worker watchdog.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — (the harness self-tests already run in `sdlc-process.yml`) |
| `local-ai/sdlc` harness | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (Python standard library)

### Architecture review

AUDIT §4.1 layer L5 (guards). No ADR: the product architecture does not change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | Scope guard row: implement scope; the timeout row names the watchdog |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Scopes of other phases, the worker's `tasks.md` rewrites and commit hygiene
(both already gated), and splitting `foreman.sh`.
