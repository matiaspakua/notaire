> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ArchUnit R2 wave after FE suppressions = 0. Deed is the smallest high-fan domain (~15 R2 edges @ `01eaf983a`). Builds on #1811 `DeedOpsPort` views.

## Goals / Non-Goals

**Goals:** No `business.*` on deed inbound numbering ports; application.usecase.deed free of Spring Data / entity R2 edges.
**Non-Goals:** Extracting a full `domain.deed.Deed` aggregate; rewriting FolioRepository access beyond moving services into the adapter package.

## Decisions

1. Replace `Person notary` on `CalculateNextDeedNumberUseCase` / `ValidateDeedNumberingUseCase` with `Integer notaryId` (ports already used ids on `DeedNumberingPort`).
2. Relocate `DeedService` and `DeedSigningService` to `adapter.out.persistence.deed` — they are persistence helpers for `DeedOpsAdapter`, not application orchestration. R2 only scans `application.*`.

## Riesgos / Trade-offs

Moving `@Service` classes between packages is a Spring component-scan move only; behaviour unchanged. Callers outside the adapter package must update imports (tests + OpsAdapter).

## Testing Strategy

Update `DeedNumberingUseCaseTest` and `DeedServiceTest` / `DeedSigningServiceTest` imports; run `HexagonArchitectureTest` with `-Darchitecture.baseline=shrink`.

## Regression Strategy

Existing deed unit + controller/integration tests; Bruno deed paths if present.

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
