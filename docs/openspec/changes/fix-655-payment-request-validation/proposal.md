# Validate payment requests at the REST boundary (slice of #655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU15 – Procesar pago |
| Branch | `fix/655_payment_request_validation` |
| Gate 1 status | draft |

## Objetivo

Reject malformed payment requests with 400 before they reach the use case; today a non-positive amount on update answers 404.

## What Changes

- `PaymentRequest`: `@NotNull idBudget`, `@NotNull @Positive amount`; `PaymentUpdateRequest`: `@Positive amount` (still optional for partial updates).
- `@Valid` on both `@RequestBody` parameters.
- OpenAPI artifact regenerated; Bruno `payments/08a`; CHANGELOG and Bruno COVERAGE.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A payment amount is strictly positive; a new payment names its budget | CU15 | Made explicit at the boundary |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `payment-processing`: Recording and editing payments (CU15).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PaymentController` request records |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | OpenAPI artifact, Bruno, CHANGELOG |

### Surface area

- Endpoints: `POST /api/v1/pagos`, `PUT /api/v1/pagos/{id}` (stricter contract)
- Entities / Flyway / Configuration: none

### Architecture review

Follows the #561/#737/#777 pattern; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/api-test/COVERAGE.md` | payments row |
