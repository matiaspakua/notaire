# Local security gates in the AI SDLC

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1500 |
| Use Case | CU78 — Security and Compliance |
| Branch | `cursor/local-security-sdlc-81d6` |
| Gate 1 status | passed |

## Objetivo

The security program from #307 is documented and the secret scan fails CI, but
a workstation can still open a pull request without running threat-model, SAST,
SCA, or DAST cases. This change makes that case catalog a blocking phase of the
pre-PR pipeline and of the AI SDLC instructions.

## What Changes

- Add `security/cases/catalog.yml` as the security test schema (threat model,
  secrets, SAST, SCA, DAST, policy, SDLC wiring).
- Add `security/run_gates.py`, a Semgrep ruleset, a DAST baseline, and
  `security/tools/ensure_local_tools.sh` (Semgrep 1.180.0, Trivy 0.75.0).
- Run the local profile from `workspace/sdlc/run_pipeline.sh` and
  `workspace/sdlc/preflight.sh`. CI runs the same catalog and keeps CodeQL and
  OWASP ZAP.
- Require the gate from `AGENTS.md`, `CONSTITUTION.md`, the agent card, the
  OpenSpec agent, and the fleet architecture.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A change that affects authentication, authorization, personal data, secrets, dependencies, or a public endpoint updates the threat model and the security case catalog in the same change | CU78 | Made explicit |
| The local security case catalog must pass before a pull request is opened | CU78 | New |
| CI keeps the same cases and may use a deeper tool for SAST and DAST | CU78 | New |
| Trivy findings do not fail the build until the remediation board exists | CU78, SR-13, #282 | Made explicit |

## Capabilities

### New Capabilities

- `local-security-gates`: the case catalog, the local runner, and the pipeline phase that reports each case

### Modified Capabilities

- None. The #307 policy requirements stay as written. This change adds the local execution schema.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No product code |
| `frontend` | no | No product UI |
| `infra` / observability | no | The gate report is a local HTML file; Grafana is unchanged |
| CI/CD (`.github/workflows`) | yes | Security job installs Semgrep and runs the catalog |
| `security` | yes | Catalog, runner, rules, baseline, tool installer |
| `workspace` | yes | `run_pipeline.sh` and `preflight.sh` |
| `docs` | yes | Program, toolchain, monitoring, CU78, OpenSpec |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: Semgrep and Trivy are installed on the workstation under `security/.tools/`, not as application dependencies

### Architecture review

The change follows the existing security module and the pipeline composition
rule: `run_pipeline.sh` calls `security/run_gates.py` instead of reimplementing
checks. No ADR. The threat model and the SAD are unchanged because no control
in the product moved.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Local gate and the case catalog |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | Semgrep and the DAST baseline as selected local tools |
| `docs/200-architecture/206-security/MONITORING.md` | Local report and the CI artifact |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Pointer to the catalog |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Fleet item 10 runs the local gate |
| `CONSTITUTION.md`, `AGENTS.md` | The pre-PR gate includes the security cases |
| `CHANGELOG.md` | One bullet under the existing Security heading |

## Out of Scope

- Failing the build on Trivy findings (#282, #712)
- Promoting ZAP from warn-first to a failing PR gate (#281)
- Object-level authorization (SR-11) and the Escribano product decision (SR-16)
- Installing Gitleaks as a second secret scanner
- External Alertmanager paging (#1465)
