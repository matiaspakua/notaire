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
| Issue | #1048 | open → implement |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists; AC updated |
| Related | #701 CLOSED; #1057 merged (`a70827ae` / PR #1150); audit-2026-09 | referenced |
| Specification | `openspec/changes/fix-1048-eslint-blocking/` | Gate 1 complete |
| Branch | `cursor/fix-1048-eslint-blocking-69d3` | created from `origin/main` |
| Tasks | `tasks.md` | implement in progress |
| Commits | `f993aacc` docs(openspec); `3df5c805` ci(frontend); `6f9a8098`+ docs(openspec) SHA/PR ledger | recorded |
| Pull Request | [#1152](https://github.com/matiaspakua/notaire/pull/1152) | draft |
| CI run | heavy gate via `scripts/check-heavy-ci.sh` | pending |
| Merge commit | coordinator after heavy CI exit 0 | pending |
| Release / tag | — | pending |
| Smoke test | Frontend CI ESLint blocking on tip | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| ESLint step fails the job on lint errors | `test_eslint_step_is_not_continue_on_error` | covered |
| Clean tree passes blocking ESLint | `npm run lint` exit 0 | covered |
| Preflight maps ESLint as blocking in CI | `test_map_documents_blocking_eslint_in_ci` | covered |
| Preflight and CI share max-warnings=0 semantics | `test_preflight_and_package_share_max_warnings_zero` | covered |
| jsx-a11y rules are enabled | `test_eslint_config_extends_next_core_web_vitals` | covered |
| a11y violation fails blocking lint | `test_a11y_violation_fails_blocking_lint` | covered |
| Obsolete #701 continue-on-error comment removed | `test_obsolete_issue_701_advisory_comment_removed` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | (same PR) |
| `docs/300-development/CI-PREFLIGHT.md` | yes | (same PR) |
| `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` | yes | (same PR) |
| `scripts/preflight.sh` MAP comment | yes | (same PR) |
| `CHANGELOG.md` | yes | (same PR) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this folder; `validate-sdlc-plan.sh` PASS |
| 2 | Failing tests written, test cases designed | yes | red: 4 FAIL on pre-change tree; then green |
| 3 | Suite green, coverage held, docs updated | yes (local) | unittest 9 OK; `npm run lint` 0; docs updated |
| 4 | CI green, review approved, no conflicts | pending | draft PR + heavy CI |
| 5 | Deployed, smoke test passed, Issue closed | pending | coordinator merge |

## Exceptions

- Issue label `in-progress`: GraphQL ACL 403 for integration token (expected).
- Merge: coordinator only after `bash scripts/check-heavy-ci.sh <pr>` exit 0.
