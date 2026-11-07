# Login desk: one task surface and token motion

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2069 |
| Use Case | CU84 – Login al sistema; RNF-05 – Aspecto visual |
| Branch | `fix/2069_login-operate-desk` |
| Gate 1 status | pending |

## Objetivo

The login page is the first Operate surface a notary sees, and it is the one
that still breaks the desk: three competing lines, no card, and a decorative
spring rotation on the seal. Group the task on one shared card and move the
entrance onto `frontend/src/theme/motion.ts`.

## What Changes

- Wrap the task title, session-expired status, and credential form in one card
  (`data-testid="login-card"`).
- Keep a single `h1` (Notaire). Render `login.title` as an `h2` inside the card.
- Replace the seal's spring and rotation with opacity plus a 4px rise using
  motion tokens. The card follows one stagger step later. The session-expired
  status fades in only when it is shown.
- Preserve FormContainer / FormField / FormActions, existing test ids, and CU84
  behavior (validation, lockout, redirect).
- Record the incumbent visual system in root `DESIGN.md` and `PRODUCT.md`, and
  note the Operate motion rule in the design-system doc and fleet lessons.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A clerk signs in with username and password on one task surface | CU84 | Made explicit (presentation only; credentials unchanged) |
| Operate motion uses the shared duration and easing tokens and does not rotate | RNF-05; #1368 | Made explicit for the login seal |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `ui-login-form-patterns`: the login page also presents one desk card, one page heading, and a non-rotating seal entrance.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | Login page, its unit test, design-system note |
| `backend-api` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: none
- Endpoints: none (`POST /api/v1/users/login` unchanged)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `frontend/src/app/login/**`
- `frontend/src/tests/unit/login-page.test.tsx`
- `testing/e2e/tests/TS-0001-login-authentication.spec.ts`
- `docs/openspec/changes/fix-2069-login-operate-desk/**`
- `docs/openspec/changes/archive/**`
- `docs/openspec/specs/ui-login-form-patterns/**`
- `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`
- `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md`
- `CHANGELOG.md`
- `DESIGN.md`
- `PRODUCT.md`
- `.impeccable/design.json`

### Architecture review

Presentation only. Tokens and form patterns stay the source of truth. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `DESIGN.md` | Scan-mode capture of the incumbent tokens, plus the desk and motion rules |
| `PRODUCT.md` | Product context inferred from the repo and this brief, each inference labeled |
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Short Operate note: login is one desk card; motion comes from `theme/motion.ts` |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | Friction: Operate UI must not invent a second motion language |
| `CHANGELOG.md` | Unreleased frontend note |

## Out of Scope

Table row motion that ignores `motion.ts`, title-only empty states, the home
module grid duplicating the sidebar, and the global `0.01ms` reduced-motion
kill. Those are separate issues from this pass. GitHub Pages stays on its own
Persuade surface and is not edited here.
