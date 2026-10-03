# ADR-023: REST Resource Naming Conventions

**Status:** Accepted  
**Date:** 2026-10-03  
**Deciders:** Engineering (issue #1065 / CU76)  
**Related:** ADR-003 (versioning only), ADR-021  

## Context

The public `/api/v1` surface mixes languages (`/people`, `/audit-log`, `/items`
vs Spanish nouns elsewhere), singular collection paths (`/folio`, `/inmueble`,
`/copia`, …), both `/buscar` and `/search`, and verbs in paths
(`/firmar`, `/archivar`, …). ADR-003 documents **URL path versioning** only; it
does not define resource naming. Create responses also lacked a consistent
`Location` header policy (Slice-1 of #1065 adds a shared helper and sample
coverage).

A big-bang rename would break Bruno, the Next.js client, and OpenAPI consumers
without a migration plan.

## Decision

### Naming (new paths and phased renames)

1. **Language:** Prefer **English resource nouns** that match established
   English `/api/v1` paths (`/people`, `/items`, `/audit-log`, hexagonal adapter
   vocabulary). Do not introduce new Spanish collection segments. Existing
   Spanish paths remain until a phased rename under ADR-003.
2. **Plural collection nouns** for new resources. Singular legacy collections
   (e.g. `/folio`) rename in follow-up slices, not as a big-bang.
3. **Search:** Prefer `/search` over `/buscar` for new or renamed search
   routes.
4. **Actions:** Prefer sub-resources or status transitions on the resource
   (e.g. `PUT /minutas-inscripcion/{id}/presentar`) over free-standing verbs on
   the collection root. New action routes should be nouns or clear domain
   transition names, not ad-hoc English/Spanish verb mixtures.
5. **Creates:** Successful resource creates return **`201 Created`** with a
   **`Location`** header built via the shared
   `CreatedResponses` helper (`adapter.in.web.support`).

### Versioning interaction (ADR-003)

- ADR-003 remains the sole versioning ADR.
- Breaking renames (path segment changes) follow ADR-003: deprecate + dual-route
  window and/or `/api/v2`, documented in CHANGELOG.
- Slice-1 of #1065 does **not** rename paths; it only records this policy,
  removes unused `POST /pagos/params`, and standardizes sample creates on
  `201` + `Location`.

## Consequences

- **Pros:** Clear target for new endpoints; renames can be batched; create
  responses become discoverable via `Location`.
- **Cons:** Temporary dual vocabulary until phased renames complete; clients
  must treat minuta create as `201` (was `200`); clients must stop calling
  `/pagos/params` (removed).

## Follow-up (out of this ADR's first slice)

Phased inventory renames (singular→plural, language alignment, `/buscar`→
`/search`) tracked under issue #1065 acceptance / future issues; not executed
here.
