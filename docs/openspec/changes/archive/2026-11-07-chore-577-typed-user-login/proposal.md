# Type UserController#login via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU84 |
| Branch | `refactor/577_typed-user-login` |
| Gate 1 status | passed |

## Objetivo

`UserController#login` declares `ResponseEntity<?>` because a success map, a 200 `{valid: false}` rejection, and a 429 lockout body share one method. Clients already depend on those HTTP shapes (CU84). `logout` always returns 200 `{ok: true}` and does not share those shapes, so it stays on the baseline.

## What Changes

- Domain exceptions plus `UserLoginExceptionHandler` map lockout and rejected login.
- `login` returns `ResponseEntity<LoginSuccessResponse>`.
- Shrink `controller-loose-returns.txt` by the `UserController#login` line only.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Valid credentials stay 200 with `valid: true`, a token, user fields, an optional `person`, and the HttpOnly session cookie | CU84 steps 3–4 / #1051 / #1620 | Made explicit |
| Unknown user, bad password, inactive user, and unexpected login errors stay 200 `{valid: false}` | CU84 exceptions 3a–3d | Made explicit |
| A locked account stays 429 `{valid: false, message}` | CU84 exception 3c / #560 | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: `UserController#login` leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exceptions, controller tests, baseline |
| `frontend` | no | HTTP contract unchanged |
| `frontend-swing` | no | none |
| `infra` / observability | no | login metric names unchanged |
| CI/CD (`.github/workflows`) | no | none |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/users/login` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/UserController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/UserLoginExceptionHandler.java`
- `backend-api/src/main/java/com/licensis/notaire/domain/user/Login*.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/UserLoginControllerTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/UserControllerHashTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/AdditionalControllersTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md`
- `docs/openspec/**`

### Architecture review

Follows the #577 advice pattern already used by Management archive. No new ADR. The controller still calls the inbound port only; lockout, JWT, cookies, and metrics stay in the adapter.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Login merges the ADDED requirement |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- `UserController#logout` (always `{ok: true}`; no shared failure shape with login)
- Remaining Management, audit, and protocol loose returns
