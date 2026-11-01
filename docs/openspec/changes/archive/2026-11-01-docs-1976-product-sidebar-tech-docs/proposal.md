# Pages — Product section with a sidebar, and architecture internals in the Technical docs

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1976 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1976_product_sidebar_tech_docs` |
| Gate 1 status | passed |

## Objetivo

The Owner noticed that the Product section of the GitHub Pages site has no side table of contents, unlike the
Technical docs and the AI Factory, and asked for its sub-pages to share that structure. He also asked for the
technical docs to explain the architecture internals — the hexagonal architecture, Flyway, the OpenAPI contract and
the Bruno API suite — after a full assessment of the technologies and patterns Notaire uses, and for `/docs` to be
updated where the assessment finds it stale.

## What Changes

- Product becomes a real section of four pages under `/product/` in the shared `DocShell` (sidebar, breadcrumbs,
  reading progress, previous/next): Overview, Story, Platform, Numbers. The story's components move there.
- `/` becomes a short landing page that opens the three sections.
- Technical docs gain a "Technology stack" page and an "Architecture internals" group: Hexagonal architecture,
  Database & Flyway, API contract & Bruno. Versions, counts, migrations, ADRs and workflows are read from the
  repository at build time by `github-page/lib/tech-facts.ts`.
- The Architecture page lists every ADR with its status, read from `docs/200-architecture/202-ADR/`.
- A permanent `docs/200-architecture/TECHNOLOGY-STACK.md` describes every major technology and pattern and points to
  the file that pins each version. Stale versions in `README.md`, the SAD and the deployment guide are corrected.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every section's pages share one shell with a sidebar | Owner request (#1976) | Extended to Product |
| Versions and counts on the site come from the repository | CONSTITUTION §7, §8 | Extended to the Technical docs |
| Every repository path the site cites exists | #1934 citation test | Extended to the new pages |

## Capabilities

### New Capabilities

- `github-pages-technical-docs`

### Modified Capabilities

- `github-pages-site-shell`

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | no | – |
| `docs` | yes | `TECHNOLOGY-STACK.md`; SAD, deployment guide, architecture index; OpenSpec change; CHANGELOG; CONSTITUTION §13 |
| `workspace` | yes | `workspace/tests/test_pages_tech_docs.py`, `test_pages_site_shell.py`; `github-page/` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Static-export Pages app and documentation only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/TECHNOLOGY-STACK.md` | New: every technology and pattern, and where its version is pinned |
| `docs/200-architecture/README.md` | Link the stack document |
| `docs/200-architecture/201-SAD/sad.md` | Spring Boot version, Flyway range |
| `docs/200-architecture/209-deployment/README.md` | Java version |
| `README.md` | Spring Boot version |
| `CHANGELOG.md` | Documentation entry |
| `CONSTITUTION.md` | §13 site row: Product lives at `/product/` |
| `github-page/README.md` | Sections and `lib/tech-facts.ts` |

## Out of Scope

- Interactive diagrams and the portfolio landing (#1977).
- `backend-api/src/main/resources/db/migration/README.md` is stale too, but it sits under production resources;
  tracked as its own issue.
