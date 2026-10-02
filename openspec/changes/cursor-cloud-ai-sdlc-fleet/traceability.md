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
| Issue | #1114 | open |
| Use Case | none — documented internal-tooling exception (see proposal.md) | n/a |
| Specification | `openspec/changes/cursor-cloud-ai-sdlc-fleet/` (`skip_specs: true`) | drafted |
| Branch | `cursor/ai-sdlc-cloud-fleet-6890` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | #1111 | open |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — internal tooling, no release artifact | pending |
| Smoke test | VALIDATION-PLAN.md steps A/C/D on agent checkout | partial |

## Requirement coverage

n/a — `skip_specs: true`. Acceptance Criteria are in Issue #1114.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Fleet docs under `docs/300-development/304-ai-sdlc-cloud/` | Path presence | passed |
| Foreman + specialist agent defs | Path presence under `.claude/agents/` | passed |
| Env checklist + validation plan | Path presence | passed |
| Explicit exclusion of `local-ai/` from cloud path | Grep exclusion language in architecture + foreman | passed |
| Manifest agent_file / skills resolve | Python coherence check (VALIDATION-PLAN step D) | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/*` | yes | pending |
| `AGENTS.md` | yes | pending |
| `docs/README.md`, `docs/300-development/README.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1114; this change folder |
| 2 | Failing tests written, test cases designed | n/a (docs/agent-config; verification plan in design.md) | design.md — Testing Strategy |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **TDD (Gate 2)**: Markdown/agent-config only; verification via VALIDATION-PLAN.md.
- **No Use Case**: internal process tooling with no business behavior (precedent #1074).
- **Branch naming**: Cloud Agent required `cursor/<name>-6890`; Constitution form recorded via Issue #1114.
