# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2196 | in-progress |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2196-foreman-stage-knob-routing/` | drafted |
| Branch | `cursor/feat-2196-foreman-stage-knob-routing-e74d` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| omitted stage knobs yield empty plan | ForemanPlanTest | pending |
| mapped stage appears in plan json | ForemanPlanTest | pending |
| stage filter narrows the plan | ForemanPlanTest | pending |
| unknown stage filter fails closed | ForemanPlanTest | pending |
| plan never executes agents or mcp | ForemanPlanTest | pending |
| route is an alias of plan | ForemanPlanTest | pending |

## Module impact

| Module | Impact |
|--------|--------|
| framework/ai-sdlc | foreman plan CLI + tests |
| .claude/agents | cloud-foreman dispatch cites CLI |
| kit templates | foreman agent cites CLI |
| docs/300-development/306-ai-sdlc-framework | FACTORY notes |
| store thin plan / remaining-gaps | Foreman ops habit partially kit-backed |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `.claude/agents/cloud-foreman.md` | no | pending |
| `framework/ai-sdlc/templates/project/.aisdlc/agents/foreman.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | no | pending |
| `framework/ai-sdlc/README.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2196, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
