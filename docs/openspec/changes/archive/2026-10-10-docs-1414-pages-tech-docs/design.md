> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`github-page/` is a Next.js static export (`output: "export"`, `basePath: /notaire`) deployed by
`deploy-github-page.yml`. Technical docs live under `docs/` and ADR-026 modules under
`workspace/modules.yaml`, but Pages has no Docs surface. Diagram sources are 116 PlantUML files
plus sparse Mermaid. `#1197` Phase 0 requires a metrics baseline script that does not yet exist
(`scripts/` is forbidden by #1307 — use `workspace/ci/`).

## Goals / Non-Goals

**Goals:**
- Docs tab on Pages for module ownership, architecture, testing, DevSecOps.
- Mermaid as canonical active-diagram language (ADR-027).
- MODULE-OWNERSHIP.md + metrics baseline for #1197 Phase 0.
- Fix SAD Project #4 stale link.

**Non-Goals:**
- Split repositories or create `notaire-docs`.
- Convert all 116 `.puml` files in this PR.
- Change `deploy-github-page.yml`.
- Close #1197 (Owner decision still open).

## Decisions

1. **Curated Docs routes** — React pages under `github-page/app/docs/` with content sourced from
   committed Markdown/TS constants (not a full docs CMS). Deep links to GitHub for full SAD/ADR
   bodies when pages summarize.
2. **Mermaid client render** — `mermaid` npm package; client components only (static export safe).
3. **Metrics under `workspace/ci/repo-metrics.py`** — ADR-026 placement; path noted vs #1197 AC
   `scripts/repo-metrics.py`.
4. **One PR closes #1414–#1417** — atomic prep slice; Refs #1197 / #1256 / #921.

## Riesgos / Trade-offs

- [Heavy CI while #1410–#1412 open] → Docs/Pages PR is non-product; serialize merges via heavy gate.
- [PlantUML archive debt] → Policy forbids new `.puml` in active docs; migration is follow-up.
- [Pages content drift vs `docs/`] → Summaries + GitHub deep links; MODULE-OWNERSHIP is SSOT in docs.

## Testing Strategy

- `python3 -m unittest workspace.tests.test_repo_metrics` (or path discovery).
- `cd github-page && npm ci && npm run build`.
- `bash workspace/sdlc/validate-sdlc-plan.sh docs-1414-pages-tech-docs`.
- Manual: Docs tab + Mermaid diagrams after deploy.
- Product JUnit / Bruno / Playwright: n/a.

## Regression Strategy

- Existing: `test_modules_manifest.py`, `test_docs_links.py`, `test_scripts_layout.py` must stay green.
- Full product suite: not required for this change.

## Observability / Security / Data

- No new secrets, endpoints, or schema.
- Pages remains public marketing + docs; no JWT or PII.

## Playwright Strategy

No product UI change; n/a. Public Pages Docs tab verified by Next build and post-merge
`Deploy GitHub Page` workflow smoke of `/docs/`.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge PR; Pages auto-deploys after green CI on `main`
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open https://matiaspakua.github.io/notaire/docs/ and confirm Modules / Architecture / Testing / Security

## Rollback Strategy

- Revert the PR; documentation / static site only. Metrics script and ADR-027 leave with the revert.

## Migration / Rollout

None beyond the standard Pages deploy after merge. PlantUML bulk migration is out of scope.
