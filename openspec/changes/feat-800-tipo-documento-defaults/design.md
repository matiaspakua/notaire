> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

\#837 shipped form fields `expires` / `dueDays` / `deliveredBy` and
SubmittedDocument inheritance. Residual #800 gap: admin form EMPTY omits
`enabled` and `returned`; FE type has `enabled?` but not `returned?`;
`DtoDocumentType` has no `returned`; `DocumentTypeController.create` forces
`dto.setEnabled(true)`; `DocumentType.setAtributos` / `getDto` never map
`returned`.

## Goals / Non-Goals

**Goals:**

- Expose `enabled` and `returned` checkboxes on create/edit with EMPTY defaults
  `enabled: true`, `returned: false`.
- Persist and return both fields through the existing
  `POST/PUT/GET /api/v1/tipo-de-documento` DTO path.
- TDD with Vitest + Playwright + backend IT; keep #837 inheritance tests green.
- Englishize Spanish strings in touched non-i18n code; labels via i18n catalogs.

**Non-Goals:**

- Re-implement SubmittedDocument inheritance of expires/dueDays/deliveredBy.
- Inherit `enabled`/`returned` onto SubmittedDocument.
- Flyway schema changes (columns exist).
- Rename `/tipo-de-documento` URL path.

## Decisions

1. **Modify capability `tipo-documento-vencimiento-config`** — residual of the
   same admin catalog surface; no new capability name.
2. **Add `returned` to `DtoDocumentType`** — entity already has the column;
   without DTO mapping the write path silently drops the field.
3. **Default `enabled=true` / `returned=false` only when omitted** — create
   must not overwrite an explicit `enabled: false` from the client.
4. **UI uses existing `CheckboxField`** — same pattern as expires on this page
   and enabled on other admin catalogs.
5. **Englishize toast strings via i18n** — replace hardcoded Spanish success
   toasts on the touched page with `t("created")` / `t("updated")` /
   `t("deleted")`.

## Riesgos / Trade-offs

- [Clients that never sent `returned`] → continue to get `false` (boolean /
  Boolean.TRUE.equals default) — acceptable.
- [Create previously always forced enabled=true] → now honors false; matches
  product intent for disabling a type at create time.
- [Edit blocked when in use] → unchanged 409 path; enabled/returned edits
  follow the same gate.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Create form defaults enabled/returned | unit (Vitest) + E2E | `tipo-documento-enabled-returned.test.ts`, Playwright CU27-GW04 |
| Create persists enabled/returned | E2E + integration | Playwright CU27-GW05, `DocumentTypeReferentialIntegrityTest` |
| Edit pre-fills and updates | E2E + integration | Playwright CU32-GW02, update IT |
| API create honors enabled=false | integration | `DocumentTypeReferentialIntegrityTest` |
| #837 inheritance still green | integration | `SubmittedDocumentControllerTest` |

- New unit tests: Vitest for EMPTY defaults + type field presence / payload shape
- New integration tests: create/update/get with enabled+returned
- Coverage impact: small; ratchet floor held

## Regression Strategy

- Existing tests affected: `DocumentTypeReferentialIntegrityTest` create helper
  (still omits fields → defaults); Playwright `tipo-documento-vencimiento-config`
  (unchanged scenarios still pass).
- Full suite command: `mvn test -pl backend-api -Dtest=DocumentTypeReferentialIntegrityTest,SubmittedDocumentControllerTest`
- Frontend: `cd frontend && npm test -- --run` (or vitest filter) + Playwright
  for the form specs
- Legacy paths at risk: none — additive fields

## Playwright Strategy

- Extend `frontend/tests/e2e/tipo-documento-vencimiento-config.spec.ts` (or add
  sibling `tipo-documento-enabled-returned.spec.ts`) with:
  - CU27-GW04: new form shows enabled checked, returned unchecked
  - CU27-GW05: create with returned checked / enabled unchecked persists on edit reopen
  - CU32-GW02: edit pre-fills both checkboxes from stored values
- Viewports: ad-hoc check at 320 / 768 / 1024 on the dialog

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: backend + frontend together so FE can send
  `returned` and have it persisted
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): create type with returned=true,
  enabled=false; GET shows both; edit unused type toggles them

## Rollback Strategy

- Revert safe: yes (code-only)
- Database rollback: n/a
- Data written under the new behavior after revert: stored enabled/returned
  values remain in DB columns
- Blast radius if rollback is delayed: low — catalog flags only

## Open Questions

None — residual scope confirmed against main after #837 / #806/#801/#804/#799 merge.
