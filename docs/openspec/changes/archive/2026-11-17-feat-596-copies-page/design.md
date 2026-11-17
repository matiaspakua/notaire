> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Payments, people, budgets, deeds and managements already return a page. Copies still returned the table.

## Goals / Non-Goals

**Goals:**

- One page of copies, default 20, sort `idCopy,desc`.
- Unknown sort properties fall back to `idCopy`.

**Non-Goals:**

- Paging other catalogs.

## Decisions

The controller takes Spring `Pageable`. The port takes `page`, `size` and `sort`. Allowed sort fields are `idCopy`, `number` and `datePrinting`.

## Riesgos / Trade-offs

- [Bruno expected an array] → it reads `content`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Default request returns content and totalElements | unit | `SimpleControllersTest` |
| The created copy is inside content | API | `copies/02-list.yml` |

TDD: the controller test did not compile until `findPage` existed.

## Regression Strategy

- Existing copy controller tests.
- Full suite: `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

Existing copy screens still open the list. Newest-first keeps a new copy on page 1.

- Specs to add/update under `testing/e2e/tests/`: none
- Command: `preflight.sh --full`

## Deployment Strategy

- Flyway: no
- Configuration: none
- Smoke: the copies list

## Rollback Strategy

- Revert safe: yes
- Database rollback: none

## Migration Plan

One pull request.

## Open Questions

None.
