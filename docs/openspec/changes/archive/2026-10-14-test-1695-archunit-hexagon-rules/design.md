> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

ADR-021 accepted Spring Service and Transactional inside use cases as the pragmatic composition root; the Owner kept that decision on #1693 and asked ArchUnit to enforce it as an allow-list.

## Goals / Non-Goals

**Goals:** a baseline that goes to zero without anyone being able to add a violation meanwhile.
**Non-Goals:** refactoring production code.

## Decisions

1. **Own baseline comparison instead of FreezingArchRule.** FreezingArchRule does not fail on a stale entry by default and rewrites its store silently; the requirement is that fixed entries must be removed. 2. **Class-to-class granularity.** Aggregating by package would let a class add a second forbidden dependency unnoticed. 3. **One baseline file, rule-prefixed lines** so a diff shows exactly which rule improved.

## Riesgos / Trade-offs

- [Baseline file is large] → it is the honest size of the debt; it shrinks with every slice.
- [ArchUnit cannot read the newest class file version] → verified on Java 26 before the baseline is written.

## Testing Strategy

HexagonArchitectureTest runs in mvn test; a temporary mutation adds a forbidden dependency and shows the failure.

## Regression Strategy

mvn verify -pl backend-api

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
