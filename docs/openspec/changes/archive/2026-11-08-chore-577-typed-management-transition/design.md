> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See proposal.md — Objetivo. `transition` is the Management handler whose success body is one DTO and whose errors already leave the method as exceptions. `createCompleteCase` still returns `DtoManagementSummary` on one arm and empty bodies on the others, so it stays a later tip.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<ManagementSummaryDto>` on `transition`.
- Declare `ManagementSummaryDto` on `mapToHttpResponse`.
- Remove `ManagementController#transition` from `controller-loose-returns.txt`.
- Keep 200 `{id, currentStatus}`, 400 `ErrorResponse`, and 404 `ErrorResponse`.

**Non-Goals:**

- Closing #577.
- A `ManagementTransitionExceptionHandler`. The global handler already maps these exceptions.
- Typing complete-case writes or workflow trace.

## Decisions

Change the mapper's declared return type to the record it already constructs, then change the controller to `ResponseEntity<ManagementSummaryDto>`. `ResponseEntity.ok(dto)` sends the same JSON.

Alternative considered: cast the mapper's `Object` inside the controller and leave the mapper alone. Rejected — the cast would hide a type the mapper already returns, and the controller signature would depend on a runtime cast.

Alternative considered: throw new domain exceptions and add advice, matching archive. Rejected — `BusinessValidationException` and `ResourceNotFoundException` already produce the 400 and 404 bodies clients receive.

## Riesgos / Trade-offs

[Risk] None material. Stored data, auth, and the JSON contract stay the same. → No mitigation beyond the existing transition tests plus one MockMvc class.

[Risk] A caller that depended on the mapper's `Object` return type would fail to compile. → The only production caller is `ManagementController#transition`. The unit mock already returns `ManagementSummaryDto`.

## Testing Strategy

- Shrink the baseline first so `ControllerSignatureArchitectureTest` fails while `transition` is still `ResponseEntity<?>`.
- `ManagementTransitionControllerTest`: 200 body, 400 `message`, 404 `message`. The use case is mocked; the mapper is real so the body is the DTO it builds. `GlobalExceptionHandler` stays on the standalone setup.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| transition leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Transition keeps the 200 body | unit | `ManagementTransitionControllerTest` |
| Transition keeps the 400 and 404 error bodies | unit | `ManagementTransitionControllerTest` |

- New unit tests: `ManagementTransitionControllerTest`
- New integration tests: none (`ManagementTransitionControllerIntegrationTest` is PRESERVE)
- Coverage impact: no JaCoCo floor change. One more unit test. Owner target stays >90% (#2046).

## Regression Strategy

Existing transition assertions stay: 200 `currentStatus` on a legal move, 400 `message` on an illegal move, 404 when the management is missing (`ManagementTransitionControllerIntegrationTest`). `AdditionalControllersTest` still stubs `mapToHttpResponse` with `ManagementSummaryDto`.

- Existing tests affected: none
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk: none (`jpa` and `frontend-swing` untouched)

## Playwright Strategy

n/a — no UI change in this tip (CU83 status changes already covered by `TS-0011`).

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
- Smoke test after deploy (Gate 5): `POST /api/v1/managements/{id}/transition` still returns 200 with `id` and `currentStatus`

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
