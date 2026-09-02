# Tasks: Escritura-Folio Picker

**Input:** `proposal.md`, `spec.md`, `design.md`

## Task Groups

### Gate 1 — Prerequisites

- [x] Issue #892 created, linked to CU06, moved to `in-progress`
- [x] `spec.md` written with Given-When-Then acceptance scenarios
- [x] `design.md` written with Testing, Regression, Deployment, Rollback strategy

### Create Branch

- [ ] `git checkout main && git pull origin main`
- [ ] `git checkout -b feat/892_escritura-folio-picker`

### Gate 2 — Tests (TDD)

- [ ] Write `TS-0012-escritura-folio-firma.spec.ts` with scenarios from spec.md
- [ ] Run Playwright against pre-change code → **FAIL** (selector not found, firma returns 400)
- [ ] Verify failure matches expectation: folio selector missing, firma needs folio

### Implement

- [ ] Update `frontend/src/app/dashboard/escrituras/page.tsx`
  - [ ] Query folios via `useQuery`
  - [ ] Filter for `estado === "Nuevo"`
  - [ ] Add `<FormField>` with `Select` component (testid: `select-folio-escritura`)
  - [ ] Update form submission: include `folios: [{ idFolio }]` in payload
- [ ] Re-run `TS-0012-escritura-folio-firma.spec.ts` → **PASS**

### Regression

- [ ] `npx tsc --noEmit` clean
- [ ] `npm run lint` clean
- [ ] Full Playwright E2E suite green (existing tests unaffected)

### Gate 3 — Documentation & Quality

- [ ] Update `CHANGELOG.md` with fix entry
- [ ] No new dead code
- [ ] Run `bash scripts/run_pipeline.sh` → **green** (all gates pass)

### Commits

- [ ] Commit 1: `test(escritura): add failing E2E test for folio-firma workflow`
- [ ] Commit 2: `feat(escritura): add folio picker to escritura form` → `Closes #892`

### Pull Request & CI

- [ ] `git push -u origin feat/892_escritura-folio-picker`
- [ ] `gh pr create` referencing `Closes #892`
- [ ] Wait for CI green (all checks pass)

### Deploy & Smoke Test

- [ ] Merge PR once CI green
- [ ] Run demo script: `npx playwright test 02-demo-two-full-cases --project=chromium`
- [ ] Verify Case A:
  - [ ] Reaches line 176–189 (Escritura creation)
  - [ ] Folio selector visible and populated
  - [ ] User can select a folio
  - [ ] Firma succeeds (HTTP 200, no 400 error)
  - [ ] Continues to Testimonio step (line 191+)
- [ ] Verify Case B follows same path
- [ ] Close Issue #892 once smoke test passes

### Gate 5 — Definition of Done

- [ ] All TDD/implementation/regression/E2E/docs tasks checked
- [ ] `tsc`/lint clean
- [ ] Playwright E2E spec green
- [ ] Demo script Case A + B both complete to "archivar gestión"
- [ ] PR merged, CI green, Issue #892 closed
- [ ] Change archived under `openspec/specs/archive/`
