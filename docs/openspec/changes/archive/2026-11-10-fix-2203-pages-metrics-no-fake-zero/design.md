> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`workspace/ci/generate-github-page-metrics.sh` is the single generator for Pages deploy and the committed `github-page/public/metrics.json`. Today, when the JaCoCo snapshot is missing it calls `live_field … 0`, so a missing live file or a live file already poisoned with `0` + `lastUpdated: "unknown"` republishes a measured-looking zero. #2201 taught the Results UI to show `n/a` for `unknown`, but the generator still writes `0`.

## Goals / Non-Goals

**Goals:**
- Stop publishing numeric `0` for coverage percentages when measurement artifacts are missing.
- Preserve reuse of a *measured* live fallback when the snapshot is absent for one run.
- Lock the contract with an automated test of the shared script; keep Pages UI honest for `null`.

**Non-Goals:**
- Inventing coverage when no report exists; raising JaCoCo floors; computing real `e2eTests` / `apiEndpoints` (#760).

## Decisions

1. **JSON `null` over omitting keys or string `"n/a"`** — jq consumers and TypeScript can type `number | null`; omitting keys would silently fall through to DEFAULTS (32/19/…) in `loadMetrics`, which is worse than a fake zero. Alternative rejected: keep `0` and rely only on UI `unknown` (already insufficient because charts still show 0%).
2. **Trust live reuse only when `lastUpdated ≠ "unknown"`** — a live zero with an unknown stamp is treated as unmeasured and replaced with `null`. Alternative rejected: reuse any live number (continues the poison chain seen on 2026-11-10).
3. **Same shared script for committed JSON and Pages deploy** — regenerate committed `metrics.json` through the script (or a fixture-equivalent path in test); do not hand-edit conflicting numbers. Alternative rejected: dual logic in the workflow YAML (#758 lesson).

## Riesgos / Trade-offs

- [Charts briefly show empty/`n/a` until the next deploy that has real artifacts] → Mitigation: UI already has `coverageIsMeasured`; extend it for `null`; Numbers page copy stops claiming a shown 0% is the fallback once zeros are gone.
- [`loadMetrics` DEFAULTS merge could resurrect demo percentages if keys are omitted] → Mitigation: always emit keys with JSON `null`, never omit.
- [True measured 0% would be indistinguishable from poison if stamped unknown] → Mitigation: accept; a real 0% with a dated stamp still publishes 0; unknown stamp never claims measurement.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Missing snapshot and missing live do not publish zero coverage | unit (script) | `workspace/tests/test_generate_github_page_metrics.py` |
| Poisoned live zeros with unknown stamp are not copied forward | unit (script) | `workspace/tests/test_generate_github_page_metrics.py` |
| Present JaCoCo snapshot updates backend fields | unit (script) | `workspace/tests/test_generate_github_page_metrics.py` |
| Present frontend summary updates frontendComponent | unit (script) | `workspace/tests/test_generate_github_page_metrics.py` |
| Measured live fallback reused when snapshot missing | unit (script) | `workspace/tests/test_generate_github_page_metrics.py` |
| UI treats null / unknown as not measured | unit (Vitest) | `github-page/tests/metrics-coverage-honesty.test.ts` |

- New unit tests (`src/test/java/.../unit/`): n/a — no Java surface
- New integration tests (`src/test/java/.../integration/`): n/a
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): none expected

## Regression Strategy

- Existing tests affected: `github-page/tests/metrics-test-counts.test.ts` (types may allow `null`); Numbers/Results copy tests if present; `workspace/tests/test_scripts_layout.py` path mapping unchanged
- Full suite command: n/a for backend — `python3 -m unittest workspace.tests.test_generate_github_page_metrics` and `cd github-page && npm test`
- HTTP/Bruno API suite: n/a — no API surface
- Legacy paths at risk: none

## Playwright Strategy

n/a — no product UI surface (`frontend/` / `testing/e2e`). Portfolio pages are covered by Vitest unit tests under `github-page/tests/`; no Playwright golden path for this honesty fix.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge → Pages deploy runs the same generator; next live `metrics.json` should stop showing fabricated zeros once artifacts are still missing
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open live `/metrics.json` and confirm unmeasured backend fields are `null` (or real numbers if the deploy had a snapshot), never a fresh poison of `0` + `unknown` invented by the generator defaults

## Rollback Strategy

- Revert safe: yes — revert the PR; previous generator behavior returns (including fake zeros)
- Database rollback: none needed
- Data written under the new behavior after revert: only static Pages JSON
- Blast radius if rollback is delayed: portfolio shows `n/a` / null instead of misleading 0% (acceptable)

## Migration Plan

1. Land failing script + Vitest honesty tests.
2. Patch generator + TS/UI.
3. Regenerate committed `metrics.json` with the script (fixture live URL or post-fix pass).
4. Archive OpenSpec change in the PR.

## Open Questions

None — Issue acceptance criteria define the honesty contract.
