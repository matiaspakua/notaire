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
| Commits | `e3e2c60f` `[#1599] fix(release): name GH_REPO on CD tag dispatch` | done |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | `v0.2.1` assets backfilled (tag unmoved); next `v*` proves dispatch | pending |
| Smoke test | `v0.2.1` five assets; `publishedAt` 2026-10-13T08:02:03Z | done (backfill) |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Dispatch names the repository without a git checkout | `DispatchOnTagTest.test_dispatch_names_repository_without_git_checkout` | passing |
| Release creation dispatches delivery on the tag | `DispatchOnTagTest.test_release_please_dispatches_cd_on_tag` | passing |
| No release created, no dispatch | `DispatchOnTagTest.test_release_please_dispatches_cd_on_tag` (`release_created` guard) | passing |
| Manually pushed tags still publish | `DispatchOnTagTest.test_cd_keeps_tag_filters` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/RELEASE.md` | yes | `e3e2c60f` |
| `docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md` | yes | `e3e2c60f` |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | `e3e2c60f` |
| `CHANGELOG.md` | yes | `e3e2c60f` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1599 OPEN; `validate-sdlc-plan.sh` on this change |
| 2 | Failing tests written, test cases designed | yes | `test_dispatch_names_repository_without_git_checkout` FAIL then PASS |
| 3 | Suite green, coverage held, docs updated | yes | 17 targeted + 319 `workspace/tests`; no Java delta |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending |

## Exceptions

Issue labels / board Status writes remain 403 for fleet tokens (#1551). No
Constitution exception requested for that; a human paste is required.
