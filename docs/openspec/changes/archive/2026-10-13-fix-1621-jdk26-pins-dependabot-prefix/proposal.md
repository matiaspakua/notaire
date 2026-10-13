# Restore pinned JDK 26 backend images; make Dependabot PRs pass the SDLC gates

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1621 |
| Use Case | CU78 — Security, Privacy and Compliance |
| Branch | `fix/1621_jdk26-pins-dependabot-prefix` |
| Gate 1 status | passed |

## Objetivo

`main` @ b43e2eb6 fails two process self-tests. Dependabot PRs #1615 and #1616
were merged with red `Process Checks`: they moved the backend images to JDK 27
and the builder to a floating `maven:3-…` tag. Behind that sits a second cause.
Every Dependabot PR is red by construction, because its `deps(deps): …` title is
not an allowed Conventional Commit type. Merging over red checks has therefore
become routine.

## What Changes

- `backend-api/Dockerfile` and `Dockerfile.slim` go back to the pinned JDK 26
  bases (`maven:3.10.0-eclipse-temurin-26-alpine`,
  `eclipse-temurin:26.0.2.1_1-jre-alpine`).
- `.github/dependabot.yml` uses `prefix: "build"` (titles `build(deps): …`), and
  the `/backend-api` docker entry ignores JDK major bumps for `eclipse-temurin`
  and `maven`.
- `security/tests/test_image_pins_and_dependabot.py` gains two guards for these
  rules.
- Docs: ADR-017, the devsecops README, and `.claude/rules/hooks.md`. The last two
  also stop claiming that the `protect-main` ruleset enforces PRs and required
  checks today (#1040).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The backend builds and runs on pinned JDK 26 bases | `jdk-toolchain` spec, ADR-017, #1276 | Unchanged — restored |
| Dockerfile bases are minor-or-digest pinned | #1045, CU78 | Unchanged — restored |
| Dependabot PR titles and commits use allowed Conventional Commit types | CONSTITUTION §9, CU78 | New |
| The JDK major is never bumped by Dependabot | `jdk-toolchain`, ADR-017 | New |

## Capabilities

### New Capabilities

### Modified Capabilities
- `dependabot-npm-docker`: adds the allowed-prefix and JDK-major-ignore requirements.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Dockerfile base tags only; no Java change |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `.github/dependabot.yml` only; workflows unchanged |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: container base images return to JDK 26 pins

### Architecture review

Follows ADR-017 and the `jdk-toolchain` spec as written. No new ADR: this
restores the recorded decision and adds a bot guard that enforces it.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-017-container-base-images.md` | Dependabot ignores JDK majors; why |
| `docs/200-architecture/208-devsecops/README.md` | Dependabot prefixes/guard; ruleset live vs desired state |
| `.claude/rules/hooks.md` | Ruleset live state; red check is a policy stop |
| `CHANGELOG.md` | n/a — not user visible (release-please lists `fix` commits) |

## Out of Scope

- Applying the `protect-main` ruleset (Owner-only, #1040).
- Running process self-tests on `push: main` (#1622).
- A planned JDK 27 move (would be its own issue + ADR update).
