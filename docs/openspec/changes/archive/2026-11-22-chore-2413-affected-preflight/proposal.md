# Change-aware pre-push gate with a pass cache

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2413 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/2413_change_aware_gate` |
| Gate 1 status | passed |

## Objetivo

Every push runs `preflight.sh --full`, about 35 minutes, whatever the branch changed. A docs-only or factory-only branch pays for the product suites and trips their unrelated failures. The Owner decided (2026-11-22) that the gate runs only what a branch touches and does not run again for a commit that already passed.

## What Changes

- `preflight.sh --full` becomes change-aware. A new helper, `workspace/sdlc/preflight_affected.py`, maps the paths the branch changed (compared with origin/main) to suite groups: backend, frontend, e2e-suite lint, Pages and server-backed. Each group runs only when touched. Process checks, security gates and self-tests always run.
- Cross-cutting paths force everything: the gate itself, `.githooks/`, `workspace/stack/`, compose files, the root `pom.xml` and Dockerfiles. Everything also runs when there is no base to compare with.
- A new flag, `preflight.sh --all`, runs everything, for releases and cross-cutting changes.
- The pre-push hook records a pass per commit and mode under the git common directory, and skips a re-push of a commit that already passed. `PREFLIGHT_ALL=1 git push` forces everything.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The pre-push gate runs the suites a branch touches; cross-cutting changes run everything | Owner decision 2026-11-22 (#2413); CONSTITUTION §5 step 19 | Changed |
| A commit that passed the gate is not gated again when re-pushed | Owner decision 2026-11-22 (#2413) | New |

## Capabilities

### New Capabilities

- `preflight-change-aware`: the local gate scopes its suites to the branch's changes and caches passes per commit.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

<!-- Mark every module this change touches. Delete rows that do not apply, but do
     not delete the table: an empty table means the analysis was not done. -->

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace/sdlc` and `.githooks` | yes | change-aware preflight, affected helper, hook pass cache |
| Governance docs | yes | CONSTITUTION, agent card, AGENTS.md, ai-agent-workflow, steward skill |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities: none.
- Endpoints: none.
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: `PREFLIGHT_ALL=1` for the hook; pass markers in `$(git rev-parse --git-common-dir)/preflight-pass/` (never committed).
- Dependencies: none.

### Write set

- `workspace/sdlc/preflight.sh`
- `workspace/sdlc/preflight_affected.py`
- `workspace/tests/test_preflight_affected.py`
- `workspace/tests/test_preflight_hook_cache.py`
- `.githooks/pre-push`
- `CONSTITUTION.md`
- `CONSTITUTION-AGENT-CARD.md`
- `AGENTS.md`
- `.claude/rules/ai-agent-workflow.md`
- `.claude/skills/steward/SKILL.md`
- `CHANGELOG.md`
- `docs/openspec/changes/**`
- `docs/openspec/specs/preflight-change-aware/**`

### Architecture review

No ADR: the gate keeps its checks and their CI mapping; only when each group runs changes. The classification is a stdlib Python helper so it is unit-tested.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | The local preflight and merge gate rows say `--full` is change-aware and `--all` runs everything |
| `CONSTITUTION-AGENT-CARD.md`, `AGENTS.md`, `.claude/rules/ai-agent-workflow.md`, `.claude/skills/steward/SKILL.md` | Same sentence where they name the gate |
| `CHANGELOG.md` | `[Unreleased]` entry |

## Out of Scope

- Running suite groups in parallel. Possible next step; it needs separate stack and port isolation.
- Changing what any check verifies or its CI mapping.
