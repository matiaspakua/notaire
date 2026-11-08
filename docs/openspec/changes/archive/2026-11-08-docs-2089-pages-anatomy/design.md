> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The site is a Next.js static export with a shared registry (`lib/site.ts`), shared figure kit (`components/iso`), stepper and doc primitives. Facts are read from the repository at build time.

## Goals / Non-Goals

**Goals:** explain the system from the basics; keep every claim checkable; reuse the site's shell, stepper and kit.
**Non-Goals:** new infrastructure or a second design system.

## Decisions

1. **One figure per question** (system, engineering loop, AI SDLC), each a real machine with 60 or more solids, built at build time as SVG.
2. **Reuse `useStepper` and `StepControls`** so the figures behave like the other diagrams: nothing plays on its own, and the list is the text version.
3. **Describe the CI model as it is**: the pull request is proved by local preflight, the product workflows run on a version tag.
4. **Test the claims**: stations, parts and cited paths are checked.

## Riesgos / Trade-offs

- [page weight: each figure adds about 0.3 MB] -> the SVG is built once at build time and shared with the client as one string.
- [contrast of the plate text] -> darkened to 4.5:1 and covered by axe.

## Testing Strategy

`tests/anatomy.test.tsx` (18), the updated site map tests, and Playwright with axe on every page at 375 and 1280 px; the static export builds and the internal links resolve.

## Regression Strategy

`npm run typecheck && npm run lint && npm test && npm run build && npm run check:links && npm run test:smoke` in `github-page`.

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
