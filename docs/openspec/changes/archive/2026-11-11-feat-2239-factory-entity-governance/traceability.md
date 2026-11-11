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
| Issue | #2239 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2239-factory-entity-governance/` | Gate 1 artifacts present |
| Branch | `feat/2239_factory-phase-a-domain` | created |
| Tasks | `tasks.md` | pending |
| Commits | db0f4584, 530b7471, c849dd1e | recorded |
| Pull Request | #2244 | open |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| new version preserves prior bytes | test_entity_versioning.py | pending |
| providers kind round-trip | test_entity_versioning.py | pending |
| unknown kind rejected | test_entity_versioning.py / test_catalogs.py | pending |
| happy path to approved | test_workflow_governance.py | pending |
| illegal transition | test_workflow_governance.py | pending |
| approve fails closed without live approval or override | test_workflow_governance.py | pending |
| approved filter excludes draft | test_workflow_governance.py | pending |
| contract check green / schemas present | test_contract.py | pending |
| update creates new revision | test_entity_versioning.py | pending |
| show returns current version | test_entity_versioning.py | pending |
| live verify required for approve | test_workflow_governance.py | pending |
| no cosmetic approve | test_workflow_governance.py | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | no | pending |
| `CHANGELOG.md` | omit (Unreleased fence / release-please) | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | yes | test_entity_versioning / test_workflow_governance |
| 3 | Suite green, coverage held, docs updated | yes | verify.sh + preflight --full |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Gate 1.6 WIP label: SKIPPED (addLabelsToLabelable / issues:write 403). Continue
per FLEET-IDENTITY.md — do not fake in-progress on GitHub.
