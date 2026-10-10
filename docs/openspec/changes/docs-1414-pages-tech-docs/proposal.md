# Publish technical documentation on GitHub Pages and finish #1197 Phase 0 doc readiness

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1414 (also Closes #1415, #1416, #1417; Refs #1197, #1256, #921) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-1197-pages-modules-cf98` |
| Gate 1 status | draft |

## Objetivo

GitHub Pages is a marketing timeline only; SAD, ADRs, module ownership (ADR-026), testing and
DevSecOps process docs are invisible there. Owner direction (2026-10-10) is to prepare the monorepo
for [#1197](https://github.com/matiaspakua/notaire/issues/1197) with clear folder ownership (no
sibling repos yet), unify diagram language, and render technical documentation on Pages.

## What Changes

- Add a **Docs** tab/route on `github-page/` that renders module ownership, architecture (SAD/ADR),
  testing process, and DevSecOps process pages (static export compatible).
- Add `docs/300-development/MODULE-OWNERSHIP.md` with a Mermaid dependency map from
  `workspace/modules.yaml`.
- Add **ADR-027** adopting Mermaid as the sole diagram language for *active* documentation; document
  PlantUML migration policy under `docs/200-architecture/204-diagrams/README.md`.
- Fix SAD stale link to missing Project #4 → Delivery Board #1; bump SAD changelog.
- Add `workspace/ci/repo-metrics.py` + baseline under `docs/300-development/REPO-METRICS-BASELINE.md`
  (#1197 P0.1 / #1256 / #1417).
- Link indexes (`docs/300-development/README.md`, ADR index, REPO-SPLIT-PLAN, AGENTS module map).
- `CHANGELOG.md` Unreleased entry. Deploy workflow unchanged.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public Pages exposes a Docs surface for architecture and process knowledge | #1414, CU76 | New |
| Active documentation diagrams use Mermaid only | #1415, CU76 | New |
| Every Foreman module has a discoverable ownership contract | #1416, ADR-026, #1197 | Made explicit |
| Topology decisions use a reproducible metrics baseline | #1417, #1256, #1197 AC | Made explicit |

## Capabilities

### New Capabilities

- `github-pages-tech-docs`: Pages Docs tab renders curated technical documentation.
- `module-ownership-map`: permanent module ownership doc + Mermaid map aligned with `modules.yaml`.
- `diagram-language-mermaid`: Mermaid is canonical for active docs (ADR-027).
- `repo-metrics-baseline`: reproducible repository metrics script and committed baseline.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `github-page` | yes | Docs routes, Nav, Mermaid diagrams |
| `docs` | yes | MODULE-OWNERSHIP, ADR-027, SAD fix, baseline, diagrams README |
| `workspace` | yes | `ci/repo-metrics.py` + unit test |
| `security` / `testing` / `infra` | no code | linked from Docs pages |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: `mermaid` npm package in `github-page/` only

### Architecture review

ADR-027 (diagram language). No production API change. Aligns with ADR-024 Phase 0 and ADR-026.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/MODULE-OWNERSHIP.md` | new |
| `docs/300-development/REPO-METRICS-BASELINE.md` | new |
| `docs/200-architecture/202-ADR/ADR-027-mermaid-diagrams.md` | new |
| `docs/200-architecture/202-ADR/README.md` | index row |
| `docs/200-architecture/204-diagrams/README.md` | new policy |
| `docs/200-architecture/201-SAD/sad.md` | Project #4 → #1; diagram policy note |
| `docs/300-development/README.md` | links |
| `docs/300-development/REPO-SPLIT-PLAN.md` | link ownership + metrics |
| `CHANGELOG.md` | Unreleased |
| `github-page/` | Docs tab |
