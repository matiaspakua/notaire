---
title: E2E Coverage Report - 2026-06-02
---

# E2E Coverage Report

**Date:** 2026-06-02
**Trigger:** manual supervised run (headed Google Chrome, `SLOW_MO=1500`)
**Stack under test:** full system — app (PostgreSQL, backend, frontend, pgAdmin)
+ observability/quality infra (Prometheus, Grafana, Loki, Promtail, SonarQube, Homer).

## Summary

Full Playwright E2E suite executed in **headed Google Chrome** at 1.5 s/action
so every UI action could be reviewed visually. The session opens with a new
**supervised guided tour** (login → every module → logout) and then runs every
module spec.

| Result | Count |
|--------|-------|
| ✅ Passed | **185** |
| ❌ Failed | **3** |
| ⏭️ Skipped (intentional — unimplemented CUs) | 36 |
| ⏱️ Duration | 13.2 min |

Backend unit suite (pre-run): **427 passed**, frontend-swing **14 passed**.

## Modules Covered (via UI)

Auth (login + **logout**), Dashboard, Gestiones, Presupuestos, Personas/Clientes,
Escrituras, Pagos, Protocolo, Inmuebles, Copias, Items, Documentos, Auditoría,
Reportes, Suplencias, and Administración (Trámites, Conceptos, Estados de
gestión, Folios, Plantillas, Tipos de documento, Ítems, Usuarios, Auditoría).
Each CRUD module's primary "create" modal was opened and dismissed. Plus
l10n (ES/EN switch), icon rendering, and "page loads without console errors"
for all 25 routes.

## Issues Found

### 🔴 Critical / API 500s — FIXED this run

1. **`PUT /api/v1/conceptos/{id}` → 500** — `NullPointerException` unboxing
   `DtoConcepto.getHabilitado()` when the payload omits `habilitado`
   (create forced it to `true`, update did not).
   **Fix:** `ConceptoController.updateConcepto` preserves the existing enabled
   state when the payload omits it. Regression test added.

2. **`GET /api/v1/folio` → 500** — `"Failed to write request"`; the controller
   returned raw `Folio` JPA entities whose lazy associations
   (`foliosCopiasCollection`, `copiaList`, `fkIdEscritura`) are uninitialized
   Hibernate proxies that Jackson cannot serialize. (POST worked because
   request-body entities have plain `null` collections.)
   **Fix:** `FolioController` GET endpoints now map to `DtoFolio` (mirrors the
   sibling `TipoDeFolioController`). Integration guards added.

### 🟠 Robustness — FIXED this run

3. **`DELETE /api/v1/personas/{id}` → 500** when the persona is still referenced
   by other records (FK violation surfaced as a raw 500). Seen repeatedly in
   global-teardown.
   **Fix:** `PersonaController.deletePersona` now returns **409 Conflict** with a
   clear message on constraint violation (matches `ConceptoController`).
   *Note:* the E2E global-teardown also deletes `usuario` after `persona`; the
   ordering should be reversed (delete children first) — tracked below.

### 🟡 Frontend / a11y — FIXED this run

4. **Plantillas de Presupuesto was broken end-to-end** — the page used the
   **wrong endpoint** (`/plantilla-presupuesto`, singular → 404; the backend is
   `/plantilla-presupuestos`) and a **fictional model** (`nombre`/`descripción`),
   while the real model is a `(tipo de trámite × concepto)` pair with
   `observaciones` (composite key). The create-modal therefore lacked the
   required selectors (`admin.spec.ts:132` failed on the absent combobox), and
   `POST` 500'd because the backend derives the PK from the related entities.
   **Fix:** rewrote the page to the real model — two Selects (tipo de trámite,
   concepto) + observaciones, correct plural endpoints, and the verified payload
   `{ plantillaPresupuestoPK, tipoDeTramite:{idTipoTramite}, concepto:{idConcepto}, observaciones }`.
   Create/update/delete now work end-to-end.

5. **Empty / duplicate `<h1>`** — the dashboard layout rendered a global
   title-less `<AppHeader />`, emitting an empty (a11y-invalid) `<h1>` on every
   page plus a **duplicate header** on pages that render their own.
   **Fix:** removed the layout `AppHeader`; each page keeps its own titled header.
   Also gave the shared `SelectTrigger` proper `role="combobox"` ARIA.

6. **E2E teardown delete ordering** — `global-teardown` deleted `persona` before
   the `usuario` that references it (guaranteed FK error).
   **Fix:** reordered so the usuario is deleted first.

## Test / Tooling Additions

- `tests/e2e/00-supervised-tour.spec.ts` — single watchable journey
  (login → all modules + create-modals → logout); runs first.
- `data-testid="btn-logout"` and `data-testid="sidebar"` on `AppSidebar`;
  `select-tipo-tramite` / `select-concepto` on the Plantillas modal.
- Playwright `chromium` project pinned to the real Google **Chrome** channel.
- `ConceptoControllerTest` — regression test for the `habilitado`-null update.
- `PreviouslyBrokenEndpointsIntegrationTest` — folio list + not-found guards.

## Action Items

- [ ] Reconcile the remaining Plantillas tech debt (unused `usePlantillas` hook,
      legacy type fields, `hooks.test.ts` assertions) in a dedicated cleanup.
- [ ] Implement the still-skipped documentos/testimonio CU flows (CU03/04/07/08/11/12).

---
*Report generated from a supervised headed Playwright run.*
