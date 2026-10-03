# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1050 | open (implement in progress) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #585, #682; audit-2026-09; #1046 removed frontend-swing from CODEOWNERS | referenced |
| Specification | `openspec/changes/chore-1050-repo-hygiene/` | Gate 1 validated on implement branch |
| Branch | `cursor/chore-1050-repo-hygiene-69d3` | active |
| Tasks | `tasks.md` | Gate 2–3 implement in progress |
| Commits | pending first push | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | — | pending |
| Release / tag | `docs-manuals` (user-manual PDF asset) | published |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Global `*.txt` no longer blocks needed text assets | `scripts/test_repo_hygiene.py` | green |
| `.serena/` is ignored and not tracked | same | green |
| CODEOWNERS paths match current tree (no `frontend-swing`) | same | green |
| 13 MB user-manual PDF not an ordinary git blob | same + Release `docs-manuals` | green |
| ADR records filter-repo decision | ADR-022 + same | green |
| CHANGELOG / contributor docs updated | docs review | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md` | yes | pending |
| `docs/200-architecture/202-ADR/README.md` | yes | pending |
| `docs/100-business/105-manuals/` + setup README + CU76 | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh chore-1050-repo-hygiene` |
| 2 | Failing tests written, test cases designed | yes | TDD red then green on `scripts/test_repo_hygiene.py` |
| 3 | Suite green, coverage held, docs updated | pending | preflight / CI |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None for ignore/CODEOWNERS/PDF/ADR. History rewrite deferred per ADR-022
(related #585/#682). Issue `in-progress` label ACL returned 403 for integration.
