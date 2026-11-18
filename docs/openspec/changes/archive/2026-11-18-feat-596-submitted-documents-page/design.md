> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Copies, payments, people, budgets, deeds and managements already return a page. Submitted documents still returned the table. Only the documents screen loads that list.

## Goals / Non-Goals

**Goals:**

- One page, default 20, sort `idSubmittedDocument,desc`.
- Unknown sort properties fall back to `idSubmittedDocument`. `dateEntry` is also accepted.

**Non-Goals:**

- Paging upcoming expirations.

## Decisions

The controller takes Spring `Pageable`. The catalog port takes `page`, `size` and `sort`.

## Riesgos / Trade-offs

- [Bruno and the integration test expected an array] → they read `content`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| The list body has content and totalElements | integration | `SubmittedDocumentControllerTest` |
| The created document is inside content | API | `submitted-documents/02-list.yml` |

TDD: the list assertion was changed to a page before the controller returned one.

## Regression Strategy

- Full suite: `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

Existing document screens still open the list. Newest-first keeps a new document on page 1.

- Specs to add/update under `testing/e2e/tests/`: none
- Command: `preflight.sh --full`

## Deployment Strategy

- Flyway: no
- Configuration: none
- Smoke: the documents list

## Rollback Strategy

- Revert safe: yes
- Database rollback: none

## Migration Plan

One pull request.

## Open Questions

None.
