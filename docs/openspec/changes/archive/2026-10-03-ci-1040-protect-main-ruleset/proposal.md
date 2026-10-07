# Protect main with a GitHub ruleset

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1040 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure; **CU78** – Security and Compliance |
| Branch | `cursor/ci-1040-protect-main-ruleset-69d3` |
| Gate 1 status | ready; implement after #1041 on `main` (`6b246a72`) |

## Objetivo

CONSTITUTION.md requires PR-only merges and green quality gates, but those
rules are still partly convention. As of Gate 1 verification (2026-10-03),
`main` has an active ruleset `protect-main` (id `24128115`) that only blocks
deletion and non-fast-forward — it does **not** require a pull request or any
status checks. Classic branch-protection remains unused (and must stay unused
per DevSecOps guidance). The Claude Code `block-push-to-main` hook and
`.claude/rules/hooks.md` still describe a full “no protection” gap from
2026-09-22. Close the gap: PR-only + required checks + no force-push/delete,
with bot bypass temporary until #1041 stops CI Bot commits to `main`.

## What Changes

- Extend (do not replace) the existing repository ruleset `protect-main` so
  `main` / default branch requires:
  - pull request before merge
  - required status checks for the five suites named in the issue AC
  - block force-push and deletion (already present — keep)
- Make the five AC check names exist as **exact GitHub check-run names** by
  adding thin aggregator jobs where missing (`CI`, `Frontend CI`,
  `Playwright E2E`, `PR Validation`; `Code Lint` already exists).
- Allow a **temporary** bypass for the GitHub Actions app (or the dedicated
  token used by CI report jobs) only while #1041 is not yet merged; remove
  that bypass in the same implement PR once #1041 is on `main`, or in an
  immediate follow-up commit on the #1040 branch if #1041 lands first.
- Update `.claude/rules/hooks.md` (and DevSecOps / CHANGELOG) so they no
  longer claim `main` is unprotected / rulesets empty.
- Add a checked-in desired-state ruleset manifest + apply/assert scripts so
  the configuration is reviewable in git and verifiable after an admin applies
  it (integration tokens cannot mutate rulesets).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Changes to `main` MUST land via Pull Request only | CONSTITUTION.md §9; CU78; #1040 AC | New (enforce on GitHub) |
| Merges to `main` MUST require the named status checks (CI, Frontend CI, Playwright E2E, Code Lint, PR Validation) | CU76; #1040 AC | New (enforce on GitHub) |
| Force-push and branch deletion on `main` MUST be blocked | #1040 AC; existing `protect-main` | Made explicit / keep |
| Bot bypass for direct pushes to `main` MUST be temporary and removed once #1041 stops report commits | #1040 AC; coupling #1041 | New |
| Classic (legacy) branch protection MUST NOT be layered on top of the ruleset | DevSecOps README; enable-gh-secure guidance | Made explicit |
| Required approving reviews are NOT introduced by this change (fleet unattended merge) | fleet ops; enable-gh-secure note | Made explicit (non-goal) |

## Capabilities

### New Capabilities

- `protect-main-ruleset`: GitHub ruleset on default branch enforces PR-only,
  the five required check-run names, and no force-push/delete; bot bypass is
  temporary until #1041; hooks/docs reflect the enforced state.

### Modified Capabilities

- (none under `openspec/specs/` today cover repository ruleset policy)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Thin aggregator jobs so AC check names exist as check-runs |
| Scripts / tests | yes | Desired-state ruleset JSON + apply/assert + static unittest |
| Docs / rules | yes | `.claude/rules/hooks.md`, DevSecOps, CHANGELOG; optional audit note |
| GitHub repo settings | yes | Ruleset `protect-main` updated via admin API/UI (not classic BP) |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No application-architecture change. Strengthens delivery control plane for
CU76/CU78. Complements #1041 (stops the need for a durable Actions bypass).
Must not enable legacy branch protection or required human reviews that would
block the unattended fleet merge path.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.claude/rules/hooks.md` | Replace “branch protection not configured / 404” gap text; state ruleset `protect-main` is the GitHub-side control; hook remains defense-in-depth |
| `docs/200-architecture/208-devsecops/README.md` | Document full `protect-main` policy (PR + required checks + no force-push/delete; no classic BP) |
| `docs/github/PRODUCTION-READINESS-AUDIT-2026-09.md` | Note #1040 theme line is resolved once closed (brief status, no duplicate process) |
| `CHANGELOG.md` | `[Unreleased]` devops/security entry for #1040 |

## Out of Scope

- Implementing or merging before **#1041** (queue: #1046 → #1042 → #1041 → #1040).
- Stopping CI Bot report commits (owned by #1041).
- Enabling classic branch protection or required approving reviews.
- Rewriting git history / force-push remediation.
- Product security work (#1051 JWT/CSP, RBAC #559, etc.).
- Dependabot / deprecated Swing deletion (#1046) or CD pin (#1042).
