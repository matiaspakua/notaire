# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens — never pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1115 | open |
| Use Case | CU76 — Engineering Constitution process tooling | linked |
| Specification | `openspec/changes/cursor-cloud-agent-environment/` (`skip_specs: true`) | drafted |
| Branch | `cursor/cloud_agent_environment` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | #1112 | open |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — internal tooling, no release artifact | pending |
| Smoke test | install/start health checks on Cloud Agent VM | partial |

## Requirement coverage

n/a — `skip_specs: true`. Acceptance Criteria are in Issue #1115.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Idempotent cloud install script | Manual second-run of `.cursor/install.sh` | passed |
| Start script brings stack healthy | `/actuator/health` UP, login JWT, `/login` 200 | passed |
| Host-network Docker compose override | `docker-compose.cloud.yml` present + Cloud boot | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| Sibling fleet checklist (PR #1111) may reference scripts | deferred to #1111 | n/a |
| This OpenSpec change folder | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1115; this change folder |
| 2 | Failing tests written, test cases designed | n/a (scripts/chore; verification in design.md) | design.md — Testing Strategy |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **TDD (Gate 2)**: no production code under `backend-api`/`frontend/src`;
  verification via install/start health checks.
- **Branch naming**: Cloud Agent branch `cursor/cloud_agent_environment`;
  association recorded via Issue #1115 / PR body `Closes #1115`.
