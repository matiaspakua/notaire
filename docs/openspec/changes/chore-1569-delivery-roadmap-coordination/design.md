> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The owner-authenticated policy run assigned all issues to milestones, but the
repository still documents the superseded Phase 4/v1.0/v1.1 model. Project #1
has 809 items, the Roadmap is grouped by Milestone but filters out history, and
the Kanban has no filter. GitHub's public Project v2 API can update view names,
layouts, filters, and visible fields, but cannot set group-by fields.

## Goals / Non-Goals

**Goals:**
- Make milestone assignment and Project view configuration idempotent.
- Show history, current ownership, and future trains without catalog noise.
- Give all agent fleets one deterministic selection and claim protocol.
- Keep permanent guidance KIS and DRY.

**Non-Goals:**
- Product backlog decisions, issue closure, and SemVer release planning.
- Additional workflow columns or duplicate lane labels.
- Browser automation around GitHub settings not exposed by the API.

## Decisions

1. **Use one full-lifecycle Roadmap.** Remove `is:open` from its filter and keep
   the three catalog labels excluded. Grouping by Milestone makes the path from
   history to future trains visible without a duplicate history view.
2. **Use Kanban only for execution.** Filter it to open non-catalog work and
   preserve Status as its vertical grouping.
3. **Use a table as the agent queue.** Rename Delivery schedule to Agent queue
   and use the same delivery filter and relevant planning fields.
4. **Derive lanes.** Map existing area/type labels to `backend`, `frontend`,
   `cross`, `platform`, `quality`, `security`, `catalog`, or `epic`; do not
   maintain a second label family.
5. **Make checks read-only.** `--check` verifies assignments and view contracts;
   only explicit `--apply` mutates GitHub.
6. **Centralize prose.** Project-management docs own canonical rules; agent and
   GitHub entry points link to them.

## Riesgos / Trade-offs

- Project group-by is not writable through the public API → verify it in
  `--check`, document the one-time UI setting, and avoid brittle automation.
- Live counts change as work lands → timestamp snapshots; invariants, not frozen
  counts, are normative.
- Updating many project items is rate-limit sensitive → update only drift and
  retain transient-error retries.

## Testing Strategy

<!-- How the scenarios in the delta spec become tests. Every Acceptance Criterion
     (each `#### Scenario:`) must map to at least one test.
     TDD is mandatory (Constitution P1): tests are written and observed FAILING
     before implementation. -->

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Closed issue coverage is complete | unit + live read-only | `workspace/sdlc/test_milestone_policy.py`; `apply_milestone_policy.py --check` |
| Open issue coverage is complete | unit + live read-only | `workspace/sdlc/test_milestone_policy.py`; `apply_milestone_policy.py --check` |
| Roadmap shows history through future planning | unit + live read-only | policy tests and project-view check |
| Kanban remains an operational queue | unit + live read-only | policy tests and project-view check |
| Agent selects the next issue | unit | `workspace/sdlc/test_milestone_policy.py` |
| Agent claims and releases work coherently | regression | `workspace/sdlc/check-board-status-label-coherence.py` |
| Agent or human finds current guidance | docs/link | preflight and markdown lint |
| Release record names a live gate train | unit | `workspace/tests/test_product_release.py` |

- New unit tests: extend `workspace/sdlc/test_milestone_policy.py`.
- New integration tests: none; live checks are read-only GitHub API probes.
- Coverage impact: no Java or frontend production code changes.

## Regression Strategy

<!-- What existing behavior could this break, and how it is proven not to.
     Name the existing test classes that must be updated and why. Never weaken an
     assertion to force green - if an existing test now fails legitimately, say why
     the old expectation was wrong. -->

- Existing tests affected: milestone policy tests restored from PR #1565 and
  product release train mapping tests.
- Full suite command: `bash workspace/sdlc/preflight.sh`.
- HTTP/Bruno API suite: n/a — no Notaire API behavior.
- Legacy paths at risk: none.

## Playwright Strategy

<!-- Mandatory for any UI change. If this change has no UI surface, write
     "n/a - no UI surface" and justify it in one line; do not delete the section. -->

No Notaire product UI changes. The public static roadmap page is verified by
`npm run build` in `github-page`; live Project v2 metadata verifies GitHub
view configuration. The product Playwright suite is n/a because it does not
serve either surface.

## Deployment Strategy

<!-- How this reaches an environment. Order of operations matters when a schema
     migration and code change ship together. -->

- Flyway migration required: no
- Deployment order / coupling: apply idempotent GitHub policy, then publish docs
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy: rerun policy check, board coherence, and view metadata

## Rollback Strategy

<!-- How to undo this if the smoke test or production behavior fails.
     A revert is only a valid rollback if it is actually safe - state whether it is. -->

- Revert safe: yes; docs/scripts and view filters can be restored.
- Database rollback: none needed.
- Data written under the new behavior: milestone and Start/Target metadata.
- Blast radius if rollback is delayed: confusing planning presentation only.

## Migration Plan

1. Prove policy tests fail for missing history windows and view contracts.
2. Extend the policy and read-only checks until tests pass.
3. Apply GitHub policy and preserve required UI-only grouping.
4. Regenerate snapshots and update canonical documentation.
5. Run Gate 3 checks and publish the PR.

## Open Questions

None.
