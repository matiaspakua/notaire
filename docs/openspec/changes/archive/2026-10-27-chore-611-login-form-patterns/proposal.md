# Login page uses mandated FormContainer / FormField patterns

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #611 |
| Use Case | CU84 – Login al sistema |
| Branch | `chore/611_login-form-patterns` |
| Gate 1 status | passed |

## Objetivo

The login page is the only real form that hand-rolls `Label`/`Input` markup instead of
`FormContainer` → `FormField` → `FormActions`, violating ADR-011 and the frontend design
system. Parent epic #1941 starts the UI/UX uplift here so every surface shares one pattern.

## What Changes

- Rebuild `frontend/src/app/login/page.tsx` with theme form patterns (`FormContainer`,
  `FormField`, `FormActions`); keep CU84 behaviour (session, lockout, expired banner).
- Sentence-case field labels (drop ALL-CAPS decorative labels) per design-system restraint.
- Extend `frontend/src/tests/unit/login-page.test.tsx` to assert the pattern contract.
- Note the login pattern in `FRONTEND-DESIGN-SYSTEM.md`; `CHANGELOG.md` Changed entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Login credentials are entered on the login screen and submitted to authenticate | CU84 | Unchanged (composition only) |
| All product forms compose `FormContainer` → `FormField` → `FormActions` | ADR-011, FRONTEND-DESIGN-SYSTEM | Made explicit for login |

## Capabilities

### New Capabilities

- `ui-login-form-patterns`: login page must use the mandated theme form primitives.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | login page composition + unit tests |
| `frontend-swing` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github/workflows`) | no | – |

### Surface area

- Entities: none
- Endpoints: none (still `POST /users/login` via `authUseCases`)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING: no

### Architecture review

Follows ADR-011 (centralized design system). No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Note login as a FormContainer consumer; sentence-case labels on auth |
| `CHANGELOG.md` | Changed: login form uses design-system patterns |
| `docs/100-business/102-use-cases/CU84 - Login.md` | n/a — behaviour unchanged |

## Out of Scope

- Page chrome primitives (`PageHeader` / `EmptyState`) — #1942
- Dashboard / workflow / charts uplift — epic #1941 later waves
- a11y axe automation — #1251
- Auth API, lockout policy, or MFA — #756 / #1640
