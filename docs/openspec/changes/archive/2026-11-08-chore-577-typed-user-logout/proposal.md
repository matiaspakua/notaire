# Type UserController#logout (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU84 |
| Branch | `refactor/577_typed-user-logout` |
| Gate 1 status | passed |

## Objetivo

`UserController#logout` declares `ResponseEntity<Map<String, Object>>` only because the map value is `Object`. The body is always 200 `{"ok": true}`, including a missing or invalid token (CU84 step 7, #676). Login already left the baseline (#2079). This tip types logout only.

## What Changes

- `logout` returns `ResponseEntity<LogoutResponse>`.
- Shrink `controller-loose-returns.txt` by the `UserController#logout` line only.
- No new exception handler: logout has one body, so advice would invent a failure.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Logout stays 200 `{"ok": true}` and clears the HttpOnly session cookie, whether or not a token was presented | CU84 step 7 / #676 / #1620 | Made explicit |
| The presented token is revoked; a missing or invalid token does not change the HTTP body | #676 | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: `UserController#logout` leaves the loose-return baseline; the login requirement no longer says logout stays listed

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Logout return type, one MockMvc test, baseline |
| `frontend` | no | HTTP contract unchanged |
| `frontend-swing` | no | none |
| `infra` / observability | no | none |
| CI/CD (`.github/workflows`) | no | none |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/users/logout` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/UserController.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/UserLogoutControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md`
- `docs/openspec/**`

### Risk

Risk: none. The JSON body, the cleared session cookie, and token revocation stay as they are. No stored-data, authz, or merge-path change.

### Review budget

Target ≤400 counted lines (`aisdlc size`). OpenSpec, `CHANGELOG.md`, and `controller-loose-returns.txt` are excluded. One thin MockMvc class, not an extension of `UserLoginControllerTest`.

### Architecture review

Follows the typed-return pattern. Logout does not use advice: the login tip kept that handler login-only because the two methods do not share a failure shape. No new ADR. The controller still calls the revocation and cookie adapters only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Logout requirement; login requirement drops the “stays on the baseline” sentence |
| `docs/300-development/303-testing/INVENTORY.md` | Regenerated after the new unit test |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- Changing login, lockout, or `UserLoginExceptionHandler`
- Remaining audit and protocol loose returns
