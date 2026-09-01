# Design: Escritura-Folio Picker

## Testing Strategy

**TDD:** Write failing E2E test first, then implement form picker.

### Test: `TS-0012-escritura-folio-firma.spec.ts`

1. **Setup:** Create folio (type="Protocolo Demo", estado="Nuevo"), create escritura  
2. **Golden path:** Open escritura form, select folio from dropdown, verify select shows folio label  
3. **Firma:** Sign escritura, verify HTTP 200 (no 400 "missing folio" error)  
4. **Regression:** Full demo case A continues to Testimonio without errors

**File location:** `frontend/tests/e2e/TS-0012-escritura-folio-firma.spec.ts`

## Regression Strategy

- Full Playwright suite (`npm run test:e2e`) must stay green
- Escritura existing tests (CU04, CU05 presupuesto workflows) unaffected
- Demo test (`02-demo-two-full-cases.spec.ts`) now progresses past line 189

## Frontend Implementation

**File:** `frontend/src/app/dashboard/escrituras/page.tsx`

**Changes:**
1. Query folios: `const { data: folios } = useQuery({ queryKey: ["folios"], queryFn: () => apiGet<Folio[]>("/folio") })`
2. Filter for "Nuevo": `const availableFolios = folios.filter(f => f.estado === "Nuevo")`
3. Add to form: 
```tsx
<FormField label={t("fields.folio")} required>
  <Select value={form.idFolio?.toString()} onValueChange={(v) => setForm({ ...form, idFolio: parseInt(v) })}>
    <SelectTrigger data-testid="select-folio-escritura">
      <SelectValue placeholder={t("selectFolio")} />
    </SelectTrigger>
    <SelectContent>
      {availableFolios.map(f => (
        <SelectItem key={f.idFolio} value={String(f.idFolio)}>
          Folio #{f.idFolio} — {f.tipoDeFolio?.nombre}
        </SelectItem>
      ))}
    </SelectContent>
  </Select>
</FormField>
```
4. Update form submission: Include `folios: [{ idFolio: form.idFolio }]` in `POST /escrituras` payload

**Pattern reference:** Same as `select-escribano-gestion` selector in `gestiones/page.tsx` (lines ~167–176)

## Deployment Strategy

Standard frontend deployment (no backend/database change):
- Build: `npm run build`
- Docker: Build frontend image
- Deploy: CD pipeline updates running container
- Smoke test: Run demo script; verify Case A completes

## Rollback Strategy

Git revert (`git revert` of form-change commit) — no data migrated, no schema changes.

## Complexity Notes

**Low:** ~50 lines of code, reuses existing patterns from same file.  
**No new dependencies.**  
**No schema/API changes.**
