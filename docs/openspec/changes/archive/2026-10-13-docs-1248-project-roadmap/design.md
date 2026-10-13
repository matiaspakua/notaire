## Context

See proposal.md — Objetivo. The September 2026 audit scheduled open work only
by priority. 607 of 799 issues have no milestone. SAD §11.3 still linked
deleted milestone numbers.

## Goals / Non-Goals

**Goals**

- One pure function from issue to milestone, lane, and claimable flag.
- A dry-run snapshot the Pages chart and the manager guide both render.
- A project-manager agent the foreman consults before claiming a lane.

**Non-Goals**

- Writing milestones back to GitHub in this change.
- Closing #1248.

## Decisions

1. **Classifier is a pure module.** `roadmap_classify.py` has no `gh` calls. The script only fetches and prints. Alternative considered: a shell of `gh issue edit` inside the agent. Rejected because this token must not mutate the board and the rules need unit tests.
2. **Three milestone kinds.** History, delivery, and scope. Catalog issues stay off delivery trains so progress percentages can reach 100%. Alternative considered: put every RF on Phase 4. Rejected because those issues stay open by policy and would pin Phase 4 below done forever.
3. **July–August frontend history is its own closed milestone.** Mixing it into the open Phase 4 train made the closed share look like 90% while 15 screens remain. Alternative considered: one Phase 4 milestone for both. Rejected for the progress chart.
4. **Lane is the lock, not the module map alone.** `lane:fullstack` blocks backend and frontend. `lane:security` wins over area labels. Alternative considered: only `python3 workspace/modules.py affected`. That still allows two backend issues at once.
5. **Due dates are sequence markers** copied from milestones that already had dates, plus quarter markers so a roadmap view can draw bars. They are not effort estimates.

## Riesgos / Trade-offs

- [Live board stays on the old priority buckets until a maintainer applies ROADMAP.md] → The Pages chart is labeled as the target layout and the guide says the board is unchanged until apply.
- [Date cohorts mis-file a closed issue that finished early or late] → Phase labels override the date. Issues that already have a milestone are not moved.
- [lane:owner holds real work that only lacked labels] → Those issues are visible and not auto-closed. #1248 still requires a human decision.
- [Critical JPA work stays on v1.0 while other jpa retirement moves to Phase 6] → Documented. Priority critical remains the production gate.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Assigned closed milestone preserved | unit | `test_closed_issue_keeps_existing_milestone` |
| September and July/August cohorts | unit | `test_closed_unassigned_date_cohorts` |
| Phase label overrides date | unit | `test_closed_unassigned_uses_phase_label_before_date` |
| RF and CU scope | unit | `test_functional_requirement_is_scope_not_delivery`, `test_use_case_catalog_is_scope` |
| RNF theme and train | unit | `test_nfr_catalog_records_delivery_assessment` |
| Frontend, audit, Phase 6, critical JPA | unit | `test_frontend_product_slice_is_phase4_even_without_priority`, `test_audit_finding_is_the_security_lane`, `test_phase6_and_jpa_debt_leave_priority_bucket` |
| Snapshot internal consistency | unit | `test_snapshot_matches_pages_copy` |

TDD: `workspace/tests/test_roadmap_classify.py` was run before `roadmap_classify.py` existed and failed with `FileNotFoundError`. JaCoCo ratchet: n/a, no Java.

## Regression Strategy

No product tests change. `python3 -m unittest workspace.tests.test_roadmap_classify` is the regression for this module. Existing board-coherence tests are untouched.

## Playwright Strategy

n/a — no product UI surface. The Pages Docs tab is a documentation site. The route is guarded by the unit test that reads `github-page/app/docs/roadmap/page.tsx` and `DocsChrome.tsx`. Viewports are not exercised here.

## Deployment Strategy

No Flyway migration, no `.env` keys, no feature flag. The Pages site updates when the docs PR merges and the existing `deploy-github-page.yml` workflow publishes `github-page/`. Smoke for this change is the unit test plus `bash workspace/sdlc/validate-sdlc-plan.sh docs-1248-project-roadmap`.

## Rollback Strategy

Revert the PR. No schema and no data migration. GitHub milestone state is unchanged by this change, so revert does not strand board data. The Pages chart disappears with the revert.

## Migration Plan

1. Land the classifier, guide, agent, and chart.
2. A maintainer with a write token follows `ROADMAP.md` to create milestones, lane labels, and project views.
3. Re-run the dry-run and refresh the snapshot after apply so the chart matches GitHub.

## Open Questions

None that change the spec. Whether to close individual stale issues is #1248, outside this change.
