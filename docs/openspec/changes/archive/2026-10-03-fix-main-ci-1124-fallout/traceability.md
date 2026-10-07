# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1067 (Gate 1 open issue; fallout from #1124 / #1068) | open |
| Use Case | CU39 — Crear Plantilla Presupuesto | exists |
| Specification | `openspec/changes/fix-main-ci-1124-fallout/` | Gate 1 writing |
| Branch | `cursor/fix-main-ci-1124-fallout-69d3` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending push | pending |
| Pull Request | #1127 | open |
| CI run | pending after push | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create template with DTO IDs returns 201 | `BudgetTemplateSerializationIntegrationTest` | fixing |
| Duplicate create returns 409 | `BusinessWorkflowIntegrationTest` | fixing |
| Missing FK returns 400 not 500 | controller `existsById` guard | implementing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| OpenSpec change folder (this) | yes | pending |
| Permanent docs | n/a — hotfix restores #1068 contract | — |

## Gate log

| Gate | Result | Evidence |
|------|--------|----------|
| Gate 1 Specification | in progress | this change folder |
| Gate 2 TDD | in progress | failing CI → fix → re-run |
| Gate 3 Docs / preflight | pending | CI on #1127 |
| Gate 4 Review / merge | pending | merge when green |
| Gate 5 Release | pending | main green after merge |

## Exceptions

None. No `sdlc-exception` label; OpenSpec change folder satisfies Process Checks.
No wrongful `Closes` for #1068 (already closed via #1124).
