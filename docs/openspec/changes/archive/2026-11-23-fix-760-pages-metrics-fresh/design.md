> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`generate-github-page-metrics.sh` currently falls back to the last live `e2eTests` and `apiEndpoints` via `coverage_fallback()` (lines 108–109) with no way to distinguish fresh from stale. `generate_e2e_coverage_report.py` already parses both Playwright `results.json` (stats.expected + unexpected + skipped + flaky) and Bruno `bruno-results.json` (summary.totalRequests) — but only emits a Markdown report. The Coverage UI component (`Coverage.tsx`) displays these values with no stale indicator.

## Goals / Non-Goals

**Goals:**
- Compute `e2eTests` fresh from Playwright results on every metrics generation run.
- Compute `apiEndpoints` fresh from the Bruno report on every metrics generation run.
- When computation fails, fall back to the last live value but mark it stale.
- Display stale values with a visible indicator in the Coverage UI.

**Non-Goals:**
- Changing how JaCoCo or Vitest coverage is computed (already fresh).
- Adding new Playwright or Bruno tests.
- Modifying the GitHub Pages deployment workflow.

## Decisions

1. **Add `--json` flag to `generate_e2e_coverage_report.py`** rather than a separate script. Rationale: the parsing logic (`_playwright_section`, `_bruno_section`) is already there; a `--json` mode reuses it and avoids duplicating artifact discovery. Alternative considered: a dedicated Python one-liner in the shell script — rejected because it would duplicate the Bruno list-of-iterations shape handling (issue #658).

2. **Stale flag as a top-level `staleFields` array in `CoverageSnapshot`** (e.g., `["e2eTests"]`) rather than per-field objects. Rationale: simpler JSON shape, easier to extend if more fields need staleness tracking. The UI checks membership in the array.

3. **Shell script calls `generate_e2e_coverage_report.py --json` and parses the output with `jq`**. On failure (non-zero exit, missing artifact, parse error), falls back to `coverage_fallback` and appends the field name to `staleFields`.

## Riesgos / Trade-offs

- [Risk] Playwright/Bruno artifacts may not be present in the GitHub Pages deployment workflow context (they are CI artifacts, not checked in). → Mitigation: the script already handles missing artifacts gracefully via `coverage_fallback`; the `--json` mode will return exit code 1 when artifacts are missing, which the shell script catches and falls back to stale.
- [Risk] Bruno report shape changes (already happened in #658). → Mitigation: `_bruno_summaries` already handles both list and dict shapes; tests cover the unrecognized shape case.
- [Risk] The `staleFields` array could grow unbounded if many fields use it. → Mitigation: it's a small array of field name strings; current scope is 2 fields.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fresh e2eTests from Playwright results | unit | `workspace/tests/test_generate_github_page_metrics.py` |
| Fresh apiEndpoints from Bruno report | unit | `workspace/tests/test_generate_github_page_metrics.py` |
| Stale fallback when Playwright artifact missing | unit | `workspace/tests/test_generate_github_page_metrics.py` |
| Stale fallback when Bruno artifact missing | unit | `workspace/tests/test_generate_github_page_metrics.py` |
| `--json` flag outputs correct JSON | unit | `testing/tests/test_generate_e2e_coverage_report.py` |
| `--json` flag handles missing artifacts | unit | `testing/tests/test_generate_e2e_coverage_report.py` |
| UI renders stale badge | unit | `github-page/components/Coverage.tsx` (existing test suite) |

- New Python unit tests in `workspace/tests/test_generate_github_page_metrics.py` and `testing/tests/test_generate_e2e_coverage_report.py`.
- No Java tests — this change touches no backend code.
- Coverage impact: Python script tests; TypeScript component tests (Vitest).

## Regression Strategy

- Existing tests affected: `test_generate_github_page_metrics.py` — `test_measured_live_fallback_reused_when_snapshot_missing` currently checks `e2eTests` and `apiEndpoints` fallback; it will need to assert the new `staleFields` array contains those keys when fallback is used.
- `test_generate_e2e_coverage_report.py` — existing tests exercise `build_report` (Markdown mode); new tests exercise the `--json` mode. No existing test should break.
- Full suite command: `python3 -m unittest workspace.tests.test_generate_github_page_metrics` and `python3 testing/tests/test_generate_e2e_coverage_report.py`.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface change that affects user-facing flows. The Coverage component renders on a static metrics page; the stale badge is a visual indicator, not a new interaction. The component change is covered by Vitest unit tests.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — the metrics JSON schema change (`staleFields`) is backward-compatible (new optional field). The Next.js build reads the JSON at build time.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Verify `github-page/public/metrics.json` contains `e2eTests` and `apiEndpoints` with correct values and `staleFields` when applicable.

## Rollback Strategy

- Revert safe: yes — the `staleFields` field is optional; old code that doesn't read it will simply ignore it. The shell script fallback behavior is unchanged for fields that don't use the new logic.
- Database rollback: none needed
- Data written under the new behavior after revert: none — only `metrics.json` is affected, which is regenerated on next deploy.
- Blast radius if rollback is delayed: minimal — at worst, the Coverage UI shows stale values without the badge for one deploy cycle.

## Migration Plan

Not applicable — no staged rollout needed.

## Open Questions

None.
