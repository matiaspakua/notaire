> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#1859 landed durable fleet lessons. Post-merge R2 PRs still hit Process Checks
on stale `docs/tests` paths after moving services into
`adapter.out.persistence.*` (#1894), burned CI minutes on parallel heavy tips,
and lacked copy-paste 403 templates. Agents already load FLEET-LESSONS /
steward / foreman — extend those, do not invent a second framework.

## Goals / Non-Goals

**Goals:**
- Encode post-#1859 deltas into the same load paths as #1859.
- Harden `validate-sdlc-plan.sh` retries under multi-issue preflight load.
- Guard tests that fail if path hygiene / paste templates / cost / Mac≠CI wording regress.

**Non-Goals:**
- Product hex domains, Dockerfile rewrites, CONSTITUTION rewrite.
- Re-shipping #1859 prose wholesale.

## Decisions

1. **Extend existing FLEET-LESSONS** rather than a new sibling file — agents already
   open one lessons doc; delta sections keep discoverability.
2. **Paste templates as fenced `text` blocks** — copy-paste ready for humans.
3. **Retry cap 5 + `sleep attempt*2`** — Mac saw flakes when many leftovers were
   checked; keep hard fail after retries so inventing issues still fails.
4. **`skip_specs: true`** — process/docs; ACs live in Issue #1907 + proposal.
5. **CHANGELOG Unreleased Documentation bullet** — docs-visible process change;
   fence is free per fleet notes.

## Riesgos / Trade-offs

- [Duplicating #1859] → Evidence checklist marks already-covered vs new; guard
  tests assert delta needles only.
- [Longer gh retries slow CI] → Cap at 5; sleep only on empty state; success path
  unchanged.
- [CHANGELOG Unreleased conflict] → Single writer for this tip; omit if fence taken.

## Testing Strategy

| Scenario (acceptance) | Test level | Test class / file |
|-----------------------|------------|-------------------|
| R2 path hygiene + paste templates + cost in lessons | unit | `workspace/tests/test_fleet_lessons_1907.py` |
| Mac ≠ CI green wording | unit | `test_fleet_lessons_1907.py` |
| Dockerfile already has `-U` (no re-ship) | unit | `test_fleet_lessons_1907.py` |
| `validate-sdlc-plan.sh` retry hardened | unit | `test_fleet_lessons_1907.py` |
| Steward still merge-main / never-rebase | unit | `workspace/tests/test_steward_skill.py` (existing) |

- New unit tests: `test_fleet_lessons_1907.py`
- New integration tests: none
- Coverage impact: n/a (harness Python)

## Regression Strategy

- Re-run `test_fleet_lessons_1858.py` + `test_steward_skill.py` so baseline lessons
  still hold.
- `bash workspace/sdlc/validate-sdlc-plan.sh docs-1907-ai-sdlc-fleet-lessons-delta`
- Docs-only path-scoped CI; Bruno/Playwright expected skip.

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
- Blast radius if rollback is delayed: agents may miss delta lessons; no runtime impact

## Migration Plan

None beyond merging this PR and archiving the OpenSpec change.

## Open Questions

None — Docker `-U` confirmed on `main`; #1858 closed → new issue #1907.
