> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See proposal.md. GitHub already has ten milestones, a ROADMAP view with an empty
filter, and 219 open issues with no milestone. The policy is a pure function so
it can be tested without the API. Applying it is a separate script because this
agent's token cannot mutate milestones.

## Goals / Non-Goals

**Goals:**

- One function decides the milestone and the lane.
- The handbook and the Pages chart show the same counts.
- A later `--apply` run is idempotent.

**Non-Goals:**

- Closing duplicate use-case issues.
- Adding a `lane:*` label that would drift from the derived rule.

## Decisions

- Milestone means schedule; `priority:*` stays the urgency label. Alternative
  considered: keep v1.0 / v1.1 / Backlog as the only milestones. Rejected because
  the roadmap then cannot show frontend versus security versus legacy.
- Catalog issues get a milestone and a non-pickable lane. Alternative considered:
  leave them unmilestoned, which is the previous rule. Rejected because the PM
  asked for requirements and NFRs to be assessed onto trains.
- History eras use close date, not a guess at the phase label. Phase labels are
  unused (zero to twelve issues). Alternative considered: dump all 388 closed
  issues into the small curated phase milestones. Rejected because those
  milestones were a curated subset, not the whole history.
- Lanes are derived. Alternative considered: six new labels. Rejected because
  they duplicate area labels and go stale.

## Riesgos / Trade-offs

- [Token cannot apply the GitHub edit] → The handbook states the 403 and the
  exact command. `--check` fails until a privileged token runs `--apply`.
- [Frontend train count is dominated by catalog anchors] → Roadmap filter hides
  the three catalog labels. The chart says so.
- [Three issues are already in progress on overlapping lanes] → The foreman
  must not start another frontend, backend, or cross issue until one closes.
  This change does not remove their `in-progress` labels.
- [Group-by on the roadmap is not in the GitHub API] → The script sets the
  filter when it has permission. A person sets Group by Milestone once.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Security non-functional requirement is a release blocker | unit | `ClassifyOpenTests.test_security_rnf_is_a_release_blocker` |
| Functional requirement is frontend product scope | unit | `ClassifyOpenTests.test_functional_requirements_and_use_cases_are_frontend_scope` |
| Critical legacy defect is pulled forward | unit | `ClassifyOpenTests.test_critical_preempts_legacy_theme` |
| Low priority does not enter an earlier train | unit | `ClassifyOpenTests.test_low_priority_wins_over_area` |
| Close date selects the era | unit | `ClassifyClosedTests.test_groups_unmilestoned_history_by_close_date` |
| Requirements stay off the pick queue | unit | `LaneTests.test_catalog_and_epics_are_not_pickable` |
| Cross work blocks both product lanes | unit | `LaneTests.test_cross_and_security_block_the_backend_lane` |

- New unit tests: `workspace/sdlc/test_milestone_policy.py`
- New integration tests: none (GitHub apply needs a privileged token)
- TDD: the test module was run before `milestone_policy.py` existed and failed
  on import. It then passed (29 tests).
- Coverage: this is Python under `workspace/`, outside the JaCoCo ratchet.

## Regression Strategy

No product tests change. Existing board coherence checker is untouched.
SAD links to milestones #6, #7, #8 and #10 are removed because those milestones
404; the replacement is the handbook.

## Playwright Strategy

n/a — no product UI surface. The GitHub Pages chart is a static docs route in
`github-page/`, verified by a production build of that app and a browser check
of `/docs/management/`, not by the product Playwright suite.

## Deployment Strategy

Docs and the Pages app ship with the normal main deploy. No Flyway, no env keys,
no feature flag. Smoke for Gate 5 is the Pages route after merge, plus
`python3 workspace/sdlc/apply_milestone_policy.py --check` once a human token
has run `--apply`. Until then `--check` is expected to fail.

## Rollback Strategy

Revert the commit. GitHub is unchanged by this branch except that issue #1561
exists. Reverting the docs does not require a data migration. If a later
`--apply` has run, re-running is idempotent; reversing milestone titles would
be a follow-up, not an automatic revert.
