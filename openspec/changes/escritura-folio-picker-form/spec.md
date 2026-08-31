# Specification: Escritura-Folio Picker Form

**Issue:** #892  
**Use Case:** CU06 — Firmar Escritura  
**Functional Requirement:** RF-27  

## Summary

Add a folio selector to the Escritura creation/edit form so users can assign at least one folio before signing. Backend requires this for firma validation.

## Scope

**In scope:**
- Frontend: Escritura form folio picker (testid: `select-folio-escritura`)
- E2E test: Verify folio assignment → firma succeeds
- Filtering: Show only "Nuevo" status folios

**Out of scope:**
- Backend API changes (already supports `Escritura.folios[]`)
- Multi-folio UI (single picker sufficient for MVP)
- Folio creation from within Escritura form

## Acceptance Scenarios

### Scenario 1: Folio selector populated with available folios

**Given:** User on Escritura creation form  
**When:** Folio selector (testid: `select-folio-escritura`) is focused  
**Then:** Dropdown shows list of folios in "Nuevo" estado, not assigned to other escrituras

**Example data:**  
- Folio #1 (Protocolo Demo A): "Nuevo" → shown ✅
- Folio #2 (Protocolo Demo B): "Nuevo" → shown ✅
- Folio #3 (Protocolo Demo A): "Con Escrituras" → NOT shown ✅

### Scenario 2: User selects folio and creates escritura

**Given:** Folios #1, #2 available; Escritura form open  
**When:** User fills número, fecha, selects folio #1, clicks "Crear"  
**Then:** Escritura created with `folios: [{ idFolio: 1 }]` in payload

**Expected HTTP:**  
```
POST /api/v1/escrituras
{
  "numero": 123,
  "fechaEscrituracion": "2026-08-05",
  "folios": [{ "idFolio": 1 }]
}
```

### Scenario 3: Firma succeeds after folio assignment

**Given:** Escritura created with folio assigned  
**When:** User clicks "Firmar Escritura" button  
**Then:** Backend validates "escritura has ≥1 folio" → firma succeeds (HTTP 200)

**Result:**  
```
POST /api/v1/escrituras/{id}/firmar
HTTP 200
{ "estado": "Firmada", ... }
```

### Scenario 4: E2E demo Case A completes

**Given:** Demo script executing `02-demo-two-full-cases.spec.ts`, Case A  
**When:** Script reaches line 176–189 (Escritura creation & firma)  
**Then:** Script selects folio from dropdown, firma succeeds, continues to Testimonio step (line 191)

**Expected:**  
- No timeout on folio selector locator
- `POST /escrituras/{id}/firmar` returns 200
- Demo continues without errors through Testimonio/Copia/Documento/Pago

## Notaire Traceability

| Artifact | Link |
|----------|------|
| Issue | #892 |
| Use Case | CU06 — Firmar Escritura |
| Requirement | RF-27 — Firmar Escritura (prereq: folio assigned) |
| Demo Blocker | `02-demo-two-full-cases.spec.ts` lines 176–189 |
| Dependent Use Cases | CU09–CU12 (Testimonio, Verificación, Inscripción, Retiro) |

## Design Notes

**Frontend pattern:**
- Reuse `Select` component from `@/components/ui/select`
- Pattern: Same as `select-escribano-gestion`, `select-tipo-tramite-gestion` in same file (`escrituras/page.tsx`)
- Filter: Query `useQuery({ queryKey: ["folios"], queryFn: () => apiGet<Folio[]>("/folio") })` then filter by `estado === "Nuevo"`
- Display: `Folio #{idFolio} — {tipoDeFolio.nombre}` (e.g., "Folio #1 — Protocolo Demo A")

**Backend:**
- No change: `Escritura.folios[]` and `DtoEscritura.folios[]` already exist
- Firma validation already checks: `EscrituraFirmaService.java:45` → if `folioList.isEmpty()` throw 400

**Test data:**  
- Create folio (type "Protocolo Demo A", estado "Nuevo")
- Create escritura
- Assign folio via form
- Verify firma succeeds
