> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

History (#1775) and Management (#1772) ports-only landed or in flight. Copy is the next mid-size mixed R1 hotspot (~5 edges).

## Goals / Non-Goals

**Goals:** ports-only CopyController; ArchUnit R1 shrink of five edges; REST unchanged; omit CHANGELOG (#1766).
**Non-Goals:** Full domain model extraction; TestimonyController; CHANGELOG while #1766 open.

## Decisions

1. Prefer inbound Manage/Query ports returning views/commands. 2. Reuse CopyService inside CopyOpsAdapter for canCreateCopyForTestimony and entity CRUD. 3. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [FK person/testimony nullability] → preserve null-clearing semantics when ids omitted.
- [CU87 conflict path] → keep exact Map error body.

## Testing Strategy

SimpleControllersTest$CopyControllerTests, CopyControllerTest, HexagonArchitectureTest shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=SimpleControllersTest,CopyControllerTest,HexagonArchitectureTest,RemainingControllersIntegrationTest

## Playwright Strategy

n/a — no UI surface (copy REST contract unchanged).

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
