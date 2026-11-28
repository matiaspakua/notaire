# Extract infrastructure product and pin AI SDLC as a submodule

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2512 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/2512_infra_aisdlc_extract` |
| Gate 1 status | passed |

## Objetivo

Notaire still vendors `infra/` and `framework/ai-sdlc/`, which blocks independent
product homes already published at `matiaspakua/infrastructure` (empty) and
`draclab/ai_sdlc` (`aisdlc-v1.1.0`). Phase 1 of the repository split and the AI
SDLC boundary require Notaire to consume those products via git submodules and
to publish a system descriptor so a multi-system infrastructure stack can manage
Notaire through scrape/OTLP contracts.

## What Changes

- Extract `infra/` history into https://github.com/matiaspakua/infrastructure.git,
  genericize for multi-system use (`SYSTEM_DESCRIPTOR` / schema / render), and
  replace the vendored tree with a git submodule at `infra/`.
- Add Notaire `ops/system.yaml` describing network, scrape targets, OTLP resource
  attributes and probes (runtime publish remains pull-based).
- Replace vendored `framework/ai-sdlc/` with a git submodule of
  `https://github.com/draclab/ai_sdlc.git` pinned to tag `aisdlc-v1.1.0`.
- Extend CI submodule checkout for infrastructure (public) and ai-sdlc (private).
- Stop publishing kit release tags from Notaire (`aisdlc-kit.yml` verify-only).
- Update README, AGENTS, architecture docs and GitHub Pages for the three-repo topology.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Observability and deploy tooling for systems live in `matiaspakua/infrastructure`; Notaire consumes them as submodule `infra/` and never vendors a second copy | #2512; REPO-SPLIT-PLAN Phase 1; #1179 | New |
| Notaire publishes a system descriptor at `ops/system.yaml` plus runtime metrics/logs/traces so infrastructure can manage the system | #2512; ADR-016 | New |
| Notaire consumes the AI SDLC kit only as a git submodule at `framework/ai-sdlc` pinned to a published release tag; kit releases are owned by `draclab/ai_sdlc` | #2512; `draclab/ai_sdlc` BOUNDARY.md | New |

## Capabilities

### New Capabilities

- `system-infrastructure-contract`: multi-system infrastructure product + Notaire system descriptor and submodule consumption.
- `ai-sdlc-kit-adoption`: Notaire obtains the kit from `draclab/ai_sdlc` at a pinned release.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | yes | becomes submodule of infrastructure product; system descriptor in Notaire |
| `ai-sdlc-framework` | yes | becomes submodule of `draclab/ai_sdlc@aisdlc-v1.1.0` |
| CI/CD (`.github/workflows`) | yes | checkout-with-submodules; aisdlc-kit publish ownership |
| `workspace` (harness) | yes | modules, stack scripts, seam/adoption tests |
| `github-page` | yes | topology / product links |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `SYSTEM_DESCRIPTOR` / ops descriptor (no new secrets)
- Dependencies: git submodules `infra`, `framework/ai-sdlc`

### Write set

- `.gitmodules`
- `infra` (gitlink)
- `framework/ai-sdlc` (gitlink)
- `ops/system.yaml`
- `ops/**` seam tests as needed
- `README.md`, `AGENTS.md`
- `docs/300-development/REPO-SPLIT-PLAN.md`
- `docs/200-architecture/207-monitoring/**`, `209-deployment/**`, `306-ai-sdlc-framework/**`
- `github-page/**` topology copy
- `.github/actions/checkout-with-submodules/**`
- `.github/workflows/aisdlc-kit.yml`
- `workspace/tests/**` seam and adoption guards
- `docs/openspec/changes/chore-2512-infra-aisdlc-extract/**`
- External: `matiaspakua/infrastructure` (extract + genericization)

### Architecture review

Aligns with ADR-024 / REPO-SPLIT-PLAN Phase 1 (submodule consumption instead of
`NOTAIRE_INFRA_DIR` stub-only). No new ADR required for this packaging cut;
permanent docs record the deviation (submodule at `infra/`).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `README.md` | three-repo topology; recurse-submodules; infra + ai-sdlc product links |
| `AGENTS.md` | submodule init for infra and ai-sdlc |
| `docs/300-development/REPO-SPLIT-PLAN.md` | Phase 1 status / submodule approach |
| `docs/200-architecture/207-monitoring/README.md` | system contract pointer |
| `docs/200-architecture/209-deployment/README.md` | infra product path |
| `docs/300-development/306-ai-sdlc-framework/**` | kit lives at `draclab/ai_sdlc` |
| GitHub Pages modules/architecture/ai-sdlc pages | external product URLs |
| `CHANGELOG.md` | n/a — generated by release-please |

## Out of Scope

- Generic multi-tenant Sonar productization beyond Notaire fixture.
- Alertmanager external receivers (Slack/PagerDuty).
- Extracting `local-ai/` or other REPO-SPLIT-PLAN satellites.
- Making `draclab/ai_sdlc` public.
- History purge of `deprecated/`.
