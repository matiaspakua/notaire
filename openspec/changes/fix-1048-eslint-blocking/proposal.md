# Make frontend ESLint blocking in CI

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1048 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1048-eslint-blocking-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1057 merges** |

## Objetivo

`.github/workflows/frontend-ci.yml` still runs `npm run lint` with
`continue-on-error: true`, justified as temporary until #701. Issue #701 is
**CLOSED**, so ESLint (including a11y) violations can merge to `main` while the
job appears green. Local `scripts/preflight.sh` already treats frontend ESLint
as blocking; CI must match that gate so quality cannot regress via the PR path.

## What Changes

- Remove `continue-on-error: true` from the **ESLint** step in
  `frontend-ci.yml` (lines ~47–49); update/remove the obsolete #701 comment.
- Fix any remaining `npm run lint` violations on the branch tip so the job can
  fail closed without breaking `main`.
- Ensure `jsx-a11y` rules are enabled (via `eslint-config-next` / explicit rules
  in `frontend/eslint.config.mjs`) so accessibility regressions fail lint.
- Align `scripts/preflight.sh` mapping/comments so local and CI both document
  frontend ESLint as **blocking** (same command semantics: `eslint src
  --max-warnings=0` / `npm run lint`).
- Leave unrelated `continue-on-error` (e.g. test-reporter publish) untouched.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Frontend lint MUST fail the CI pipeline when ESLint reports errors or warnings | CU76 CI quality gates; #1048 AC | Made explicit (was advisory after #701 closed) |
| Local preflight MUST mirror the blocking ESLint gate used in CI | `scripts/preflight.sh`; #1048 AC | Made explicit (comment/docs sync) |
| jsx-a11y rules MUST be enabled so a11y lint failures are blocking | #1048 AC; pairs with #1057 icon-button naming | New / reinforced tooling rule |

## Capabilities

### New Capabilities

- `frontend-eslint-blocking`: Frontend CI ESLint step is a hard gate; preflight
  mirrors it; jsx-a11y contributes to the blocking lint surface.

### Modified Capabilities

- (none under `openspec/specs/` today cover the frontend ESLint CI gate)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | eslint config (jsx-a11y), any lint violation fixes |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `frontend-ci.yml` ESLint step fails closed |
| Scripts | yes | `preflight.sh` MAP / comments for blocking ESLint |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: `eslint-plugin-jsx-a11y` already present via `eslint-config-next`;
  may tighten rules in `frontend/eslint.config.mjs` if not already done by #1057

### Architecture review

No product architecture change. Closes the local/CI gap for frontend lint
(same class of problem as Spotless unbound from Maven — see CI-PREFLIGHT docs).
Depends on / serializes after **#1057** (Playwright-heavy a11y icon names +
jsx-a11y enablement) so violation volume and rule surface are settled before
the gate flips to blocking.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Note that frontend ESLint is a blocking CI gate (incl. jsx-a11y) |
| `docs/300-development/CI-PREFLIGHT.md` (or equivalent CI docs) | Update ESLint advisory/#701 language → blocking in `frontend-ci.yml` |
| `scripts/preflight.sh` header MAP | Replace “(advisory in CI — #701; blocking here)” with blocking-in-CI |
| `CHANGELOG.md` | `[Unreleased]` CI/quality entry: frontend ESLint blocking |

## Out of Scope

- Making unrelated `continue-on-error` steps blocking (test-reporter, SpotBugs,
  Trivy, Playwright artifact steps).
- Full WCAG/axe audit beyond what jsx-a11y + #1057 cover.
- Backend Checkstyle / Spotless policy (#705).
- Starting implement / PR before **#1057** merges.
