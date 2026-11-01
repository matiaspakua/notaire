# Payment controller lets unexpected errors reach GlobalExceptionHandler

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #579 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/579_payment-propagate-exceptions` |
| Gate 1 status | passed |

## Objetivo

Payment web adapter still wraps every call in `catch (Exception)` and returns an empty
500 body, bypassing `GlobalExceptionHandler`. Remove those catch-alls so unexpected
failures return the standard `ErrorResponse` (safe message, no stack/SQL leak).

## What Changes

- Delete `catch (Exception)` from `PaymentController` (keep `IllegalArgumentException` /
  `PendingBalanceExceededException` mapping for empty-body 4xx contract)
- Strengthen `PaymentControllerTest` to require `ErrorResponse` JSON on unexpected failure
- Document as #579 payment slice (umbrella issue stays open for other controllers)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unexpected payment API failures return the standard ErrorResponse via GlobalExceptionHandler | #579, CU76 | Made explicit for payments |

## Capabilities

### New Capabilities

- `payment-exception-propagation`: payment adapter does not swallow unexpected exceptions

### Modified Capabilities

(none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | PaymentController + unit test |
| docs | yes | OpenSpec + CHANGELOG |

### Surface area

Unexpected 500s gain an `ErrorResponse` JSON body (status stays 500). Happy paths and
deliberate 4xx empty bodies unchanged.

### Architecture review

Advances #579 toward GlobalExceptionHandler as the single unexpected-error path.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

Other controllers' try/catch; moving `IllegalArgumentException` /
`PendingBalanceExceededException` into GlobalExceptionHandler.
