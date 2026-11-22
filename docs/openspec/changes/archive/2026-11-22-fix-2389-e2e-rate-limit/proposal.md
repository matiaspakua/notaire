# Fix e2e rate-limit ceiling and window-race in JPA store

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2389 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2389_e2e_rate_limit` |
| Gate 1 status | pending |

## Objetivo

The whole-product Playwright suite runs as the single `admin` user on 2 workers.
`ApiRateLimitFilter` allows `notaire.api.rate-limit.max-requests=2000` per 60 s per user.
During a full run user:admin peaked at 2025, so silent 429s break random tests.
A secondary race in `JpaApiRateWindowStore` — concurrent first requests collide on
`api_rate_windows_pkey` during `saveAndFlush` — produces intermittent errors even
below the ceiling. This change raises the e2e-only limit to 20 000, makes the 429
log useful without leaking PII, and eliminates the window-insert race.

## What Changes

- **E2e / preflight stack limit** — `workspace/sdlc/preflight.sh` exports
  `NOTAIRE_API_RATE_LIMIT_MAX=20000` (only when not already set) before calling
  `workspace/stack/start.sh --no-admin`. `docker-compose.yml` backend service passes
  `NOTAIRE_API_RATE_LIMIT_MAX: ${NOTAIRE_API_RATE_LIMIT_MAX:-2000}` so the default
  stays 2000 for manual starts. `docker-compose.prod.yml` and the production default
  in `application.properties` remain 2000 — never raised.
- **429 logging** — `ApiRateLimitFilter` logs every 429 at WARN with the key type
  (`user` or `ip`) and the request URI. It never logs the username or the IP address
  (personal data). SLF4J parameterized logging.
- **Race-proof JPA window store** — `JpaApiRateWindowStore` creates the window row
  with a native `INSERT ... ON CONFLICT (bucket_key) DO NOTHING` (new method in
  `ApiRateWindowRepository`), then advances with the existing `UPDATE`. The retry
  loop and now-unused code (`DataIntegrityViolationException` import, retries constant,
  `ApiRateWindow` public constructor if unused) are removed. No schema change.

No **BREAKING** change: all OpenAPI changes are additive (none).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The e2e/preflight stack must use a rate limit high enough that a full Playwright run never hits 429 | CU76, #2389 | New |
| The production default rate limit stays 2000 per 60 s | ADR-018 | Unchanged |
| A 429 log line must identify the key type and path but must never contain the username or IP | CU76, #2389 | New |
| Concurrent first requests on the same key must all be counted — none may throw | #2389 | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `api-quota`: three new requirements — 429 logged without PII; concurrent first requests all counted; e2e stack limit higher while prod stays 2000.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `ApiRateLimitFilter` (429 log), `JpaApiRateWindowStore` (race fix), `ApiRateWindowRepository` (new native query), cleanup of unused code |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none (uses existing `api_rate_windows` from V55).
- Endpoints: none.
- Database (Flyway): no migration needed — `ON CONFLICT DO NOTHING` works on the existing table; H2 in `MODE=PostgreSQL` supports it.
- Configuration / `.env`: `NOTAIRE_API_RATE_LIMIT_MAX` is already documented; the e2e path now sets it to 20000.
- Dependencies: none.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/security/ApiRateLimitFilter.java`
- `backend-api/src/main/java/com/licensis/notaire/security/JpaApiRateWindowStore.java`
- `backend-api/src/main/java/com/licensis/notaire/security/ApiRateWindow.java`
- `backend-api/src/main/java/com/licensis/notaire/repository/ApiRateWindowRepository.java`
- `backend-api/src/test/java/**`
- `docker-compose.yml`
- `workspace/stack/**`
- `workspace/sdlc/preflight.sh`
- `workspace/tests/test_e2e_rate_limit_env.py`
- `testing/e2e/**`
- `CHANGELOG.md`
- `docs/openspec/changes/**`
- `docs/openspec/specs/**`

### Architecture review

Follows existing architecture. The repository change is to the legacy-package class
`ApiRateWindowRepository` — the only edit to it, no new dependents. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fix entry under `[Unreleased]` — e2e rate limit raised, 429 logging, window race fix |

## Out of Scope

- Production rate limit value (stays 2000).
- `docker-compose.prod.yml` (untouched).
- Any change to the rate-limit algorithm or window size.
- Login username lockout (separate control, unchanged).
