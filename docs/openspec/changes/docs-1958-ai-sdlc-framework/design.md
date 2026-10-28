> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Owner request 2026-10-28 after UX uplift: extract reusable AI SDLC; evidence in Constitution, fleet lessons, OpenSpec, workspace/sdlc.

## Goals / Non-Goals

**Goals:** Ship kit + report + case study + ui-design skill without removing Notaire behaviour.
**Non-Goals:** Publishing a separate package registry; rewriting CONSTITUTION.md into the kit; MFA/HTTPS product work.

## Decisions

1. **Kit under framework/ai-sdlc/** (not workspace/) so adopters can copy the tree without Notaire harness coupling.
2. **Profile YAML** drives gate commands; Notaire profile shells out to existing scripts by relative path from repo root.
3. **Case study** is a new Pages route; existing AI Factory pages stay; nav gains one entry.
4. **Report** lives at repo-root `ai_sdlc_framework.md` for portfolio visibility.

## Riesgos / Trade-offs

- [Kit duplicates Constitution] → kit references principles; Constitution remains SoT for Notaire.
- [Pages build breaks] → keep to existing FactoryUI patterns; run github-page build in preflight path.

## Testing Strategy

framework/ai-sdlc/tests for profile load/reject; docs/tests or workspace test for report sections; github-page content unit if present.

## Regression Strategy

Existing AI Factory pages and Notaire gates unchanged.

## Playwright Strategy

n/a — github-page is a static site; build check covers it.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
