> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`.github/workflows/e2e-swing.yml` is already absent; Swing Maven modules are
gone (#1046). Leftovers are `testing/e2e-swing/` Robot assets and live docs /
scripts that still instruct operators to build or run Swing E2E. Hygiene today
guards module absence but not workflow reintroduction or Swing Maven builds in
YAML.

## Goals / Non-Goals

**Goals:**

- Make Swing E2E retirement durable via failing hygiene if the workflow or Swing
  Maven builds return.
- Hard-deprecate Robot suite in place without CI wiring.
- Align live docs and CU76 / CHANGELOG with RETIRE decision.

**Non-Goals:**

- Rebuild Swing client
- Port Robot → Playwright
- Rewrite `docs/000-archive/` beyond link fixes
- Delete `testing/e2e-swing/requirements.txt` (still needed by #1050 ignore-rule assert)

## Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Disposition | RETIRE (not rebuild) | ADR-012 + ADR-005; Next.js is active client |
| Suite location | Hard-deprecate in place | Smaller diff than archive move; avoid link blast radius |
| Hygiene home | Extend `scripts/test_dependabot_hygiene.py` | Already guards Swing module absence (#1046) |
| Workflow scan | All `.github/workflows/*.yml` | Catch any workflow that rebuilds Swing |
| Docs language | English in touched live docs | Project English-only rule on refactor touch |

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Archive docs still mention Swing E2E | Leave historical; live docs only |
| Operators try `run_tests.sh` locally | README + script early exit with deprecation message |
| Hygiene false positive on historical comments in YAML | Match concrete patterns: path `e2e-swing.yml`, `-pl frontend-swing`, `-pl deprecated-frontend-swing`, module names in run steps |

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| e2e-swing workflow absent | unit (stdlib) | `scripts/test_dependabot_hygiene.py` |
| workflows do not build Swing modules | unit (stdlib) | same + synthetic fixture fail |
| e2e-swing suite hard-deprecated | unit / file assert | hygiene or README presence assert |
| tip of branch green | unit | run hygiene on repo root |

- New unit tests: extend `DependabotHygieneTest` with Swing E2E retirement asserts;
  prove red with a temporary synthetic workflow fixture, then green on tip.
- New integration tests: n/a
- Coverage impact: n/a (Python hygiene scripts; no JaCoCo)

## Regression Strategy

- Existing tests affected: `test_repo_hygiene.py` still expects
  `testing/e2e-swing/requirements.txt` present and not gitignored — keep file.
- Full suite command for this change: `python3 scripts/test_dependabot_hygiene.py`
  and `python3 -m unittest discover -s scripts/tests -p 'test_*hygiene*.py'`
- Backend `mvn verify` not required for docs/hygiene-only; still run
  `bash scripts/preflight.sh` before push.
- Legacy paths at risk: none (Swing modules must stay absent).

## Playwright Strategy

- n/a — no UI surface. Active E2E remains Playwright under `frontend/tests/e2e/`.
- Specs to add/update: none
- Command: not required for this change

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge-only; hygiene runs in CI via unittest discover
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): confirm no `e2e-swing.yml` on `main`; hygiene green

## Rollback Strategy

- Revert safe: yes (docs + hygiene + deprecation README)
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: operators briefly see hard-deprecation notice

## Migration Plan

1. Gate 1 OpenSpec + validate
2. TDD: add hygiene asserts; prove fail with synthetic `e2e-swing.yml` fixture
3. Hard-deprecate `testing/e2e-swing/`; clean live docs; CU76 + CHANGELOG
4. Preflight / hygiene green; PR with `Closes #811`

## Open Questions

None — RETIRE decision is fixed by the issue assignment and ADR-012.
