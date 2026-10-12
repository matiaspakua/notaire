# Traceability — project roadmap

## Chain

| Link | Value |
|------|-------|
| Issue | #1248 OPEN. This change is a slice (grouping and the manager model). It does not complete the per-issue valid/done/duplicate/obsolete acceptance, so the issue stays open. |
| Use Case | CU76, already documented. Process and repository organization. |
| Specification | `docs/openspec/changes/docs-1248-project-roadmap/specs/project-roadmap/spec.md` |
| Branch | `cursor/project-management-sdlc-566d` |
| Commits | pending |
| PR | pending |
| CI | pending |
| Merge | pending |
| Release | pending |
| Smoke | pending |

## Requirement coverage

| Scenario | Test |
|----------|------|
| Assigned history is preserved | `test_closed_issue_keeps_existing_milestone` |
| September close date | `test_closed_unassigned_date_cohorts` |
| July or August close date | `test_closed_unassigned_date_cohorts` |
| Functional requirement | `test_functional_requirement_is_scope_not_delivery` |
| Security non-functional requirement | `test_nfr_catalog_records_delivery_assessment` |
| Frontend slice | `test_frontend_product_slice_is_phase4_even_without_priority` |
| Audit finding | `test_audit_finding_is_the_security_lane` |
| Critical jpa blocker stays on the production gate | `test_phase6_and_jpa_debt_leave_priority_bucket` |

## Permanent documentation updated

| Document | Update |
|----------|--------|
| `docs/300-development/305-project-management/` | Added in this change |
| `docs/github/README.md` | Milestones and labels |
| `docs/200-architecture/201-SAD/sad.md` | §11.3 phase table |
| `docs/300-development/304-ai-sdlc-cloud/` | Fleet role |
| `github-page/app/docs/roadmap/page.tsx` | Chart |

## Gate log

| Gate | State | Evidence |
|------|-------|----------|
| 1 Plan | passed | `openspec validate docs-1248-project-roadmap --strict` and `bash workspace/sdlc/validate-sdlc-plan.sh docs-1248-project-roadmap` |
| 2 Tests red then green | passed | Red: missing `roadmap_classify.py`. Green: `python3 -m unittest workspace.tests.test_roadmap_classify` (16 tests) |
| 3 Docs and preflight | pending | |
| 4 CI and review | pending | |
| 5 Smoke and close | pending | #1248 stays open; see Exceptions |

## Exceptions

- **Do not `Closes #1248`.** The issue's acceptance criteria require a verified outcome on every open issue. This change groups milestones and lanes and leaves that triage open. Commits and the PR use `Refs #1248`.
- **Do not add `in-progress` on #1248** from this run. The issue is not claimed as product work.
- **GitHub board is not mutated.** `--apply` does not edit milestones or labels. Documented in ROADMAP.md.
