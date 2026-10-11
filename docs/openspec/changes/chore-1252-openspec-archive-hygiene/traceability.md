# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1252 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1252-openspec-archive-hygiene/` | seeded + filled |
| Branch | `cursor/chore-1252-openspec-archive-hygiene-69d3` | created |
| Tasks | `tasks.md` | Gate 1 items ticked; implement pending |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Closed issue still has an active change | `workspace/tests/test_openspec_archive_hygiene.py` | pending |
| Archived after merge | `workspace/tests/test_openspec_archive_hygiene.py` | pending |
| Open issue WIP stays active | `workspace/tests/test_openspec_archive_hygiene.py` | pending |
| Unticked implementation task fails the gate | `workspace/tests/test_openspec_archive_hygiene.py` | pending |
| Implementation groups complete | `workspace/tests/test_openspec_archive_hygiene.py` | pending |
| Exception label bypass | `workspace/tests/test_openspec_archive_hygiene.py` | pending |
| Merge-gate document names archive verification | `workspace/tests/test_openspec_archive_hygiene.py` (doc guard) | pending |
| Missing instruction fails the doc guard | `workspace/tests/test_openspec_archive_hygiene.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | no | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | no | pending |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | no | pending |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | no | pending |
| `docs/openspec/specs/ai-sdlc-enforcement/spec.md` | no | pending sync at archive |
| `CHANGELOG.md` | deferred — #1518 holds Unreleased | pending after #1518 |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | no | pending apply |
| 3 | Suite green, coverage held, docs updated | no | pending apply |
| 4 | CI green, review approved, no conflicts | no | pending |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Related / Refs

- Assessment umbrella #1242; finding row in `docs/300-development/ASSESSMENT-2026-10.md`
- Prior archive hygiene under #1445 process PRs (pattern only; do not invent Owner decisions)
- TPF DevSecOps-from-QA practices adapted in `docs/300-development/INGENIERIA.md` (traceability + RDUF; not SecTx/GitFlow)
- CHANGELOG serialization: #1518

## Planned Files

| Path | Role |
|------|------|
| `workspace/sdlc/` (new checker script) | process gate |
| `workspace/tests/test_openspec_archive_hygiene.py` | Gate 2 tests |
| `.github/workflows/sdlc-process.yml` and/or `pr-validation.yml` | wire checker |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | agent instructions |
| `docs/openspec/changes/archive/*` | reconciled shipped trees |
| `docs/openspec/specs/ai-sdlc-enforcement/spec.md` | sync at archive |

## Exceptions

- Cloud branch suffix `-69d3` per fleet assignment (Constitution default remains
  `<type>/<issue>_<description>` for local agents).
- `in-progress` label may fail with integration 403; record outcome at apply.
- `CHANGELOG.md` intentionally untouched while Agent 3 / #1518 owns Unreleased.
