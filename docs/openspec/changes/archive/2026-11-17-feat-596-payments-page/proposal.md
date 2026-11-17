# feat: page the payments list

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU47 — Query payment status |
| Branch | `cursor/596-payments-page-47dc` |
| Gate 1 status | passed |

## Objetivo

`GET /api/v1/payments` returned every payment. The list screen loaded that array. This slice returns one page (default 20, newest id first) and the payments screen reads that page. Budget and date filters stay as they are.

## What Changes

- `GET /api/v1/payments` returns a Spring page with `page`, `size` and `sort`.
- The payments screen asks for `idPayment,desc` and shows the shared pager when no budget or full date range is selected.
- OpenAPI, generated frontend types and the Bruno list check match the page.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A payment list is one page, default 20, newest id first | CU47; #596 | Changed |
| One budget's payments and a full date range stay their own queries | CU47 | Made explicit |

## Capabilities

### New Capabilities

- `payment-list-page`: `GET /api/v1/payments` returns `content`, `totalElements`, `number` and `size`.

### Modified Capabilities

- The unpaged array response is replaced for this one endpoint.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Query port, adapter, controller, OpenAPI, Bruno |
| `frontend` | yes | Payments list screen and HTTP gateway |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Endpoints: `GET /api/v1/payments` response is a page
- Database: none
- Not a new auth rule. Clients that read a bare array must read `content`.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/payment/PaymentController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/payment/PaymentPersistenceAdapter.java`
- `backend-api/src/main/java/com/licensis/notaire/application/port/in/payment/QueryPaymentsUseCase.java`
- `backend-api/src/main/java/com/licensis/notaire/application/port/out/payment/PaymentRepositoryPort.java`
- `backend-api/src/main/java/com/licensis/notaire/application/usecase/payment/PaymentQueryService.java`
- `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/payment/PaymentControllerTest.java`
- `backend-api/src/test/java/com/licensis/notaire/application/usecase/payment/InMemoryPaymentRepository.java`
- `backend-api/openapi/openapi.yaml`
- `backend-api/api-test/payments/02-get-all.yml`
- `frontend/src/app/dashboard/payments/page.tsx`
- `frontend/src/hooks/usePayments.ts`
- `frontend/src/adapters/http/payment/payment-http-gateway.ts`
- `frontend/src/core/payment/application/**`
- `frontend/src/types/api.generated.ts`
- `frontend/src/tests/unit/payments-*.test.tsx`
- `docs/openspec/changes/feat-596-payments-page/**`

### Architecture review

Follows the existing budget and people page ports (ADR-030). No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/openapi/openapi.yaml` | page, size, sort and `PageDtoPaymentResponse` |
| `CHANGELOG.md` | n/a — not a release |

## Out of Scope

- Paging payments by budget or by date. #596 stays open.
- Other growing lists.
- #580.
