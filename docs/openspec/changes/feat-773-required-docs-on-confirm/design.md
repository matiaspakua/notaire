> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #773 remaining AC after PR #1240. Complete-case
(`ManagementController.saveProcedure`) creates `Procedure` + folder only.
`DocumentReentryService` already loads `ProcedureTemplate` by procedure type for
CU43. Case summary (`ManagementCaseSummaryService`) lists submitted documents
only — empty right after confirm. Flyway tip V45; no schema gap for pending rows
on `submitted_documents`.

## Goals / Non-Goals

**Goals:**
- Materialize CU03 checklist at confirm time for new procedures.
- Surface checklist via existing case-summary `documents`.
- Prove with failing-then-green JUnit + Bruno + focused E2E.

**Non-Goals:**
- New junction table / Flyway (unless foreman directs otherwise).
- Replacing CU03/CU43 on-demand screens.
- #1241 PUT semantics; full tutorial rewrite; #774 hub; ADR-023 path renames.

## Decisions

1. **Materialize pending `SubmittedDocument` rows** rather than a parallel
   read-only checklist DTO — case-summary and CU72 already model presented docs;
   pending rows with `delivered=false` fit without API shape change.
2. **Hook after folder generation in `saveProcedure`** via a dedicated
   `RequiredDocumentationOnConfirmService` (or equivalent) in
   `application/usecase/...`, using `ProcedureTemplateRepository` +
   `SubmittedDocumentRepository` — not legacy `jpa`.
3. **Seed only when `saveProcedure` creates a new procedure** (POST complete-case
   and PUT when no procedures exist). Do not re-seed on updates of an existing
   first procedure.
4. **Idempotency key:** `(procedureId, documentTypeId)` — skip if a submitted
   document for that type already exists on the procedure.
5. **Pending flags:** `reentered=false`, `delivered=false`, `released=false`,
   `flagged=false` (distinct from CU43 reentry which sets `reentered=true`).
6. **Frontend:** light — ensure post-complete-case UX shows case-summary docs
   (existing dialog/panel); no new route required for this slice.
7. **Englishize** every touched identifier/log/message; leave i18n catalogs alone.

## Riesgos / Trade-offs

- [Pending rows look like “empty submissions”] → Mitigation: flags distinguish
  undelivered checklist items; CU04 registration updates the same row or creates
  by type (existing create path) — tests document expected UX.
- [Template data missing for some types] → Empty seed is valid (scenario covered).
- [CHANGELOG / heavy CI contention with #1518] → Do not open product PR until
  foreman clears; Gate 1 stockpile only until then.
- [Security: no new public endpoint] → Same authz as complete-case / case-summary;
  notify agent #4 only if authz/PII surface changes (not expected).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Complete-case seeds pending required documents from procedure template | integration | `ManagementCompleteCaseRequiredDocsIntegrationTest` (or extend existing complete-case IT) |
| Empty template yields no seeded documents | unit | `RequiredDocumentationOnConfirmServiceTest` |
| Case summary lists pending required documents after confirm | unit + integration | `ManagementCaseSummaryServiceTest` + IT |
| Idempotent seed skips existing document types for the procedure | unit + integration | `RequiredDocumentationOnConfirmServiceTest` + IT |
| PUT complete-case on existing procedure does not wipe registered docs | integration | complete-case IT |
| E2E: checklist visible after complete-case | E2E | new or extend under `testing/e2e/tests/` (TS-nnnn); map in E2E-TEST-MAPPING |

- New unit tests: `RequiredDocumentationOnConfirmServiceTest`
- New/extended integration tests: complete-case + case-summary
- Coverage impact: positive; keep JaCoCo ratchet

**TEST_CMD (Gate 2 red proof):**

```bash
cd backend-api && mvn test -Dtest=RequiredDocumentationOnConfirmServiceTest,ManagementCompleteCaseRequiredDocsIntegrationTest
```

(Adjust class names to match implementation; record observed red exit ≠ 0 before green.)

## Regression Strategy

- Existing tests affected: `DocumentReentryServiceTest`,
  `ManagementCaseSummaryServiceTest`, complete-case / procedure-folder ITs —
  update expectations if they assume zero docs after complete-case.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: extend
  `backend-api/api-test/procedure-folders/02-complete-case.yml` and
  `backend-api/api-test/managements/05b-case-summary.yml` (bru CLI ≥4.x /
  OpenCollection YAML).
- Legacy paths at risk: none (`jpa` untouched).

## Playwright Strategy

- Specs to add/update: focused TS after complete-case asserting required docs in
  case summary / gestión surface; not a full tutorial rewrite.
- Golden path: create gestión with procedure type that has templates → open case
  summary → see pending required doc names.
- Edge / error paths: procedure type without templates → empty documents list OK.
- Viewports: 1024px primary; smoke 768 if dialog layout changes.
- Command: `cd testing/e2e && npx playwright test <spec>`

## Deployment Strategy

- Flyway migration required: **no**
- Deployment order / coupling: single PR after serialize clear
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): complete-case then case-summary shows template
  document names

## Rollback Strategy

- Revert safe: **yes** (additive behavior; no schema)
- Database rollback: none needed (pending rows are ordinary submitted_documents;
  orphan pending rows after revert are harmless / can be cleaned manually if needed)
- Data written under the new behavior after revert: pending checklist rows remain
  until deleted; clients still see them via case-summary
- Blast radius if rollback is delayed: low — extra undelivered document rows

## Migration Plan

1. Gate 1 validated (this pack).
2. Wait for #1518 merge / foreman clear (CHANGELOG + heavy-CI serialize).
3. Branch `cursor/feat-773-required-docs-on-confirm-69d3` from updated `main`.
4. TDD red → implement → Bruno/OpenAPI → light FE/E2E → CHANGELOG → PR `Closes #773`.

## Open Questions

None blocking Gate 1. If product prefers a non-persisted checklist DTO instead of
pending `SubmittedDocument` rows, escalate to foreman before implement (would
change case-summary shape and possibly need FE contract work).
