# Audit and fix drift between scripts/preflight.sh and .github/workflows/*.yml

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1029 |
| Use Case | none — documented technical/process exception, same precedent as #973 |
| Branch | `chore/1029_preflight_ci_drift_audit` |
| Gate 1 status | passed |

## Objetivo

CONSTITUTION.md, CLAUDE.md and `scripts/preflight.sh` itself claim that local
tooling mirrors CI exactly, so "local green" reliably predicts "CI green."
This is the exact recurring failure mode already called out once (Spotless
unbound from the Maven lifecycle, #705). This change re-verifies the claim
today against the real `.github/workflows/*.yml` jobs and fixes any drift
found, so the claim stays true instead of silently rotting.

## What Changes

- Full enumeration of every job/step in `ci.yml`, `pr-validation.yml`,
  `frontend-ci.yml`, `playwright-e2e.yml` and a diff against
  `scripts/preflight.sh` (including its own `--list` mapping).
- Cross-check of the JaCoCo ratchet floor in `backend-api/pom.xml` against
  CONSTITUTION.md §6/§7's stated floor — confirmed accurate (70% line / 25%
  branch), no fix needed there.
- **Fix**: `scripts/preflight.sh --list`'s `frontend eslint` row did not
  annotate that `frontend-ci.yml`'s ESLint step runs with
  `continue-on-error: true` (advisory, tracked by #701), unlike every other
  advisory row in the same table (checkstyle, dependency analysis, spotbugs,
  trivy), which are explicitly marked `(warn...)`. Annotated it consistently.
  The local check itself stays **blocking** — a deliberate, stricter-than-CI
  choice documented in the issue, not downgraded to match a known-broken CI
  step.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `scripts/preflight.sh --list`'s local-check → CI-job mapping must accurately state whether the CI counterpart is blocking or advisory, so a developer reading it can trust which local failures will actually fail CI. | CLAUDE.md "CI Preflight" section; `scripts/preflight.sh` header comment ("KEEPING IT HONEST") | Made explicit — the convention already existed for other rows, this change applies it consistently to the one row it was missing from. |

## Capabilities

This is a tooling/process-hygiene change with no user-facing or API
behavior change — it corrects a documentation string printed by a local
developer script.

### New Capabilities
_None._

### Modified Capabilities
_None — `skip_specs: true` set in `.openspec.yaml`._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — (drift audit only; no workflow file changed — none needed it) |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none — only `scripts/preflight.sh`'s `--list` heredoc text
  changes.

### Architecture review

Not architectural; no ADR required. Follows the existing convention already
used by every other advisory row in the same `--list` table.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `scripts/preflight.sh` | `--list` output: annotate the `frontend eslint` row as advisory-in-CI, matching the table's existing convention |
| `CHANGELOG.md` | n/a — not user visible; internal dev-tooling accuracy fix |

## Out of Scope

- Fixing the underlying `eslint-config-next` FlatCompat crash tracked in
  #701 — out of scope, tracked there.
- Downgrading the local ESLint check to `run_warn` to match CI's advisory
  status — deliberately rejected; would reopen the drift in the dangerous
  direction (local green masking a real lint regression). Documented as a
  judgment call in #1029.
