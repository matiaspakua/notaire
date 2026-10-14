> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The component is pure given a trace; the tests render it with the Spanish messages and a mocked reduced-motion hook.

## Goals / Non-Goals

**Goals:** fast tests for the tracker's interactions and layout.
**Non-Goals:** changing the component.

## Decisions

1. **Render with the real messages**, not a translation mock.
2. **Wait real time to prove the modal stays open** because AnimatePresence defers removal.
3. **Prove the tests can fail** by breaking the Escape key and the backdrop guard once.

## Riesgos / Trade-offs

- [exit animation timing] → closing is awaited with waitFor and staying open is checked after 2 seconds.

## Testing Strategy

The new file; mutation checks failed the intended tests.

## Regression Strategy

`npx vitest run --coverage` in `frontend`; `python3 -m unittest discover -s testing/tests` for the inventory.

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
