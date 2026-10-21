# Pages "AI Factory" section — the AI SDLC that builds Notaire

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1934 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1934_ai_factory_pages` |
| Gate 1 status | passed |

## Objetivo

The public site explains the product (story + Docs tab) but not the process that builds it. The Owner asked for
a complete, clearly separate AI SDLC section: the "AI factory" that builds Notaire, with diagrams and step-by-step
explanations, grounded in the repository.

## What Changes

- New route tree `github-page/app/ai-sdlc/` (own layout/chrome, overview + eight sub-pages).
- `github-page/lib/ai-sdlc-content.ts`: Mermaid diagrams and cited sources; `lib/ai-sdlc-facts.ts`: counts read from the repo at build time.
- Story `Nav` and `DocsChrome` link the factory.
- Stale docs: `FLEET-ARCHITECTURE.md` §9 and `ENVIRONMENT-CHECKLIST.md` §7 still say "rebase" for stale tips (the rule is merge `origin/main`, never rebase a pushed branch, as `CI-MERGE-GATE.md` states); `AIEra.tsx` ADR count; `github-page/README.md` was create-next-app boilerplate; `lib/releases.json` still showed v0.2.3 after the v0.2.4 cut.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The AI SDLC is presented as the factory, apart from the product docs | Owner request (#1934) | New |
| Public pages cite real files; nothing is invented | CONSTITUTION §8, skill contracts (§5) | Made explicit (test guard) |
| Stale pushed tips are fixed by merging `origin/main`, never rebase | `CI-MERGE-GATE.md` | Made consistent across fleet docs |

## Capabilities

### New Capabilities

- `github-pages-ai-factory`

### Modified Capabilities

- none

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | no | – |
| `docs` | yes | fleet docs wording; OpenSpec change |
| `workspace` | yes | `workspace/tests/test_pages_ai_sdlc.py`; `github-page/` (Pages site) |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (Mermaid is already a `github-page` dependency)

### Architecture review

Follows ADR-027 (Mermaid only) and the existing static-export Pages app. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Documentation entry |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | §9 item 7: merge `origin/main`, not rebase |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | §7 stale-tip row: merge, not rebase |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Context and contention table: "rebased" wording → merge `main` |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Link the public AI Factory section |
| `github-page/README.md` | Replace boilerplate with how the site is built and deployed |

## Out of Scope

- Amending `CONSTITUTION.md` (Owner-only; §13 still names the site as story + `/docs/`).
- Phase 2 of the label rename (#1860) in the Constitution and docs.
