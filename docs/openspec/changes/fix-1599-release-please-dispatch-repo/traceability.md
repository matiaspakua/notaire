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
| Issue | #1599 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1599-release-please-dispatch-repo/` | drafted |
| Branch | `cursor/fix-release-please-cd-dispatch-393d` | created |
| Tasks | `tasks.md` | in progress |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | next `v*` after merge; `v0.2.1` backfill | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Dispatch names the repository without a git checkout | `DispatchOnTagTest.test_dispatch_names_repository_without_git_checkout` | pending |
| Release creation dispatches delivery on the tag | `DispatchOnTagTest.test_release_please_dispatches_cd_on_tag` | pending |
| No release created, no dispatch | `DispatchOnTagTest.test_release_please_dispatches_cd_on_tag` (`release_created` guard) | pending |
| Manually pushed tags still publish | `DispatchOnTagTest.test_cd_keeps_tag_filters` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/RELEASE.md` | no | pending |
| `docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md` | no | pending |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1599 OPEN; `validate-sdlc-plan.sh` on this change |
| 2 | Failing tests written, test cases designed | | pending |
| 3 | Suite green, coverage held, docs updated | | pending |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

Issue labels / board Status writes remain 403 for fleet tokens (#1551). No
Constitution exception requested for that; a human paste is required.
