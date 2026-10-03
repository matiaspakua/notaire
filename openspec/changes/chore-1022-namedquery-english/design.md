> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1022 (DOC / REFACTOR / priority:low). Verified on updated `origin/main`:

| Location | Finding |
|----------|---------|
| `business/*.java` | ~137 `@NamedQuery(name=…)` annotations |
| Spanish prefixes | ~26 entity prefixes remain; Folio/Item/Person already English |
| `createNamedQuery` | ~49 call sites in main + test |
| Latent bug | `Person` declares `Person.*`; `PersonJpaController` + tests call `Persona.*` |
| Undeclared callers | `Historial.estadoActualGestion`, `Presupuesto.findByPersonaTramite` called but not declared — rename call-site strings for consistency only |

Package is `com.licensis.notaire.business` (not legacy `negocio`).

## Goals / Non-Goals

**Goals:**

- English NamedQuery name strings (prefix + method tail) across all entities.
- Matching `createNamedQuery` / Mockito stubs.
- Fix `Persona.*` → `Person.*` mismatch.
- Static hygiene test as the TDD ratchet.

**Non-Goals:**

- JPQL body edits
- Flyway / schema / API / DTO / frontend renames
- Broad Spanish comment translation outside NamedQuery identifiers

## Decisions

1. **Single PR batch rename** — all entities together; names are string literals
   with no migration coupling. Alternative rejected: per-entity slices (overkill
   for cosmetic identifiers).

2. **Rename map from issue instructions** — Spanish prefix → English entity
   class name; method tails Englishized to match entity field semantics
   (e.g. `findByFechaEscrituracion` → `findByDeedDate`).

3. **FolioCopy prefix** — map uses `FolioCopy` even though the Java class is
   `FolioCopies` (plural). Follow the issue rename map for consistency with
   other singular English prefixes.

4. **Undeclared NamedQueries** — rename call-site strings only; do not invent
   missing `@NamedQuery` declarations in this change (pre-existing gap).

5. **Hygiene test scans source files** — static inventory of
   `@NamedQuery(name=` and `createNamedQuery("` under `backend-api`, failing on
   forbidden Spanish prefixes / `Persona.` / known Spanish Folio|Item|Person
   tails. No runtime EntityManager bootstrap required.

6. **Spring Data collision params** — English NamedQuery names that match
   `*Repository.findBy…` methods take precedence over derived queries. For those
   collisions only, Englishize the JPQL *named parameter* tokens (e.g.
   `:numeroIdentificacion` → `:identificationNumber`) and matching
   `setParameter` call sites so Spring Data binding works. Entity/field paths in
   JPQL stay untouched. `User.findByFkIdPerson` is renamed to
   `User.findByPersonId` (id param) to avoid colliding with
   `UserRepository.findByFkIdPerson(Person)`.

## Riesgos / Trade-offs

- [Missed call site] → Mitigation: hygiene inventory + full `mvn test` of JPA
  unit mocks that stub `createNamedQuery`.
- [Wrong method-tail English] → Mitigation: PR checklist with rename-map
  examples; names are internal-only.
- [Undeclared queries still fail at runtime if invoked] → Pre-existing; out of
  scope to add JPQL; only rename the string.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No Spanish NamedQuery prefixes | unit (static) | `NamedQueryEnglishNamesHygieneTest` |
| No Persona createNamedQuery mismatch | unit (static) | same |
| Folio/Item/Person English tails | unit (static) | same |
| Scope naming-only | review / git diff | PR checklist |

- New unit tests: `backend-api/src/test/java/.../unit/NamedQueryEnglishNamesHygieneTest.java`
- New integration tests: n/a
- Coverage impact: negligible (static file scan)

TDD: add hygiene test first, observe FAIL on Spanish prefixes / Persona.*, then
rename until green.

## Regression Strategy

- Existing tests affected: JPA unit tests that mock `createNamedQuery("Spanish…")`
  — update expected name strings without weakening assertions.
- Full suite: `mvn test -pl backend-api` then `mvn verify -pl backend-api` as needed
- HTTP/Bruno: n/a (no API change)
- Legacy `jpa` package: call sites updated in place

## Playwright Strategy

n/a — no UI surface. NamedQuery identifiers are backend-only.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ordinary backend deploy; no schema coupling
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy: hit a known NamedQuery path (e.g. login / person
  lookup) or rely on unit suite — no new runtime surface

## Rollback Strategy

- Revert safe: yes — pure string rename; revert the PR
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: none (identifiers only)

## Migration Plan

1. Hygiene test (red)
2. Mechanical rename of `@NamedQuery` names + `createNamedQuery` + mocks
3. Hygiene green + full backend tests
4. CHANGELOG + PR

## Open Questions

None — rename map and scope fixed by issue #1022 / coordinator brief.
