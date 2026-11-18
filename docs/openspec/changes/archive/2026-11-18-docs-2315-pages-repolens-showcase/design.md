> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

RepoLens merged in #2303; #2311 CLI usage telemetry; #2314 ambiguous same-directory call edges. Bench artifacts live under framework/ai-sdlc/repolens/bench/results/. Owner asked for one PR; only Matias merges; do not open extra issues for findings.

## Goals / Non-Goals

**Goals:** One honest RepoLens showcase page with charts tied to committed benches, wired into factory nav/home.
**Non-Goals:** Re-running benches; product docs; new GitHub issues for incidental findings.

## Decisions

1. **Place RepoLens under Act IV (after Results).** It is evidence of the factory tool and the learning loop, before the kit adoption pages.
2. **Build-time snapshot from committed JSON/MD into github-page/lib.** Same honesty pattern as delivery-metrics / ai-sdlc-history; prebuild regenerates so charts stay current without inventing numbers.
3. **Use report.py headline labels (4.9x, 2.5x, 43x, 91x) only when unit tests pin the raw JSON numerators/denominators.** Rounded markdown tables alone are not the source of truth.
4. **Interactive SVG/HTML charts in site style**, not only embedding static bench SVGs, so dark mode tokens apply; static SVGs may be linked as provenance.

## Riesgos / Trade-offs

- [Stale snapshot] → prebuild/generate script + vitest against JSON paths
- [Over-claiming factory-loop percentages] → show paired-sample n and explicit caveats from factory-loop-comparison.md
- [i18n gaps] → TRANSLATED_ROUTES + pair() coverage tests

## Testing Strategy

`RepoLensPagesTest` (vitest) covers navigation, metric honesty against committed JSON, and locale route registration. Pages Playwright/axe remain in preflight --full.

## Regression Strategy

bash workspace/sdlc/preflight.sh --full

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
