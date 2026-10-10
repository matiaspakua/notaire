> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`github-page/` already hosts curated Docs routes (modules, architecture, testing, security) from
#1414. Home copy mentions business docs, but there is no `/docs/business/` route. Business SSOT
lives under `docs/100-business/`.

## Goals / Non-Goals

**Goals:**
- Curated Business Docs page with deep-links to requirements, use cases, actors, traceability, manuals.
- Nav + home card parity with other Docs sections.
- Unit guard against silent removal.

**Non-Goals:**
- In-app Markdown CMS for CU documents.
- LICENSE or ADR-022 Owner decisions.
- Changing `deploy-github-page.yml`.

## Decisions

1. **Deep-link pattern** — Same as SAD/ADR pages: summarize + link to GitHub blob paths under
   `docs/100-business/`.
2. **Chrome label** — Keep “Technical Docs” eyebrow for the shell (existing brand); Business is an
   additional nav item under the knowledge-base home that already claims business + engineering.
3. **Guard in `workspace/tests/`** — File-content assertions (pattern of `test_adr022_owner_decision_pack.py`).

## Riesgos / Trade-offs

- [Nav clutter] → One extra pill; five total still fits the chrome wrap.
- [Spanish filenames with special characters] → Use GitHub blob URLs with encoded paths where needed.
- [Issue close 403] → PR uses `Closes #1441` in body; agents may not close manually.

## Testing Strategy

| Acceptance Criterion | Test level | Test class / file |
|----------------------|------------|-------------------|
| Business nav + home card + page deep-links | unit | `workspace/tests/test_pages_business_docs.py` |
| Static export still builds | build | `cd github-page && npm run build` |

TDD: write the unit guard first and observe FAIL on `main` tip / before page exists.

## Regression Strategy

- Existing Pages Docs pages unchanged functionally.
- `test_adr022_owner_decision_pack` must stay green.

## Observability / Security / Data

- No secrets, endpoints, or schema.
- Public deep-links only.

## Playwright Strategy

- Product Playwright: n/a (no `frontend/` change).
- Pages smoke: after deploy, `GET /notaire/docs/business/` returns 200 and shows Business heading.

## Deployment Strategy

- No new workflow. After merge to `main`, `CI - Build, Test & Security` success triggers
  `deploy-github-page.yml` via `workflow_run` (unchanged).
- Static export under `github-page/out` with `basePath: /notaire`.

## Rollback Strategy

- Revert the merge commit on `main`; the next successful main CI run redeploys the previous
  Docs surface without Business.
- No database, secret, or API migration to undo.
