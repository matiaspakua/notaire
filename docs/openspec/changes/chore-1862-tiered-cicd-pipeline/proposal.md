# Harden the local CI/CD pre-PR pipeline

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1862 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/1862_tiered_cicd_run_pipeline_sonarqube` |
| Gate 1 status | passed |

## Objetivo

The local pre-PR command is the evidence gate for CU76, but its new mode flags
do not consistently enforce their documented contract.  In particular,
`--with-infra` may silently skip SonarQube and `--fast` still starts the stack
and runs DAST, defeating its purpose as a quick feedback path.

## What Changes

- Make `--with-infra` require a successful SonarQube quality-gate run.
- Make `--fast` omit the application-stack and DAST phases while retaining the
  static checks and `preflight.sh --fast`.
- Reject contradictory Sonar mode arguments and document the precise mode
  contracts in the permanent CI/CD plan.
- Add regression coverage for the command-line contracts and run the complete
  dashboarded pipeline as pre-PR evidence.

## Reglas de negocio

<!-- The business rules this change introduces, alters, or makes explicit. State
     each rule in one sentence. The normative, testable form of each rule belongs
     in the delta spec as a Requirement + Scenarios; here, name them and cite the
     Use Case or business document they come from.
     If the rule is new and no Use Case covers it, say so - a missing Use Case
     must be created before Gate 1 passes. -->

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A requested local quality gate MUST be blocking; it cannot become a silent skip. | CU76; `CONSTITUTION.md` §6 | Made explicit |
| Fast feedback MUST not start services or execute server-backed DAST. | CU76; RNF-11 | Made explicit |
| Mutually exclusive command modes MUST fail before executing a quality gate. | CU76; RNF-08 | New |

## Capabilities

### New Capabilities
- `local-cicd-pipeline`: Defines the observable contracts for the local
  dashboarded pre-PR pipeline modes.

### Modified Capabilities
None. This is a new behavior contract; existing archived pipeline specs remain
historical records.

## Impact Analysis

### Módulos afectados

<!-- Mark every module this change touches. Delete rows that do not apply, but do
     not delete the table: an empty table means the analysis was not done. -->

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No application behavior changes. |
| `frontend` | no | No UI behavior changes. |
| `frontend-swing` | no | Retired client is unaffected. |
| `infra` / observability | yes | `run-sonar.sh` is invoked only through a stricter caller contract. |
| CI/CD (`.github/workflows`) | no | This PR documents a remote-CI roadmap; it does not change workflows. |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities: none.
- Endpoints: none; `http://localhost:9000/api/system/status` is a local health probe only.
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: existing SonarQube configuration only; no new key or secret.
- Dependencies: none.
- API compatibility: no API-client breaking change.

### Architecture review

The change composes existing stack, security, preflight and Sonar scripts rather
than duplicating their behavior. It narrows CLI orchestration semantics only;
no new architectural decision or ADR is required.

## Documentation Impact

<!-- Which PERMANENT documentation must be updated before merge (Gate 3).
     Permanent docs are the single source of truth; this proposal is not.
     Do not duplicate their content here - name the file and what changes in it.
     Mark documents to be archived into docs/000-archive/. -->

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/PROJECT-CICD-PIPELINE-IMPROVEMENT.md` | Correct the local mode guarantees and distinguish implemented behavior from the remote-CI roadmap. |
| `CHANGELOG.md` | n/a — developer tooling behavior is not a released product change. |

## Out of Scope

This PR does not alter GitHub Actions workflow topology, runner timing,
container path filters, or required-status-check policy. Those planned remote
optimizations remain unchecked in
`docs/300-development/PROJECT-CICD-PIPELINE-IMPROVEMENT.md` and are to be
implemented in independently testable follow-up work under #1862.
