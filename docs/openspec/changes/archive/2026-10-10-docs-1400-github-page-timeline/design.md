> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`github-page/` is a Next.js static marketing site deployed by
`.github/workflows/deploy-github-page.yml` after green CI on `main`.
`components/Timeline.tsx` currently ends at May–August 2026 ("Production Ready").
Metrics JSON is regenerated in CI; this change does not invent new coverage numbers.

## Goals / Non-Goals

**Goals:**
- Append Sept–Oct 2026 progress to the public timeline and related AI narrative.
- Keep every prior timeline era intact.
- Ship via draft PR for #1400.

**Non-Goals:**
- Redesign the site visual language.
- Change deploy workflow, metrics pipeline, or backend/frontend product code.
- Close board hygiene issues (agent lacks Issues write / Project write).
- Pick up #1197.

## Decisions

1. **Append-only timeline** — add new `events` entries after May–August 2026 instead of rewriting earlier copy, so decade narrative stays continuous.
2. **Light AIEra / AITools touch** — extend the workflow banner and add Cursor Cloud to the tools list so the site matches the current agent fleet without replacing Claude Code as Core.
3. **FunFacts schema challenge** — keep the historical challenge card and append a one-sentence "Resolved" note (Flyway sole source of truth) rather than deleting it.
4. **Docs-only OpenSpec** — capability `github-pages-timeline`; verification is content review + `github-page` build, not JUnit/Playwright product suites.

## Riesgos / Trade-offs

- [Stale commit counts on new timeline cards] → Use approximate, clearly narrative figures; CI metrics remain authoritative for Stats/Coverage.
- [Issue #1399 probe stub left open] → Document admin close; create used #1400 with full body because `updateIssue` is 403 for the integration token.
- [Spanish board view names / FRONTEND label still say Swing] → Out of PR scope; admin checklist in the sanity report.

## Testing Strategy

- Docs/site: `npm ci && npm run build` in `github-page/` (Next build).
- No backend, Bruno, or Playwright product suite required (no product UI change).
- Manual: confirm timeline still shows 2014→Aug 2026 eras plus the new entry after merge/deploy.
- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none (marketing site only).
- Full suite command: n/a for product; `cd github-page && npm run build`.
- HTTP/Bruno API suite: unchanged.
- Legacy paths at risk: none.

## Playwright Strategy

No product UI change; n/a. Public Pages verified by Next build and post-merge `Deploy GitHub Page` workflow.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge draft PR; Pages auto-deploys after green CI on `main`
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open https://matiaspakua.github.io/notaire/ and confirm the new timeline era

## Rollback Strategy

- Revert the PR; documentation / static site only.

## Migration / Rollout

None beyond the standard Pages deploy after merge.
