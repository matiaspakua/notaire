> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

ESLint 9.39 supports bulk suppressions (eslint-suppressions.json, --suppress-rule, --prune-suppressions): suppressed violations are counted per file and rule, so extra violations in a suppressed file still fail.

## Goals / Non-Goals

**Goals:** a baseline that reaches zero and cannot grow.
**Non-Goals:** moving code into core or adapters.

## Decisions

1. **ESLint's own suppressions instead of a hand-made baseline** so editors and CI use the same mechanism. 2. **Rules scoped by folder** so adding core and adapters later needs no config change.

## Riesgos / Trade-offs

- [A file in the baseline gets a second violation] → ESLint counts per file and rule, so the extra one fails.
- [The baseline is edited by hand] → the test only accepts existing files and the two boundary rules; review catches the rest.

## Testing Strategy

architecture-boundaries.test.ts with virtual files; npm run lint on the real tree; mutation checks of a new violation and a stale entry.

## Regression Strategy

cd frontend && npm run lint && npx vitest run

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
