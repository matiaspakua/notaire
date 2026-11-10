> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Cloud agents on ASLI-60 / #2119 failed `gh issue edit --add-label` with
`addLabelsToLabelable` 403. The ambient Cursor integration can open PRs but lacks
Issues write. `notaire-fleet` (#1648) already documents the correct App permissions,
but Cloud runs often have no `FLEET_APP_*` secrets mid-run.

## Goals / Non-Goals

**Goals:** (a) short durable docs on required scopes + where the Owner grants them;
(b) honest SKIP in the Gate 1.6 path so a 403 does not fake success or block Gate 1;
(c) TDD proof.
**Non-Goals:** Granting Cursor dashboard scopes from the agent; product code; reopening
#2217/#2218; Foreman stage knobs; Wilson; Portal.

## Decisions

1. **Helper script over silent catch** — `workspace/sdlc/add-issue-wip-label.sh` is the
   single Gate 1.6 entry: resolve `status:in-progress` (taxonomy), call `gh`, on
   permission errors print `SKIPPED` and exit 0; on success print `✓ … labeled`.
2. **Docs + Owner offline grant** — agents cannot widen App scopes mid-run; document
   Cursor App Issues Read & write **or** `FLEET_APP_ID` / `FLEET_APP_PRIVATE_KEY`.
3. **Skill/template point at helper** — replace raw `gh issue edit --add-label` in
   ai-agent-workflow and the notaire-sdlc tasks template so new plans inherit honesty.
4. **KISS** — no validate-sdlc-plan network call; no fake "in progress" Project Status.

## Riesgos / Trade-offs

Board Status may stay Todo while work proceeds until the Owner grants label write —
acceptable vs lying about WIP. Agents that ignore the skill and call raw `gh` still
see the same 403; the helper is the taught path.

## Testing Strategy

`workspace/tests/test_add_issue_wip_label.py`: fake `gh` on PATH for 403 → SKIPPED /
exit 0 / no `✓`; success → labeled; docs assert `issues: write` / `addLabelsToLabelable`
and Owner grant wording; skill/rule/template name the helper. RED then GREEN.

## Regression Strategy

`test_fleet_token.py` and board/label taxonomy tests unchanged. Live smoke:
`bash workspace/sdlc/add-issue-wip-label.sh 2221` must SKIP under ambient Cursor auth.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs/scripts/skills only; Owner may add fleet secrets on the Environment card later.

## Rollback Strategy

Revert the PR.

## Migration Plan

None. Existing open OpenSpec plans may still say raw `gh`; new seeds use the template.

## Open Questions

None.
