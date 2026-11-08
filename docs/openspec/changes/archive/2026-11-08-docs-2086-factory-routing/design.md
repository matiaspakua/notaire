> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`java-architect.md` and `backend-implementer.md` still describe the layered
packages ADR-030 retired. The fleet manifest does not name Impeccable or
`notaire-cybersec`. The Mac worker trigger was unwritten. The Owner then
directed this run to stay on Cursor Cloud and not start a private worker.
The commands are recorded and marked inactive.

## Goals / Non-Goals

**Goals:**

- Make the specialist prompts match ADR-030.
- Record Impeccable provenance and route form review to it.
- Record the local Cursor worker commands and the open hex queue.
- Keep a marker-less WIP label from being released.

**Non-Goals:**

- Product form edits, hexagonal slices, merges, ruleset edits.
- Treating `local-ai/sdlc/foreman.sh` as the Cursor trigger.

## Decisions

- One factory doc for the handoff and one for the form inventory. Product docs stay untouched.
- Do not release #577. `release_stale` prints `no claim marker` and keeps the label. Releasing a marker-less lane would drop in-progress work that has no open PR.
- Do not publish an Impeccable 0–4 score. The rendered audit did not run.
- Branch name is `cursor/factory-phase1-routing-b7c4` because this cloud runtime requires that form. The Constitution pattern `<type>/<issue>_<desc>` is recorded here as the exception for this run.

## Riesgos / Trade-offs

- [Handoff issue list goes stale] → The doc includes the `gh` commands to refresh it and dates the 2026-11-08 measurement.
- [Agents still merge] → This change does not grant merge rights. The Owner merges.
- [Unlabeled #2086] → The integration token cannot add labels (`Resource not accessible by integration`). Human paste is in the PR. The issue is not claimable until it has work labels.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Architect prompt follows ADR-030 and drops the layered mandate | unit | `workspace/tests/test_factory_phase1_routing.py` |
| Backend implementer does not send new code to legacy layers | unit | same |
| Manifest routes ports-and-adapters and names Impeccable and notaire-cybersec | unit | same |
| Handoff names `agent worker start` and the open hex issues | unit | same |
| Forms plan says the rendered audit did not run | unit | same |
| Marker-less WIP is reported and kept | unit | `workspace/tests/test_claim_issue.py` |

- New unit tests: the two files above. Observed failing on the red commit, then passing.
- New integration tests: none. No API change.
- Coverage impact: no Java. JaCoCo floor unchanged.

## Regression Strategy

- Existing tests affected: `workspace/tests/test_fleet_lessons_1858.py` still passes; the foreman and hex rule text it checks was not removed.
- Full suite command: `bash workspace/sdlc/preflight.sh --full` before any push.
- HTTP/Bruno API suite: included in that preflight. No API change.
- Legacy paths at risk: none. No `jpa` or Swing edits.

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a - no UI surface
- Edge / error paths covered: n/a - no UI surface
- Viewports: n/a - no UI surface
- Command: not run for this change. The form plan schedules the rendered pass on #2087.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: docs and agent prompts ship with the image only as files. No runtime flag.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): not this PR. The Owner merges. No deploy is requested.

## Rollback Strategy

- Revert safe: yes. The change is docs, prompts, and two unit tests. Revert restores the old prompts.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: agents may keep following the corrected prompts, which match ADR-030.

## Migration Plan

No staged rollout. After this change is verified, #2087 is the next frontend slice and #577 stays the backend slice. Both run on Cursor Cloud. The Mac commands in `LOCAL-CURSOR-HANDOFF.md` stay recorded and are not executed.

## Open Questions

None that change this change. Label and board Status for #2086 need a human paste because the token cannot write labels.
