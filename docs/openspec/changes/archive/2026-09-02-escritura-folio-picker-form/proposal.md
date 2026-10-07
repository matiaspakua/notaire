# Proposal: Escritura-Folio Picker

**Issue:** #892  
**Date:** 2026-08-31  
**Proposer:** Demo E2E blocker discovery

## Problem

The demo E2E script builds two complete use cases end-to-end through the real UI. At the Escritura firma step (line 176–189), it fails because:

1. Backend enforces: *"La escritura debe tener al menos un folio asignado para poder firmarse"* (via `EscrituraFirmaService.java:45`)
2. Frontend form has no folio picker
3. No way to assign a created folio to a created escritura through UI

## Solution (Minimal)

Add a folio selector to the Escritura creation/edit form (`/dashboard/escrituras`):
- Show only folios in "Nuevo" estado (not yet assigned)
- Testid: `select-folio-escritura` (consistent with other pickers)
- Backend already supports `Escritura.folios[]` field; no API change needed

## Impact

- **Unblocks:** Case A → Testimonio → Verificación → Inscripción → Retiro (CU09–CU12)
- **Enables:** Demo E2E to complete both Case A and Case B end-to-end
- **Scope:** Frontend form + E2E test (1–2 hours, small change)
- **Risk:** Very low (backend already supports it, just wiring to UI)

## Use Cases & Requirements

- **CU06** — Firmar Escritura (prerequisite: folio assigned)
- **CU09–CU12** — Testimonio, Verificación, Inscripción, Retiro (depend on CU06)
- **RF-27** — Firmar Escritura (functional requirement)

## Acceptance Criteria

1. Escritura form has folio selector (testid: `select-folio-escritura`)
2. Selector populated from `GET /api/v1/folio?estado=Nuevo` (or similar filtering)
3. Selector only shows folios not already assigned to other escrituras
4. E2E test confirms: folio assignment → `POST /api/v1/escrituras/{id}/firmar` succeeds (HTTP 200)
5. Demo script completes Case A line 189 without errors

## Technical Notes

- Backend: `Escritura.java:85` already has `List<Folio> folioList`
- DTO: `DtoEscritura.java` already has `folios` field
- API: Likely accepts folio IDs in PUT/POST payload (verify via contract test)
- Frontend: Reuse existing `Select` component from `@/components/ui/select`
- Pattern: Same as `select-escribano-escritura` and `select-tipo-tramite-gestion` in same file

## Definition of Done

- [ ] Gate 1: This proposal approved
- [ ] TDD: E2E test written and failing
- [ ] Implementation: Form picker + filtering logic
- [ ] Tests: E2E passes, no regression
- [ ] Docs: CHANGELOG.md updated
- [ ] PR merged, smoke test confirms demo completes
