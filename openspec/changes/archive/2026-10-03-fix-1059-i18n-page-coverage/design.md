> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1059 (audit-2026-09): several `page.tsx` files never call
`useTranslations`, and `login/page.tsx` still hardcodes connection/lockout/
validation/welcome/forgot/footer Spanish. English locale switching (TS-0040)
therefore leaves those surfaces in Spanish.

Verified inventory (implement scope):

| Surface | Path | Status |
|---------|------|--------|
| Workflows list | `dashboard/administracion/workflows/page.tsx` | no i18n |
| Workflows editor | `dashboard/administracion/workflows/[id]/page.tsx` | no i18n |
| Suplencias | `dashboard/suplencias/page.tsx` | no i18n |
| Reportes | `dashboard/reportes/page.tsx` | no i18n |
| Roles | `dashboard/administracion/roles/page.tsx` | partial (`common` edit/delete only) |
| Items (canonical) | `dashboard/items/page.tsx` | no i18n; existing `items` keys outdated |
| Auditoria leftover | `dashboard/auditoria/page.tsx` | mostly done; `"Todos los módulos"` leftover |
| Login leftovers | `login/page.tsx` | core form translated; leftovers hardcoded |
| Redirect stubs | `/`, `/auditoria`, `administracion/items`, `administracion/auditoria` | exempt |

Reference pattern: `administracion/usuarios/page.tsx` —
`useTranslations("administracion.usuarios")` + `useTranslations("common")`.
ADR-015 already mandates next-intl + synced catalogs + `i18n.test.ts`.

## Goals / Non-Goals

**Goals:**

- All user-visible strings on gap UI pages + login leftovers live in
  `messages/{es,en}.json` and are consumed via next-intl.
- Unit tests fail on missing required namespaces/keys (AC).
- Optional EN title assertion on 1–2 gap pages via TS-0040.
- Docs pointers (CU76, ADR-015, E2E mapping, CHANGELOG).

**Non-Goals:**

- Redesign, copy rewrite, or new product features.
- Wiring redirect stubs.
- Backend localization.
- Full-string audit of already-partial pages outside the gap set.

## Decisions

1. **Namespaces follow existing hierarchy**
   - Admin CRUD: `administracion.roles`, `administracion.workflows` (+ `editor`
     nested keys for `[id]`).
   - Top-level pages: `suplencias`, `reportes`; expand existing `items` and
     `login`; add `auditoria.allModules`.
   - Why: mirrors `administracion.usuarios` / sibling admin catalogs.
   - Alternative rejected: flat keys under `pages.*` — breaks established layout.

2. **Reuse `common` for shared actions/labels**
   - Prefer `tc("edit"|"delete"|"cancel"|"create"|"update"|"id"|"name"|…)` when
     keys already exist; add `common.active` / `common.inactive` if reused
     across roles/workflows.
   - Why: avoids duplicate translations and matches usuarios pattern.

3. **Expand `items` catalog to match real UI fields**
   - Replace outdated `concepto`/`cantidad`/`precio` with fields used by
     `dashboard/items` (nombre, valor, tipo, motivo, presupuesto, report
     section, tipo labels).
   - Why: wiring without catalog update would leave dead keys / missing UI keys.
   - Alternative rejected: invent a second `itemsPresupuesto` namespace.

4. **TDD: extend `i18n.test.ts` first (observe fail), then catalogs + pages**
   - Required-namespace assertions are the AC gate; page wiring follows.
   - Optional TS-0040 EN title on workflows/roles serialized after unit green.

5. **Redirect stubs stay untouched**
   - `administracion/items` and `administracion/auditoria` only `redirect()`;
     canonical routes receive the i18n work.

## Riesgos / Trade-offs

- **[Risk] Large message JSON diffs** → Keep keys focused on visible strings;
  prefer `common` reuse.
- **[Risk] Welcome toast interpolation** → Use next-intl rich/params
  (`welcome` with `{name}`) consistently in es/en.
- **[Risk] Playwright flakiness on TS-0040 extension** → Keep optional; assert
  stable headings with locale cookie; serialize after unit/page work.
- **[Trade-off] Expanding `items` fields may break unused consumers of old
  keys** → Grep confirms page never used old keys; safe to replace.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Gap page namespaces exist in both catalogs | unit | `frontend/src/tests/unit/i18n.test.ts` |
| Catalog key structures stay identical | unit | same (existing + extended) |
| Login leftover keys exist in both catalogs | unit | same |
| Login page uses translated leftovers | unit | `login-page.test.tsx` (update literals) |
| Workflows / suplencias / reportes / roles / items use translations | unit (keys) + page wiring | `i18n.test.ts` + optional TS-0040 |
| Auditoria all-modules filter translated | unit keys + page | `i18n.test.ts` |
| Optional EN title on former gap pages | E2E | `TS-0040-l10n-language-switching-qa.spec.ts` |

- New unit tests: required-namespace assertions in `i18n.test.ts` (AC).
- New integration tests: n/a (no backend).
- Coverage impact (JaCoCo): n/a — frontend-only.

## Regression Strategy

- Existing tests affected: `i18n.test.ts`, `login-page.test.tsx` (connection /
  welcome / validation strings), TS-0040 if extended.
- Full suite command: `cd frontend && npm test`; backend `mvn verify -pl backend-api`
  only as sanity (no Java delta expected).
- HTTP/Bruno API suite: n/a — no API change.
- Legacy paths: none (`frontend-swing` removed).

## Playwright Strategy

- Specs to add/update:
  - Optionally extend `frontend/tests/e2e/TS-0040-l10n-language-switching-qa.spec.ts`
    with EN titles on 1–2 gap pages (workflows and/or roles).
  - Update `E2E-TEST-MAPPING.md` if TS-0040 coverage expands.
- Golden path: set `NEXT_LOCALE=en` → open gap page → assert English title.
- Edge: default ES still shows Spanish titles.
- Viewports: 320 / 768 / 1024 — layout unchanged (string swap only).
- Command: `cd frontend && npx playwright test TS-0040`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only image/deploy; no schema coupling
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): login footer/forgot in EN; open workflows
  or roles with EN locale and confirm titles

## Rollback Strategy

- Revert safe: yes — catalogs + page wiring + tests only; no data migration
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: low (UI language only)

## Migration Plan

1. Extend `i18n.test.ts` with required namespaces/keys → observe fail.
2. Add es/en message keys.
3. Wire pages + login leftovers.
4. Update login unit tests if they assert Spanish literals for leftovers.
5. Optional TS-0040 EN title assertions.
6. Docs + CHANGELOG.
7. Preflight, push, draft PR.

## Open Questions

None blocking. Store seed path was unavailable; artifacts filled from issue
#1059 + coordinator decisions.
