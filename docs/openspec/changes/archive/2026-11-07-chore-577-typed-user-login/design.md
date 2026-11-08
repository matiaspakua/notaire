> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Issue #577 requires controller handler returns to stop using `Object` or `?`. `login` is the auth-surface method still on the baseline. Its success body, the 200 `{valid: false}` rejection, and the 429 lockout body are three shapes. `logout` is `ResponseEntity<Map<String, Object>>` only because the map value is `Object`; the body is always `{ok: true}`, including a missing or invalid token (#676). Those methods do not share a failure shape, so one advice would either invent a logout failure or change a login body. This tip types `login` only.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<LoginSuccessResponse>` on `login`.
- Map lockout to 429 `{valid, message}` and rejection (including unexpected errors) to 200 `{valid: false}` through `UserLoginExceptionHandler`.
- Remove `UserController#login` from `controller-loose-returns.txt`.
- Declare the Write set for `aisdlc scope`.

**Non-Goals:**

- Closing #577.
- Typing `logout` or any Management method.
- Changing credentials, cookies, lockout counters, or the HTTP contract.

## Decisions

Throw `LoginLockedException` and `LoginRejectedException` from the controller, matching Management archive. The handler is `@RestControllerAdvice(assignableTypes = UserController.class)` at highest precedence so `GlobalExceptionHandler`'s `Exception` catch-all does not turn 200/429 into 500. The broad `catch (Exception)` stays, rethrows those two types, and maps every other failure to `LoginRejectedException` after the `login/error` metric — that is the current 200 `{valid: false}` path (CU84 3d). The session cookie is still added with `HttpServletResponse.addHeader` (#1620). `LoginSuccessResponse` omits `person` only when `personId` is null (`@JsonInclude(NON_NULL)` on that component); other fields stay so a null token is still `"token": null`. Standalone MockMvc tests register the advice; without it those throws become 500.

## Riesgos / Trade-offs

None material. Stored data, auth cookies, and the JSON contract stay the same. A missing advice registration in a new MockMvc test would hide 200/429 as 500; this tip wires the handler in `UserLoginControllerTest`, `UserControllerHashTest`, and `AdditionalControllersTest`. The full Spring context picks up the `@RestControllerAdvice` bean.

## Testing Strategy

- `UserLoginControllerTest`: success body and cookie, optional person, 200 `{valid: false}` for not-found / bad credentials / inactive / unexpected error, 429 lockout message, with `GlobalExceptionHandler` also registered.
- `ControllerSignatureArchitectureTest`: `UserController#login` is absent from the baseline and is not loose. `UserController#logout` stays listed.
- Existing `JwtAuthIntegrationTest` and `LoginRateLimitIntegrationTest` are PRESERVE.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| login leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Rejected and locked logins keep their HTTP bodies | unit | `UserLoginControllerTest` |

- New unit tests: `UserLoginControllerTest`
- New integration tests: none (existing login integration tests are PRESERVE)
- TDD: shrink the baseline and run `ControllerSignatureArchitectureTest` before the return type changes

## Regression Strategy

Existing CU84 login assertions stay. `UserControllerHashTest` calls `login` directly and casts the body to `Map`; rejection will throw, so those tests assert the exception or MockMvc JSON without dropping the hash assertions. `AdditionalControllersTest` standalone MockMvc must register the advice or its login failure lines become 500. Integration login flows are PRESERVE: status codes and bodies do not change.

- Existing tests affected: `UserControllerHashTest`, `AdditionalControllersTest`
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk: none (`jpa` and `frontend-swing` untouched)

## Playwright Strategy

n/a — no UI change in this tip (CU84 screens already covered by `TS-0001` and `TS-0100`).

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: existing login spec
- Edge / error paths covered: existing lockout spec
- Viewports: unchanged
- Command: not run for this tip

## Deployment Strategy

Next release. No migration and no config change.

- Flyway migration required: no
- Deployment order / coupling: application only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `POST /api/v1/users/login` still returns `valid`

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
