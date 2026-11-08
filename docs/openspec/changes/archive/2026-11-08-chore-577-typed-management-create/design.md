> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See proposal.md — Objetivo. `create` is the smallest Management write still on `controller-loose-returns.txt` whose error bodies are empty. The archive tip already noted it can be typed without a new advice. `createCompleteCase` and `updateCompleteCase` return `DtoManagementSummary`, so they do not share this signature.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<ManagementResponse>` on `create`.
- Remove `ManagementController#create` from `controller-loose-returns.txt`.
- Keep 201 `ManagementResponse`, empty 400, empty 404, empty 200, and 500 `ErrorResponse`.

**Non-Goals:**

- Closing #577.
- A `ManagementCreateExceptionHandler`. Advice maps a second JSON shape; these arms are empty or already thrown.
- Typing complete-case writes, workflow trace, or transition.

## Decisions

Change only the declared return type. `ResponseEntity.badRequest().build()`, `notFound().build()`, and `ok().build()` infer `ManagementResponse` and still send no body. `SaveResult.ServerError` keeps `throw new IllegalStateException("Management create failed")`, which `GlobalExceptionHandler` already maps to 500 `{"message": "An unexpected error occurred"}` (`ManagementControllerServerErrorResponseTest`).

Alternative considered: throw domain exceptions and map empty bodies in advice, matching archive. Rejected — the HTTP body would not change, and a handler would be a new class for statuses the controller already returns.

## Riesgos / Trade-offs

[Risk] None material. Stored data, auth, and the JSON contract stay the same. → No mitigation beyond the existing create tests plus one MockMvc class for the empty arms.

[Risk] A future client that expected a JSON body on 400/404 would still not find one. → That body does not exist today. Do not add advice until a real second shape exists.

## Testing Strategy

- Shrink the baseline first so `ControllerSignatureArchitectureTest` fails while `create` is still `ResponseEntity<Object>`.
- `ManagementCreateControllerTest`: 201 body, empty 400, empty 404, empty 200. ServerError stays on `ManagementControllerServerErrorResponseTest`.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| create leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Create keeps the 201 body and empty error statuses | unit | `ManagementCreateControllerTest` |

- New unit tests: `ManagementCreateControllerTest`
- New integration tests: none (`ManagementControllerIntegrationTest` is PRESERVE)
- Coverage impact: no JaCoCo floor change. One more unit test. Owner target stays >90% (#2046).

## Regression Strategy

Existing create assertions stay: 201 on `SaveResult.Created`, 500 `ErrorResponse` on `SaveResult.ServerError`. No test casts the create body to `Object`.

- Existing tests affected: none
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk: none (`jpa` and `frontend-swing` untouched)

## Playwright Strategy

n/a — no UI change in this tip (CU02 screens already covered by `TS-0011`).

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: existing managements spec
- Edge / error paths covered: existing managements spec
- Viewports: unchanged
- Command: not run for this tip beyond preflight `--full`

## Deployment Strategy

Next release. No migration and no config change.

- Flyway migration required: no
- Deployment order / coupling: application only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `POST /api/v1/managements` still returns 201 with the management body

## Rollback Strategy

Revert the pull request.

- Revert safe: yes — no schema or stored-data change
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none beyond the typed Java signature

## Migration Plan

None.

## Open Questions

None.
