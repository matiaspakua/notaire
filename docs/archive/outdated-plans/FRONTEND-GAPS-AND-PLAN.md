# Frontend / Full-Stack Gaps & Phased Plan

_Last updated 2026-05-30 (branch `design/407_ui_ux_motion_crud`)._

This document records the concrete gaps, findings and bugs discovered while
auditing the catalog (administración) pages and their backing endpoints, plus a
light phase-by-phase plan. Items marked ✅ are addressed in the current branch.

> **Note on methodology.** Part of this audit was done through a token-saving
> CLI proxy (RTK) whose output filtering produced misleading/stale views of some
> files. All facts below were re-verified against the git object store
> (`git show HEAD:<path>`), which is authoritative.

---

## Findings

### Correctness — data layer (root cause of #396)
1. ✅ **Three catalog pages called API paths that do not exist.** The real REST
   controllers are:
   | Entity | Real base path (`/api/v1`) | Page was calling |
   |---|---|---|
   | EstadoDeGestion | `/estado-gestion` | `/catalogos/estados-gestion` ❌ |
   | Folio | `/folio` | `/folios` ❌ |
   | PlantillaPresupuesto | `/plantilla-presupuesto` | `/presupuestos/plantillas` ❌ |
   There is **no `/catalogos/*` controller** in the backend. _Fixed: pages
   re-pointed to the real endpoints._
2. ✅ **`EstadoDeGestion` field-name mismatch.** The frontend type/page used
   `idEstadoDeGestion` and `descripcion`, but the API DTO exposes
   `idEstadoGestion` and `observaciones`. So the ID column rendered blank and the
   description never displayed/saved, even if the endpoint had been correct.
   _Fixed: type + page aligned to the DTO; `version` now round-tripped on update._
3. ✅ **`Folio` create sent a non-existent field.** The old page posted
   `disponible: true`; the entity has no such field (it has
   `numero/anio/estado/observaciones/tipoDeFolio`). _Fixed: form now edits the
   real fields._
4. ✅ **No edit/delete UI on the three pages** (create + list only), even though
   the backend supports full CRUD. _Fixed: edit dialog + delete `ConfirmDialog`
   wired on all three._

### Backend — audited, no change required
5. **All three controllers already implement full, correct CRUD**
   (`GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}`) via proper
   `*Service`/repository layers. `DELETE` honestly removes the row and returns
   `204`/`404`; `EstadoDeGestion` additionally returns `409` when the row is
   referenced by FKs. **No backend defect was found for these entities.**
6. `PlantillaPresupuesto` is a simple-id entity (`idPlantillaPresupuesto`,
   `nombre`, `descripcion`, `tipoDeTramite`, `itemList`) — not a composite key.

### Stale React-Query hooks
7. Hooks `useEstadosGestion`, `useFolios`, `usePlantillas` exist but the pages
   used inline `apiGet/apiPost` instead, and some hook paths drifted from the
   controllers. The pages now call the correct endpoints inline; consolidating
   onto verified hooks is Phase 3.

### Tooling / build hygiene (pre-existing on `main`)
8. **`npm run lint` is broken** — `next lint` + legacy `.eslintrc.json` are
   incompatible with Next 16 / ESLint 9. `next build` skips lint, so this passes
   silently in CI.
9. **Pre-existing TS errors** in `tests/e2e/cu-matrix.spec.ts` (6 × `TS2322`:
   sync function supplied where `Promise<string>` is expected).
10. **Two lockfiles** (`/package-lock.json` and `/frontend/package-lock.json`) →
    Next warns about the inferred workspace root.
11. **`middleware.ts` deprecation** — Next 16 wants `proxy.ts`.

### UX
12. Possible double header (`dashboard/layout.tsx` renders `<AppHeader/>` while
    pages render their own).
13. FK fields (Folio `tipoDeFolio`, Plantilla `tipoDeTramite`) are not selectable
    in the forms.
14. No dark-mode wiring despite tokens; limited optimistic updates.

---

## Phased plan

### Phase 1 — Tooling trust ✅
- Established a reliable read channel (git object store) after discovering the
  RTK proxy was returning filtered/stale file views.

### Phase 2 — Correctness ✅
- Fix the three dead endpoints and the `EstadoDeGestion`/`Folio` field
  mismatches (closes #396).

### Phase 3 — CRUD completion ✅
- Add edit + delete (+ `ConfirmDialog`) to `estados-gestión`, `folios`,
  `plantillas` using the design-system form patterns; add the i18n keys (es+en).

### Phase 4 — Backend integrity audit ✅
- Verified all three controllers have correct, working CRUD (no change needed).
- _Follow-up:_ audit the remaining controllers' `update()` methods for
  full-field persistence and confirm every `@DeleteMapping` end-to-end with
  `mvn test -Ppg-integration`.

### Phase 5 — Frontend data-layer cleanup
- Route every page through typed React-Query hooks; reconcile/remove drifted
  hooks; centralize endpoint paths.

### Phase 6 — Tooling / build hygiene
- Migrate ESLint to flat config so `lint` runs; fix `cu-matrix.spec.ts`; remove
  the stray root lockfile (or set `turbopack.root`); rename `middleware.ts` →
  `proxy.ts`.

### Phase 7 — UX depth
- Resolve the double header; add FK relation selectors; optimistic mutations,
  refined skeletons/empty states, full keyboard/ARIA pass, dark-mode wiring.

### Phase 8 — E2E coverage
- Playwright specs covering create/edit/delete on the completed catalog pages and
  the remaining concept pages.
