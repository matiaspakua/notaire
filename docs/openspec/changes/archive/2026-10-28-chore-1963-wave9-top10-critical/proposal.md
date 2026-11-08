# Wave 9 — top-10 critical platform & security closes

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1963 |
| Use Case | CU-78 (Security) + CU-76 (Platform) + ADR-029/ADR-030 slices |
| Branch | `chore/1963_wave9_top10_critical` |
| Gate 1 status | passed |

## Objetivo

Board review prioritized the ten most critical open items, challenged each, and
defined a **Wave 9 close-set** of ten solvable issues. This change implements
that set under the Constitution AI SDLC, then supports full local E2E and a
product release cut with docs/Pages updates.

## What Changes

- Close #1866 if already shipped on `main` (verify ArchUnit / ports) or finish residual.
- Enforce role module grants server-side (#1683).
- HTTPS/TLS profile + Docker + self-signed local + production runbook (#254).
- Verifiable per-installation isolation checklist (#1684).
- CI: Trivy fails on CRITICAL; Checkstyle/SpotBugs fail-on-error with ratchet (#566).
- Bean Validation on remaining write controllers (#655).
- Unbounded EAGER `@OneToMany` → LAZY (#595).
- Budget/Management/Payment: invalid input → 400, missing → 404 (#1921).
- Accepted-breaking list empty + guard proven (#1920).
- OpenAPI coherent with empty accepted-breaking (#1315).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Role module grants SHALL restrict API access server-side | ADR-029 / #1683 | New enforcement |
| Installations SHALL use non-default secrets and isolated stack | ADR-029 / #1684 | Made verifiable |
| TLS SHALL be available for local prove-out and documented for prod | CU-78 / #254 | New |
| CI quality/security scanners SHALL fail the build on new/critical findings | #566 | Changed |
| Write APIs SHALL reject invalid bodies with 400 via Bean Validation | #655 | Expanded |
| Collections that can fan out SHALL not be EAGER | #595 | Changed |
| IllegalArgument for bad input SHALL map to 400; missing resource to 404 | #1921 / #1315 | Changed |

## Capabilities

### New Capabilities

- `wave9-critical-platform`: Wave 9 security, CI, validation, OpenAPI, and hex child closes

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | security, validation, JPA fetch, HTTP status mapping, HTTPS config |
| `frontend` | maybe | only if grants/HTTPS UX required |
| `infra` / Docker | yes | TLS wiring |
| CI/CD | yes | Trivy / Checkstyle / SpotBugs fail behavior |
| docs / github-page | yes | isolation, HTTPS, release notes |

### Surface area

- Entities: fetch type on EAGER collections
- Endpoints: status codes; module-grant authorization
- Database: none expected (unless grants schema already exists)
- Configuration / `.env`: TLS and isolation checklist
- Dependencies: none expected

### Architecture review

Follows ADR-029 (isolation + module grants) and ADR-030 (hex child #1866). No new ADR.

## Documentation Impact

- Security runbooks / HTTPS setup
- Isolation checklist under docs/200-architecture or ops
- CI docs for ratchet behavior
- CHANGELOG `[Unreleased]` for user-visible / security changes
- README / GitHub Pages as part of release follow-on
