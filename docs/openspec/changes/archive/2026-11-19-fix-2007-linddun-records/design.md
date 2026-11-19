> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`Person` identifiers were LD-1. Testimony has number, flags, notes, and an optional deed link, and no person column. Folio adds the notary person. Submitted documents store `deliveredBy`, a card number, notes, dates, and amounts, and no file bytes. `ApiModulePathRegistry` maps testimonies and folios to `escrituras` and submitted documents to `tramites`. `@AuditPersonalDataRead` appears only on `PersonController`.

## Goals / Non-Goals

**Goals:**

- The markdown model and `system.stride.yml` share LD-7, LD-8, and LD-9.
- Each open row names a control file or a test.
- The text does not name a statute.

**Non-Goals:**

- Implementing read audit for the three resources.
- Adding a privacy notice.

## Decisions

Leave LD-9 Open instead of annotating controllers in this change. The verification is the existing Person read-audit test, and the residual says the other reads have no event. LD-4 and LD-5 cite the program section so they no longer use a closed issue as their only pointer.

## Riesgos / Trade-offs

- [A reader treats LD-7 as a claim that cross-tenant access is tested for these paths] → the row points at the module grant and at SR-11 for intra-tenant sharing. It does not add an isolation test.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Register and markdown share LD-7, LD-8, LD-9 and the field names | unit | `test_linddun_rows_and_module_grants_match_the_system_model` |

TDD: the test failed with LD-7, LD-8, and LD-9 missing from the register.

## Regression Strategy

- `python3 -m unittest security.tests.test_threat_register security.tests.test_assessment_currency`
- `bash workspace/sdlc/preflight.sh --full` before push.

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- No runtime change.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
