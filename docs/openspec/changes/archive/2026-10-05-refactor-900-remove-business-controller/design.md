> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #900, Use Case CU76 – Quality Assurance and Testing Infrastructure. SAD section 11 lists the god class as a critical debt; a call-graph check shows production code reaches 2 of its 127 public methods.

## Goals / Non-Goals

**Goals:** BusinessController gone; the two lookups tested; no behaviour loss beyond the documented exact-match fix.
**Non-Goals:** Migrating the *JpaController classes (#576); changing REST contracts.

## Decisions

1. Extract a tiny class instead of distributing 127 methods to services: the other 125 have no production caller (compile-time proof: the build passes after deletion), so porting them would revive dead code.
2. The lookup takes the id or name, not a DTO: the old signature read `dtoPerson.getDtoIdentificationType()` before the callers had set it, which was a latent NPE.
3. The catalog is injected through a supplier so the class is unit-testable; the default instance reads the existing JPA controller, keeping entities free of Spring.

## Riesgos / Trade-offs

- Exact matching can differ from `contains` for unusual names; the seed data uses full names, and the integration tests that create people with identification types cover it.
- A reflective or XML reference to the class: grep over sources and resources found none.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| An id matches exactly | unit | `IdentificationTypeLookupTest` |
| The id is unknown or null | unit | `IdentificationTypeLookupTest` |
| A name matches exactly | unit | `IdentificationTypeLookupTest` |
| The name is unknown or null | unit | `IdentificationTypeLookupTest` |
| BusinessController and its excludes are gone | static | `scripts/test_no_business_controller.py` |

- New unit tests (`src/test/java/.../unit/`): `IdentificationTypeLookupTest`
- New integration tests: existing person/management integration tests cover the callers
- Coverage impact: positive: the new class is fully covered; the deleted class was excluded from the gate

## Regression Strategy

- Existing tests affected: PersonEntityTest, DeedManagementEntityTest if they reach the fallback
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; login and person lookup work in the stack

## Rollback Strategy

- Revert the PR.
