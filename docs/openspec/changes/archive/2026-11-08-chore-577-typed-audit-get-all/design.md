> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`getAll` returns either a flat list or a page object. `AuditRecordGetAllResponse` uses `@JsonValue` so the HTTP JSON is unchanged.

## Goals / Non-Goals

**Goals:** Type the handler; remove `AuditRecordController#getAll` from the baseline.

**Non-Goals:** Closing #577; changing query parameters or pagination defaults.

## Testing Strategy

- Shrink baseline first; `AuditRecordControllerTest` proves flat and paginated JSON.

| Scenario | Test |
|----------|------|
| getAll leaves baseline | `ControllerSignatureArchitectureTest` |
| Flat and page shapes | `AuditRecordControllerTest` |

## Decisions

Introduce `AuditRecordPageResponse` and `AuditRecordGetAllResponse` in the web adapter. Reject a second GET endpoint.

## Riesgos / Trade-offs

[Risk] None material. Existing `AuditRecordControllerTest` covers both shapes.

## Regression Strategy

`AuditRecordControllerTest` and Bruno audit list collections are PRESERVE.

## Playwright Strategy

n/a

## Deployment Strategy

Next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
