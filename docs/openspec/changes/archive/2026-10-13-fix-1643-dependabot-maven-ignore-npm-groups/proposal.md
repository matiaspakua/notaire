# Stop the JDK 27 maven builder bump and group npm Dependabot updates

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1643 |
| Use Case | CU78 — Security, Privacy and Compliance |
| Branch | `fix/1643_dependabot-maven-ignore-npm-groups` |
| Gate 1 status | passed |

## Objetivo

The Owner cannot merge Dependabot PRs. One cause (#1629) is a JDK 27 + floating
maven builder bump that slipped past #1621's semver-major ignore, which can never
pass the toolchain and pin tests. Another is that `protect-main` requires
up-to-date branches, so five single-package npm PRs need five serial rebase and
full-CI cycles. (The shared E2E failure is #1620.)

## What Changes

- `/backend-api` docker: ignore every `maven` update.
- `/frontend` npm: group minor and patch updates into one PR.
- Self-test guards for both. ADR-017 and the devsecops README updated. #1629 closed.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The builder's JDK never moves through Dependabot | `jdk-toolchain`, ADR-017, #1621 | Changed — stricter (all maven updates) |
| npm minor/patch bumps come as one PR | #1643 | New |

## Capabilities

### New Capabilities

### Modified Capabilities
- `dependabot-npm-docker`: maven ignore without update-types; npm minor/patch group.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `.github/dependabot.yml` only |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: Dependabot behaviour only

### Architecture review

Follows ADR-017.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-017-container-base-images.md` | maven fully ignored; why |
| `docs/200-architecture/208-devsecops/README.md` | maven ignore and npm grouping |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- The E2E/Bruno failure shared by every PR (#1620, PR #1642).
- Turning off strict required checks (a ruleset decision for the Owner).
