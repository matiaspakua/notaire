> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`/docs/hexagonal/` already prints port and adapter counts from `hexagon()`. The anatomy kit in `github-page/components/iso` draws the artifact cabinet and the gated line. Issue #2083 asked for the same treatment of ports and adapters. A first figure, a ring of six plugs, shipped in #2084. It reads as a diagram of a hexagon. This change replaces it with a sealed junction instrument.

## Goals / Non-Goals

**Goals:** One recognisable instrument whose parts are the domain, the ports and the adapters. The readout states `adapter → port → domain`. The factory home points at the existing gated line.

**Non-Goals:** No WebGL. No second copy of the gated line. No change to ArchUnit or to the package map below the figure.

## Decisions

The case is a closed slab. Glands are bosses on the front face and on the rear of the lid; their boxes are tested to stay outside the case interior. Each adapter is a module on the plinth joined to its gland by a short cable. Selecting a gland eases that plug back along the cable. The open inbound gland has no plug, so the socket is visible. Counts in the caption come from the same `hexagon()` object the stat grid uses.

## Riesgos / Trade-offs

The figure is static SVG plus a small client loop, like the cabinet. It runs only while on screen. A reader who wants the package graph still has the ring map and the Mermaid chart under the figure.

## Testing Strategy

Vitest asserts the four glands, the dependency readout, that no attachment box enters the case, the solid count (40–80), keyboard movement of the readout, and that the factory home links to the lifecycle page without mounting `GateLine`.

## Regression Strategy

`cd github-page && npm test` covers the existing site, cabinet and gated-line tests.

## Playwright Strategy

`github-page/e2e/site.spec.ts` already opens `/docs/hexagonal/` and `/ai-sdlc/` and runs an axe sweep. No new flow.

## Deployment Strategy

Ships with the GitHub Pages site on the next release. No service deploy.

## Rollback Strategy

Revert the pull request. The package map and the lifecycle figure stay.

## Migration Plan

None.

## Open Questions

None.
