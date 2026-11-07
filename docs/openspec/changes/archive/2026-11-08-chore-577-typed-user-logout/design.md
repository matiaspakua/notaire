> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See proposal.md — Objetivo. `logout` is the last auth-surface line on `controller-loose-returns.txt`. Its body is always `{ok: true}`.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<LogoutResponse>` on `logout`.
- Remove `UserController#logout` from `controller-loose-returns.txt`.
- Keep the 200 body and the `Set-Cookie` clear (`HttpServletResponse.addHeader`, #1620).

**Non-Goals:**

- Closing #577.
- A `UserLogoutExceptionHandler`. Advice maps a second HTTP shape; logout has one. Adding one would invent a failure the login tip refused.
- Changing revocation, cookie flags, or login.

## Decisions

Nested `LogoutResponse(boolean ok)` on `UserController`, same place as `LoginSuccessResponse`. The controller still resolves the token, calls `TokenRevocationService.revoke`, clears the cookie, and returns `ok: true`. Unexpected revocation errors still propagate to `GlobalExceptionHandler` (500). That is the current contract; this tip does not catch them.

Alternative considered: throw a domain exception and map `{ok: true}` in advice. Rejected — success is not a failure, and the HTTP status would not change.

## Riesgos / Trade-offs

[Risk] None material. Stored data, the auth cookie, and the JSON contract stay the same. → No mitigation beyond the existing logout integration tests.

[Risk] A future MockMvc test that expects a logout error body would not find a handler. → There is no such body. Do not add advice until a real second shape exists.

## Testing Strategy

- Shrink the baseline first so `ControllerSignatureArchitectureTest` fails while `logout` is still `Map<String, Object>`.
- `UserLogoutControllerTest`: 200 `{"ok": true}` and the cleared cookie, with a bearer token and with no token. Revocation receives the resolved token or null.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| logout leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Logout keeps the success body and cleared cookie | unit | `UserLogoutControllerTest` |
| login leaves the loose baseline (logout sentence removed) | unit | `ControllerSignatureArchitectureTest` |

- New unit tests: `UserLogoutControllerTest`
- New integration tests: none (`JwtLogoutRevocationIntegrationTest` is PRESERVE)
- Coverage impact: no JaCoCo floor change. One more unit test. Owner target stays >90% (#2046).

## Regression Strategy

Existing logout integration assertions stay: status 200, `ok: true`, replay rejected. No test casts the logout body to `Map`.

- Existing tests affected: none
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk: none (`jpa` and `frontend-swing` untouched)

## Playwright Strategy

n/a — no UI change in this tip (CU84 logout is already covered by `TS-0002`).

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: existing logout spec
- Edge / error paths covered: existing logout spec
- Viewports: unchanged
- Command: not run for this tip

## Deployment Strategy

Next release. No migration and no config change.

- Flyway migration required: no
- Deployment order / coupling: application only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `POST /api/v1/users/logout` still returns `ok: true`

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
