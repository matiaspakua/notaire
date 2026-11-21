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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2384-factory-dashboard-v2/` | passed |
| Branch | `feat/2381_factory_dashboard` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| a catalog edit | kit, dashboard and repolens tests | pending |
| the kit is not reachable | kit, dashboard and repolens tests | pending |
| no fabricated run | kit, dashboard and repolens tests | pending |
| two workspaces | kit, dashboard and repolens tests | pending |
| two named tokens | kit, dashboard and repolens tests | pending |
| a read-only user | kit, dashboard and repolens tests | pending |
| a Claude Code session | kit, dashboard and repolens tests | pending |
| through the contract | kit, dashboard and repolens tests | pending |
| a knowledge search | kit, dashboard and repolens tests | pending |
| independence | kit, dashboard and repolens tests | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/factory-dashboard/README.md` | no | pending |
| `framework/ai-sdlc/docs/adr/` | no | pending |
| `framework/ai-sdlc/improvements/SESSION-FEATURES.md` | no | pending |
| `framework/repolens/README.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2384, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
