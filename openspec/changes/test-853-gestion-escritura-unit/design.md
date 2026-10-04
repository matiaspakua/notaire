> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Entity renamed: `GestionDeEscritura` → `DeedManagement`; helpers renamed
`getDtoEscribano` → `getDtoNotary`, `fkIdPersonaEscribano` →
`fkIdNotaryPerson`, `fkIdEstadoDeGestion` → `fkIdManagementStatus`. On tip
(`8d8b72af`), `DeedManagementEntityTest` has 15 `@Test` methods covering
create/link/equals/hashCode/setAtributos with status present — but **no**
`getDto` / `getDtoNotary` null-path tests. Residual NPE sites:

1. `DeedManagement.getDto()` → `fkIdManagementStatus.getDto()` (status null)
2. `getDtoNotary()` → dereference `fkIdNotaryPerson` (notary null)
3. `getDtoNotary()` → `getFkIdIdentificationType().getIdIdentificationType()`
4. `setAtributos()` → `dtoManagement.getStatus()` into `setAtributo` without
   null guard (NPE / invalid DTO path)
5. `Person.getDto()` → identification type null; `DeedManagementList` null
   before `.isEmpty()`

Issue text still mentions "28 tests / 15 passing" — treat that as historical;
implement measures success by green expanded suite + no NPE on cited paths.

## Goals / Non-Goals

**Goals:**
- Null-safe DTO mapping for the cited methods.
- Expand unit tests so every #853 AC scenario is covered and green.
- Keep constructor list initialization semantics explicit in tests.

**Non-Goals:**
- Broad entity mapper refactor; new REST endpoints; Flyway; FE/E2E.

## Decisions

- **Return null for missing notary/status on DTO rather than empty stubs.**
  Callers already tolerate null personNotary (setAtributos test). Empty stub
  Person/Status would invent ids and confuse clients.
- **Guard identification type**: if null, omit `DtoIdentificationType` (null)
  instead of calling `BusinessController.asociarNameIdentificationType`.
- **setAtributos with null status**: skip status assignment (leave existing)
  rather than throwing — mirrors null personNotary branch.
- **Person.DeedManagementList**: treat null like empty (skip loop); optionally
  lazy-init on read only if existing patterns do so — prefer null-check without
  mutating entity unless tests require init.
- **Test class stays** `DeedManagementEntityTest` (English class, DisplayName
  may still say GestionDeEscritura historically — update DisplayName to
  DeedManagement for clarity).
- **Target ~28 scenarios** by grouping: construction, equals/hashCode,
  setAtributos branches, getDto/getDtoNotary null+happy, list init, Person
  getDto null paths related to gestión. Exact count is not a gate if coverage
  of AC scenarios is complete.

## Riesgos / Trade-offs

- [Clients that relied on NPE→500 may now see null fields] → Mitigation:
  intentional; document in CHANGELOG; preferable to 500.
- [BusinessController singleton call inside getDtoNotary] → Mitigation: only
  call when identification type present and name not already hydrated; unit
  tests prefer the hydrated-name path.
- [Scope creep into all entities] → Mitigation: only DeedManagement + Person
  paths in #853.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| getDto with null status does not NPE | unit | `DeedManagementEntityTest` |
| getDtoNotary with null notary returns null | unit | same |
| getDtoNotary with null identification type | unit | same |
| setAtributos null status leaves status unset/unchanged | unit | same |
| Constructor initializes empty procedure/history lists | unit | same |
| Person.getDto null identification type | unit | `PersonEntityTest` |
| Person.getDto null DeedManagementList | unit | `PersonEntityTest` |
| Happy-path getDto with status+notary | unit | `DeedManagementEntityTest` |

- New unit tests (`src/test/java/.../unit/`): expand
  `DeedManagementEntityTest`; add Person cases as needed
- New integration tests (`src/test/java/.../integration/`): n/a unless a
  controller path proves an NPE in integration (optional)
- Coverage impact (JaCoCo ratchet floor; 80% target): should raise entity
  coverage; must not lower floor

## Regression Strategy

- Existing tests affected: `DeedManagementEntityTest` (extend, do not weaken
  equals/setAtributos asserts); any test that expected NPE must be rewritten
  to expect null-safe behavior
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: n/a for pure unit entity fix (still run preflight)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: n/a — no UI surface
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: n/a — record in tasks 7.4

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: backend jar only
- Configuration or `.env` keys to add (add to `.env.example`, never commit secrets): none
- Feature flag: no
- Smoke test after deploy (Gate 5): `mvn test -pl backend-api -Dtest=DeedManagementEntityTest,PersonEntityTest` green in CI; optional GET gestión with sparse associations

## Rollback Strategy

- Revert safe: yes (pure Java + tests)
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: low

## Migration Plan

1. Write failing unit tests for each NPE path (observe red).
2. Implement null guards until green.
3. Expand remaining coverage cases (equals/list/happy getDto).
4. `mvn test` + jacoco; CHANGELOG; PR `Closes #853`.

## Open Questions

None. Historical "28 tests" count is a target, not a hard gate — AC is
all cited NPE paths fixed and suite green.
