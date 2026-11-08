# Security assessment, portfolio explorer, and agent gates

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2003 |
| Use Case | CU78 – Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/security-assessment-portfolio-6eaf` |
| Gate 1 status | passed |

## Objetivo

The security program still described pull-request CI and unenforced module grants after #2001 and #1683. This change records the evidence, explains it on the portfolio, and tells agents how to run the local gates.

## What Changes

- Add `ASSESSMENT-2026-11-05.md` and a YAML STRIDE register checked by unittest.
- Correct `CYBERSECURITY-PROGRAM.md`, `SECURITY-TOOLS.md`, the system threat model, and the OWASP map.
- Add the GitHub Pages Security section and correct the AI Factory cadence copy.
- Add `.claude/skills/notaire-cybersec/` and `SECURITY-GATES.md`.
- File follow-up issues #2004–#2011. Do not implement them here.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Personal data and notarial records stay behind authentication and a module grant | CU78, ADR-029, #1683 | Made explicit |
| A pull request is not a GitHub security scan | CU78, #2001 | Made explicit |
| The public site does not publish live scan output | CU78 | New |

## Capabilities

### New Capabilities

- `security-posture-register`: the checked-in STRIDE register and the public cadence stay aligned with the markdown model and the workflow triggers.

### Modified Capabilities

- `github-pages-site-shell`: the primary navigation includes Security, and the section uses the shared document shell.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No product code |
| `frontend` | no | No product UI |
| `frontend-swing` | no | Retired |
| `infra` / observability | no | Existing report service unchanged |
| CI/CD (`.github/workflows`) | no | Triggers stay release-only. Docs and tests describe them |
| `security` | yes | Register test, cadence test, catalog flag, CodeQL trigger assertion |
| `github-page` | yes | Security routes and corrected factory copy |
| `docs` | yes | Assessment, program, threat model, AI Factory procedure |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No new architecture. The PlantUML trust-boundary diagram gains the module-grant filter that already exists. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | New assessment |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Cadence and control status |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | Tools evaluated and where they run |
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | Module grants and open issues |
| `docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md` | Agent procedure, separate from the product model |
| `CHANGELOG.md` | Unreleased note for #2003 |

## Out of Scope

ZAP on tags (#2004), security headers (#2005), the authorization matrix (#2006), deeper LINDDUN (#2007), a findings pipeline (#2008), Threat Dragon CI (#2009), a general rate limit (#2010), and a CodeQL fail decision (#2011). The live `protect-main` ruleset stays an Owner action (#2001).
