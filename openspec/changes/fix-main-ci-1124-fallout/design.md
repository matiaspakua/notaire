> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`BudgetTemplateJpaController.create` (line 51) does
`budgetTemplate.getConcept().getIdConcept()` before `em.getReference`. After
#1124, `BudgetTemplateController.create` built a `BudgetTemplate` with only the
composite PK and notes — `getConcept()` was null → NPE → HTTP 500. Integration
and Bruno suites that POST `fkIdConcept` / `fkIdProcedureType` failed.

## Goals / Non-Goals

**Goals:**

- Hydrate `concept` and `procedureType` from request IDs before JPA create.
- Keep DTO request shape (`fkIdConcept`, `fkIdProcedureType`, optional nested PK).
- Align remaining tests with DTO contracts; restore Unit/Integration/Coverage/Bruno.
- Satisfy Process Checks with this OpenSpec folder.

**Non-Goals:**

- Replacing `BudgetTemplateJpaController` with Spring Data.
- Changing Bruno request field names (already DTO-shaped).
- Closing #1068 again.

## Decisions

1. **Repository `getReferenceById`** — inject `ConceptRepository` and
   `ProcedureTypeRepository`; set associations before legacy create. Rationale:
   matches JpaController's expectation without loading full graphs when IDs exist.
2. **`existsById` guard → 400** — missing FK returns bad request instead of NPE/500.
3. **OpenSpec folder over `sdlc-exception`** — agents cannot set the label.

## Riesgos / Trade-offs

- [getReferenceById on missing id] → Mitigated by `existsById` before create.
- [Static EMF JpaControllerProvider] → Unchanged; known DirtiesContext pattern in tests.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Create template with DTO IDs returns 201 | integration | `BudgetTemplateSerializationIntegrationTest` |
| Duplicate template returns 409 | integration | `BusinessWorkflowIntegrationTest` |
| Concept-in-use link via template create | integration | `ConceptReferentialIntegrityTest` |
| Bruno budget-templates create | API | `api-test/budget-templates/01-create.yml` |

## Regression Strategy

- Re-run previously failing suites: AuditAspectTest, Management/Substitution/Copy
  helpers, BudgetTemplate create paths, Bruno budget-templates.
- Full Unit + Integration + Coverage Gate on PR CI.

## Playwright Strategy

n/a — no UI surface in this hotfix.

## Deployment Strategy

- Ship via normal PR merge to `main`; backend image rebuild picks up
  `BudgetTemplateController` only. No Flyway migration, no `.env` change.
- Smoke: `POST /api/v1/plantilla-presupuestos` with valid
  `fkIdProcedureType`/`fkIdConcept` returns 201; Bruno `budget-templates/01-create`.

## Rollback Strategy

- Revert the merge commit of this PR on `main` if create regressions appear.
- Rollback restores the previous NPE/500 behavior for DTO-only creates; prefer
  forward-fix of association hydration instead.

