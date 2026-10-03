# Clear Dependabot noise from dead Swing log4j and pin smol-toml

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1046 |
| Use Case | **CU78** – Security, Privacy and Compliance |
| Branch | `cursor/fix-1046-dependabot-alerts-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1044 and #1051 merge** |

## Objetivo

Open Dependabot critical/high alerts from EOL `log4j:log4j:1.2.17` in the dead
`deprecated-frontend-swing/` tree (plus a failing Dependabot scan for that
path) keep the Security tab permanently red and hide real findings. The
frontend lockfile also pulls `smol-toml` via `markdownlint-cli2`. Clear the dead
Swing surface and pin/override `smol-toml` so critical/high dependency alerts
for these findings go to zero.

## What Changes

- Delete the entire `deprecated-frontend-swing/` tree from `main` (dead Swing
  client; excluded from the root Maven reactor already). This removes the
  `log4j:log4j:1.2.17` dependency that drives the critical/high alerts.
- Add a minimal npm `overrides` entry (or equivalent lockfile pin) so
  `frontend/package-lock.json` resolves `smol-toml` to a patched release
  (`>=1.7.1`; prefer latest `1.9.0`) even though `markdownlint-cli2@0.23.3`
  currently declares `smol-toml@1.8.0`.
- Refresh stale references that still point at a live Swing module path
  (e.g. `.github/CODEOWNERS` `/frontend-swing/`, outdated skill/README notes)
  so docs match “Swing removed; do not recreate.”
- **No product API/UI behavior change.** No Flyway. No backend runtime deps.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Active product tree MUST NOT retain EOL Log4j 1.x dependencies | CU78; #1046 AC | Made explicit (delete dead tree) |
| Frontend lockfile MUST resolve `smol-toml` to a non-vulnerable version | CU78; GHSA-7w5x-hrqm-74c2; #1046 AC | New (override/pin) |
| Security dashboard MUST NOT stay red solely from archived Swing dead code | CU78; audit-2026-09 | Made explicit |
| Legacy Swing MUST NOT be reintroduced as an active module | CLAUDE.md / ADR-005 | Unchanged (enforce by deletion) |

## Capabilities

### New Capabilities

- `frontend-smol-toml-override`: Pin/override `smol-toml` in the frontend npm
  lockfile so Dependabot’s high alert for that package is resolved.
- `retire-deprecated-frontend-swing`: Remove `deprecated-frontend-swing/` (and
  stale live-path references) so Maven Dependabot log4j critical/high alerts
  from that dead tree disappear.

### Modified Capabilities

- (none under `openspec/specs/` today cover Dependabot alert hygiene for these
  packages)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes (deps only) | `package.json` overrides + `package-lock.json` refresh for `smol-toml` |
| `frontend-swing` / `deprecated-frontend-swing` | yes | **Delete** `deprecated-frontend-swing/` tree |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | maybe | Drop/adjust any leftover Swing path refs; CODEOWNERS cleanup |
| Scripts / docs | yes | README/SAD/CODEOWNERS/skill notes that still imply Swing is present |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: remove Maven `log4j:log4j:1.2.17` (via tree delete); override npm
  `smol-toml` → `^1.9.0` (or `>=1.7.1` minimum)
- **BREAKING**: anyone still building the archived Swing client locally loses
  that path on `main` (intentional; module already out of reactor/CI)

### Architecture review

Aligns with ADR-005 / CLAUDE.md: Swing is not an active client; Next.js is the
only frontend. Deleting the deprecated tree finishes the retirement that
exclusion from the Maven reactor started. No new architecture — optional short
note in ADR-005 “Status” that the deprecated directory was removed under #1046.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Note dependency/alert hygiene outcome if CU tracks security ops AC |
| `README.md` | Remove or archive the `deprecated-frontend-swing/` tree listing |
| `docs/200-architecture/202-ADR/ADR-005-modern-frontend-migration.md` | Status: deprecated directory deleted (#1046), not merely renamed |
| `docs/200-architecture/201-SAD/sad.md` | Drop “still present” risk rows for `deprecated-frontend-swing` |
| `.github/CODEOWNERS` | Remove stale `/frontend-swing/` entry |
| `deprecated-frontend-swing/README.md` | Removed with the tree (history remains in git) |
| `CHANGELOG.md` | Security/deps entry: cleared Dependabot log4j (dead Swing) + smol-toml pin |

## Out of Scope

- **#585** — still OPEN; original scope is `src.old` / legacy monolith tree
  cleanup (`deprecated-src.old/` on `main`). Do **not** absorb full #585 here.
  Issue #1046 AC cites “(#585)” for Swing deletion; audit comments on #585
  raised priority because Swing still triggers log4j alerts, but **this change
  owns deleting `deprecated-frontend-swing/`**. Leave `deprecated-src.old/` to
  #585 (or a follow-up) unless the coordinator explicitly widens scope.
- Other npm high findings (e.g. `braces`/`micromatch` via markdownlint) not
  named in #1046 AC.
- Backend Maven dependency upgrades unrelated to Swing log4j.
- Product auth/CSP work (#1051), prod compose (#1044), or other audit issues.
