# Fix AuditAspectTest reflection for DeedRequest DTOs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1108 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-auditaspect-dto-reflection-69d3` |
| Gate 1 status | passed |

## Objetivo

After #1124 merged request-DTO binding for `DeedController.create` / `update`,
`AuditAspectTest` still reflected `Deed.class` and threw `NoSuchMethodException`
on five mutation cases — failing the Unit Tests job on `main`. This thin PR
updates reflection to `DeedController$DeedRequest` so Unit Tests unblock.
Full integration-test fallout from #1124 is owned by another worker.

Related process work under CU76 / Issue #1108 (Gate 1 / CI green). This PR does
**not** close #1108.

## What Changes

- `AuditAspectTest` looks up `create(DeedRequest)` and `update(Integer, DeedRequest)`
  via `Class.forName("...DeedController$DeedRequest")` (same pattern as
  `UserController$UserRequest`).
- Remove unused `business.Deed` import from that test.
- Add this OpenSpec change folder so Process Checks pass without `sdlc-exception`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unit tests SHALL compile against current controller method signatures | CU76 | Made explicit |
| Audit aspect unit coverage SHALL keep reflecting real controller methods | CU76 / #555 audit tests | Changed |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `auditaspect-deedrequest-reflection`: AuditAspectTest reflection uses DeedRequest

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `AuditAspectTest` only |

### Surface area

- Entities: none
- Endpoints: none (test-only)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No production code change. Test reflection aligned with post-#1124 controller API.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None | Test-only hotfix; no permanent doc update |
