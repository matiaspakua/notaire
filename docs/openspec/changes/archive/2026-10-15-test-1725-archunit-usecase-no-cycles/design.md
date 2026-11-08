> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

HexagonArchitectureTest already enforces R1–R6 with class-to-class baselines (#1695). Owner moved no-cycles between `application.usecase.(*)` out of #1695 because cycle reports need a separate baseline format. Tip has one-way edges into `catalog` (CatalogLockSupport) and `document -> management`; graph probe shows no SCCs with size > 1.

## Goals / Non-Goals

**Goals:** enforce no new use-case-slice cycles with a shrink-only baseline; CHANGELOG-free; runs in mvn test.
**Non-Goals:** breaking existing cycles by refactor (none today); changing REST; touching CHANGELOG; expanding R1–R6.

## Decisions

1. **Separate baseline file** `usecase-cycle-baseline.txt` (not R1–R6 class pairs). 2. **Slice key** = first package segment under `application.usecase` (e.g. `document`, `catalog`). 3. **Canonical ring** = rotate so the lexicographically smallest slice is first, choose the direction that yields the smaller following name, then close the ring. 4. **Same ratchet flags** as HexagonArchitectureTest: `-Darchitecture.baseline=init|shrink` (shared property; cycle test writes only its own file). 5. **Empty baseline is valid** when there are zero cycles.

## Riesgos / Trade-offs

- [False positives from nested packages] → only the first segment under usecase is the slice id.
- [Shared catalog helpers look like coupling] → one-way edges to `catalog` are allowed; only cycles fail.
- [ArchUnit Slice API vs custom graph] → custom Tarjan/Johnson-style detection on direct inter-slice deps keeps the baseline format stable and independent of ArchUnit message formatting.

## Testing Strategy

UseCaseSliceCycleArchitectureTest in mvn test; prove new-cycle and stale-baseline failures via focused assertions on the ratchet helper; init baseline on tip (expected empty).

## Regression Strategy

mvn test -pl backend-api -Dtest=UseCaseSliceCycleArchitectureTest,HexagonArchitectureTest; mvn verify -pl backend-api when preflight allows.

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
