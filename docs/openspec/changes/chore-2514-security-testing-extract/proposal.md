# Extract security and testing fleets for multi-team governance

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2514 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/2514_security_testing_extract` |
| Gate 1 status | passed |

## Objetivo

Product, infrastructure, QA and cybersecurity need to evolve in parallel without
blocking each other. Vendored `security/` and `testing/` trees still couple those
fleets to the product PR history. Empty homes already exist at
`matiaspakua/notaire-security` and `matiaspakua/notaire-testing`. Hybrid sync:
security as a submodule + gate contract; testing as reusable workflows pinned by SHA.

## What Changes

- Extract `security/` history into https://github.com/matiaspakua/notaire-security.git
  and replace the vendored tree with a git submodule at `security/`.
- Move program/procedure docs (CYBERSECURITY-PROGRAM, ASSESSMENT, SECURITY-TOOLS,
  MONITORING, HTTPS-TLS-RUNBOOK, INSTALLATION-ISOLATION-CHECKLIST) into the
  security repo; keep code-tied threat models in `docs/200-architecture/206-security/`.
- Extract `testing/` history into https://github.com/matiaspakua/notaire-testing.git;
  publish reusable workflows `e2e.yml` and `database-vv.yml`; pin callers from Notaire.
- Parameterize seams with `NOTAIRE_APP_ROOT` / `NOTAIRE_TESTING_ROOT`.
- Add `docs/300-development/MULTI-TEAM-CONTRACT.md` and update REPO-SPLIT / README /
  AGENTS / Pages for the five-repo topology.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Security gates, policy and program docs live in `matiaspakua/notaire-security`; Notaire consumes them as submodule `security/` | #2514; REPO-SPLIT-PLAN; hybrid C | New |
| Code-tied threat models (STRIDE/LINDDUN/diagrams) remain in the product under `docs/200-architecture/206-security/` | #2514; docs option 3 | New |
| Product-level E2E/database suites live in `matiaspakua/notaire-testing`; Notaire pins reusable workflows with `app-ref` | #2514; REPO-SPLIT-PLAN Phase 3 | New |
| Bruno, pg-integration, unit/Vitest/JUnit, and root GitHub security controls (SECURITY.md, CODEOWNERS, Dependabot, CodeQL, Trivy) stay in the product | #2514; ADR-024; #1190 | Made explicit |
| Four fleets (product, infra, QA, Sec) publish/consume via declared contracts (`app-ref`, submodule pins, SYSTEM_DESCRIPTOR) without mutual merge blocking | #2514; MULTI-TEAM-CONTRACT | New |

## Capabilities

### New Capabilities

- `security-fleet-contract`: security product repo + Notaire submodule consumption and program-doc ownership.
- `testing-reusable-workflows`: testing product repo + Notaire-pinned reusable E2E/database workflows with `app-ref`.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | already a submodule |
| `security` | yes | becomes submodule of notaire-security; program docs move |
| `testing` | yes | extracted; callers pin reusable workflows; stub README |
| CI/CD (`.github/workflows`) | yes | e2e/database callers; checkout-with-submodules for security |
| `workspace` (harness) | yes | modules, preflight, CU-API matrix roots, seam tests |
| `github-page` | yes | topology / satellite links |
| `docs` | yes | 206-security slim, MULTI-TEAM-CONTRACT, REPO-SPLIT Phase 3 |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `NOTAIRE_APP_ROOT`, `NOTAIRE_TESTING_ROOT` (no new secrets)
- Dependencies: git submodule `security`; workflow pin to `notaire-testing`

### Write set

- `.gitmodules`
- `security` (gitlink)
- `testing/README.md` (stub; tree removed)
- `.github/workflows/playwright-e2e.yml`, `.github/workflows/database-vv.yml`
- `.github/actions/checkout-with-submodules/**`
- `workspace/modules.yaml`
- `workspace/sdlc/preflight.sh`, `workspace/sdlc/validate-cu-api-matrix.py`
- `workspace/tests/**` seam guards
- `docs/200-architecture/206-security/**`
- `docs/300-development/MULTI-TEAM-CONTRACT.md`
- `docs/300-development/REPO-SPLIT-PLAN.md`
- `README.md`, `AGENTS.md`
- `github-page/**`
- `docs/openspec/changes/chore-2514-security-testing-extract/**`
- External: `matiaspakua/notaire-security`, `matiaspakua/notaire-testing`

### Architecture review

Aligns with ADR-024 / REPO-SPLIT-PLAN Phase 3 and hybrid C (security submodule +
testing reusable workflows). No new ADR required; permanent docs record the cut
and program-doc ownership (option 3).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `README.md` | five-repo topology; security submodule; testing pin |
| `AGENTS.md` | submodule init for security; testing pin notes |
| `docs/300-development/REPO-SPLIT-PLAN.md` | Phase 3 testing + security done / as-built |
| `docs/300-development/MULTI-TEAM-CONTRACT.md` | new — four-fleet sync contract |
| `docs/200-architecture/206-security/README.md` | what stayed vs notaire-security |
| GitHub Pages modules/architecture | satellite URLs |
| `CHANGELOG.md` | n/a — generated by release-please |

## Out of Scope

- Moving Bruno, pg-integration, or unit/Vitest/JUnit suites.
- Separating backend/frontend.
- Moving CodeQL/Trivy/Dependabot/SECURITY.md out of the product repo.
- Mass issue transfer between repositories (optional Owner follow-up).
- Extracting `local-ai/`.
