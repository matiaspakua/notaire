# Notaire SDLC Skills

This directory combines project-specific skills with generic lifecycle skills. The Constitution and OpenSpec schema remain the policy sources of truth; skills provide executable guidance and artifact templates.

## Composition

| Lifecycle concern | Skill | Notaire owner/artifacts | Gate |
|---|---|---|---|
| Business analysis and backlog | `analyst`, `product-owner`, `delivery-maturity-roadmap` | `docs/100-business/`, roadmap and Issue | 1 |
| Traceability | `devsecops-traceability` | `openspec/changes/*/traceability.md`, `docs/100-business/104-traceability/` | 1-5 |
| Architecture and decisions | `architecture-decision-design`, `plantuml` | `docs/200-architecture/`, `docs/200-architecture/202-ADR/`, diagrams | 1 |
| Threats and security requirements | `secure-threat-modeling`, `backend`, `programming`, `devops` | security docs, `SR-*`, SAST/SCA/DAST evidence | 1-4 |
| QA strategy and tests | `qa-automation-strategy`, `testing`, `maven-build`, `api-rest` | MTP, JUnit, Vitest, Bruno, Playwright | 2-3 |
| API contracts | `api-contract-testing`, `api-rest` | OpenAPI/Swagger, Bruno and UI traceability | 2-3 |
| CI/CD quality gates | `ci-cd-quality-gates`, `devops` | `scripts/preflight.sh`, `scripts/run_pipeline.sh`, CI workflows | 3-4 |
| Operations and incidents | `operations-observability-readiness`, `devops` | `infra/`, runbooks, SLOs, incident/postmortem evidence | 3-5 |
| Specification lifecycle | OpenSpec vendor skills + `openspec-triage` | `openspec/changes/`, `openspec/specs/` | 1-5 |

## Selection rule

Use the narrowest project-specific skill for implementation details and the generic skill for lifecycle decisions, artifact quality and cross-cutting controls. Load both when a task crosses boundaries. Do not duplicate the Constitution or permanent documentation inside an OpenSpec artifact.

## Standard sequence

`delivery-maturity-roadmap` -> `analyst`/`product-owner` -> `openspec-triage` -> `openspec-propose` -> `architecture-decision-design` + `secure-threat-modeling` -> `qa-automation-strategy` + `api-contract-testing` -> `openspec-apply-change` -> `ci-cd-quality-gates` -> `operations-observability-readiness` -> `openspec-archive-change`.

All generic skills include domain references and evaluation manifests. Their examples are provider-neutral; adapt commands to the actual Notaire toolchain documented in `CONSTITUTION.md` and `openspec/NOTAIRE-ADAPTATIONS.md`.
