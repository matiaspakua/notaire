# ci: run the OWASP ZAP baseline on a version tag

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2004 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/2004-zap-on-tags-47dc` |
| Gate 1 status | passed |

## Objetivo

A version tag can publish without a fresh OWASP ZAP baseline. Monday and manual dispatch were the only triggers. The scan stays off pull requests.

## What Changes

- `dast-zap.yml` also runs on tags `v*` and `notaire-v*`.
- Monday 05:00 UTC and `workflow_dispatch` stay.
- `pull_request` and branch `push` stay absent.
- `fail_action: true` and `.zap/rules.tsv` stay the allowlist.
- No new scan payloads.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A release tag runs the same blocking ZAP baseline as Monday | CU78, #2004 | Changed |
| The baseline does not run on a pull request or a branch push | CU78, #2001 | Made explicit |
| Non-allowlisted alerts still fail the job | CU78, #1738 | Made explicit |

## Capabilities

### New Capabilities

- None. The notary HTTP API does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | yes | `dast-zap.yml` tag trigger |

### Surface area

- Entities: none
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients. A version tag starts one extra workflow.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | DAST row names the tag trigger |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | #2004 leaves the open list |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | DAST inventory and gap row |
| `docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md` | release-only table |
| `docs/200-architecture/208-devsecops/README.md` | `dast-zap.yml` trigger cell |
| `.claude/skills/notaire-cybersec/SKILL.md` | pull-request exclusion list |

## Out of Scope

- Authenticated ZAP.
- Running ZAP on `pull_request` or on every push to `main`.
- Changing `.zap/rules.tsv` or `fail_action`.
