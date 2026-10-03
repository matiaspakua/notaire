> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1058 (FRONTEND, CASO-DE-USO, audit-2026-09, priority:medium). Verified on
`origin/main` `9642a033`:

| Location | Finding |
|----------|---------|
| `AppSidebar.tsx` `navItems` | No `suplencias` / `reportes` entries; has `items`, `auditoria`, `administracion` |
| `dashboard/page.tsx` modules | No Suplencias/Reportes tiles |
| `messages/es.json` + `en.json` `navigation` | Missing `suplencias` / `reportes` keys |
| Pages exist | `dashboard/suplencias/page.tsx`, `dashboard/reportes/page.tsx` |
| Duplicates | `administracion/items` (301 lines) vs `items` (185); `administracion/auditoria` (73) vs `auditoria` (140) |
| Admin index | No links to items/auditoria/suplencias/reportes |
| E2E | TS-0070 uses `page.goto` for reportes/suplencias; TS-0060/TS-0043/TS-0091 deep-link admin duplicates |

Canonical choice (product): keep **richer** `/dashboard/items` and
`/dashboard/auditoria` as canonical (already in sidebar). Remove or redirect
admin duplicates. If `administracion/items` has unique UI the shorter
`dashboard/items` lacks, **merge unique bits into canonical first**, then delete
duplicate — do not leave two sources of truth.

## Goals / Non-Goals

**Goals:**

- Discoverable Suplencias + Reportes from sidebar (and optionally home tiles).
- One Items page, one Auditoría page.
- E2E proves nav discovery.

**Non-Goals:**

- Redesigning the entire sidebar IA.
- Implementing skipped #1146 filter UX on suplencias/reportes.
- Changing backend RBAC (#559) or admin edge deny (#1052) semantics beyond
  route cleanup.

## Decisions

1. **Add two `navItems` (+ i18n)** near related modules (e.g. after protocolo /
   before items) with Lucide icons consistent with neighbors.
2. **Optional home tiles** for parity with other modules — include if low cost;
   sidebar is the AC minimum.
3. **Redirects via `next.config` redirects or `page.tsx` redirect()** from old
   admin paths → canonical; prefer App Router `redirect()` in tiny replacement
   pages if that preserves bookmark compatibility without keeping 300-line
   duplicates.
4. **E2E helper**: add `navigateViaSidebar(label)` (or reuse existing) and update
   TS-0070 discovery steps; adjust TS-0091 / TS-0024 / TS-0060 / TS-0043 URLs to
   canonical paths.
5. **Serialize** with other Playwright-heavy PRs; implement after the named
   queue ahead of this stockpile.

## Riesgos / Trade-offs

- [Deleting richer admin/items] → lose UI — Mitigation: diff pages; merge unique
  controls into canonical before delete.
- [Role visibility] → wrong `adminOnly` — Mitigation: match who can use pages
  today; keep administración gated.
- [E2E flake] → Mitigation: reuse stable `data-testid` on nav links if needed.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Sidebar Suplencias | unit + E2E | nav unit test; TS-0070 / TS-0017 path |
| Sidebar Reportes | unit + E2E | nav unit test; TS-0070 / TS-0020 path |
| Canonical Items | unit/file + E2E | assert no duplicate page module; redirect |
| Canonical Auditoría | unit/file + E2E | same |
| E2E via nav | E2E | updated TS-0070 (and related) |

- New unit tests: nav items include hrefs + i18n keys present
- Coverage impact: frontend Vitest only; JaCoCo n/a

## Regression Strategy

- Existing tests affected: any E2E using `/dashboard/administracion/items` or
  `.../auditoria` must move to canonical or follow redirect.
- Full suite: `cd frontend && npm test && npm run lint && npm run typecheck`
- Playwright: `npx playwright test` for touched specs; heavy CI gate
- Legacy: do not recreate Swing

## Playwright Strategy

- Update TS-0070 supervised tour to open Reportes/Suplencias via sidebar.
- Update TS-0017 / TS-0020 setup to prefer nav when asserting discovery.
- Retarget TS-0091, TS-0024, TS-0043, TS-0060 away from duplicate admin URLs.
- Verify 320 / 768 / 1024 sidebar behavior for new labels.
- **Not n/a** — UI change; serialize Playwright-heavy PRs.

## Deployment Strategy

- Standard frontend deploy via CD after merge; no migration.

## Rollback Strategy

- Revert PR restores prior nav and duplicate pages. Redirect-only commits are
  easy to revert; if pages were deleted, revert restores files from git.
