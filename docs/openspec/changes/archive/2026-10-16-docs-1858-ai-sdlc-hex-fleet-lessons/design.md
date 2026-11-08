> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Epic #1693 produced repeat process failures: OpenSpec archive / Process Checks
blocking ready, Spotless / INVENTORY regen on merge-main, CHANGELOG Unreleased
collisions with Release Please, Mac preflight `gh` flakes, FE suppressions
ratchet ambiguity, R2 PRs that mixed domains or left entities on ports, bots
unable to close issues (403), and agents starting the next domain before the
current PR merged. Lessons sat in Project-store notes; agents load
`AGENTS.md`, `.claude/skills/`, `304-ai-sdlc-cloud/`, and steward/foreman.

## Goals / Non-Goals

**Goals:**
- Put #1693 do/don't into those always-or-on-demand load paths.
- Retry transient `gh` issue lookup in `validate-sdlc-plan.sh`.
- Keep KISS: checklists and pointers; no second Constitution.

**Non-Goals:**
- Product hex code, baseline edits, or FE suppressions content changes.
- Closing orphaned board issues (#1831–#1834) from here (paste pattern only).

## Decisions

1. **Two new permanent docs under `304-ai-sdlc-cloud/`** — one fleet-lessons
   playbook, one Mac-vs-CI ownership matrix — indexed from README and linked
   from steward / foreman / workflow (restatement + link, not copy-paste essays).
2. **Extend steward and CI-MERGE-GATE** — agents already open these on every PR;
   add CHANGELOG fence, sync-after-land, Process Checks before `gh pr ready`.
3. **Hex rule for R2 shrink** — domain views on ports, one domain per PR, FE
   suppressions done = `eslint-suppressions.json` is `{}` / count 0.
4. **`gh` retry (3 attempts, short backoff)** in `check_issue_live` — same false
   "Issue not found" seen on Mac tip `01eaf983`; do not weaken the check.
5. **`skip_specs: true`** — no product requirement delta; CU76 process only.

## Riesgos / Trade-offs

- [Docs drift from practice] → Guard tests cite paths and required steward needles;
  Prefer linking over duplicating long prose.
- [Agents still skip reading new docs] → Index in README + AGENTS.md one-liner +
  steward pre-ready checklist that names the file.
- [Retry hides real missing issues] → Cap retries; empty state after retries still
  fails; closed-issue inventory path unchanged.

## Testing Strategy

| Scenario (acceptance) | Test level | Test class / file |
|-----------------------|------------|-------------------|
| Steward still cites merge-main / never-rebase / heavy CI | unit | `workspace/tests/test_steward_skill.py` |
| New fleet-lessons + Mac-vs-CI paths exist and are indexed | unit | `workspace/tests/test_fleet_lessons_1858.py` (new) |
| `validate-sdlc-plan.sh` still rejects leftover templates; retry is present | unit | existing `test_validate_sdlc_plan.py` + assert retry marker |

- New unit tests (`workspace/tests/`): `test_fleet_lessons_1858.py`
- New integration tests: none
- Coverage impact: n/a (harness Python, not JaCoCo)

## Regression Strategy

- Existing tests affected: `test_steward_skill.py` (extend required topics)
- Full suite command: `python3 -m unittest discover -s workspace/tests -p 'test_*1858*.py' -p 'test_steward*.py' -p 'test_validate_sdlc_plan.py'`
- HTTP/Bruno API suite: n/a — docs/process only
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface (process/docs/scripts only).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with docs-only PR; path-scoped CI (#1257)
  skips Java/E2E leaves
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Process Checks green; `check-heavy-ci.sh`
  treats skipped heavy leaves as success for docs-only

## Rollback Strategy

- Revert safe: yes — docs + script retry only
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: agents may miss lessons; no runtime impact

## Migration Plan

None beyond merging this PR and archiving the OpenSpec change.

## Open Questions

None — evidence from Mac validation + hex lead status + gap assessment after #1846.
