# jpa-namedquery-english-names Specification

## Purpose

Ensure JPA `@NamedQuery` *name* strings and `EntityManager.createNamedQuery`
call sites under `backend-api` use English entity prefixes and English method
tails after the #973 domain-schema rename. Source: #1022 (follow-up to #973).

## Requirements

### Requirement: NamedQuery names use English entity prefixes

Every `@NamedQuery(name = "…")` declaration under
`backend-api/src/main/java/com/licensis/notaire/business/` MUST use an English
entity prefix (`Deed`, `User`, `AuditRecord`, `Person`, `Folio`, `Item`, and
the rest of the renamed entities). The Spanish prefixes recorded in
`NamedQueryEnglishNamesHygieneTest` MUST NOT appear, including `Escritura.`,
`Usuario.`, `Presupuesto.`, `Tramite.`, `Persona.`, `Historial.`, and
`EstadoDeGestion.`.

#### Scenario: No Spanish NamedQuery prefixes in business entities

- **WHEN** the hygiene inventory scans `@NamedQuery(name = "` under
  `business/*.java`
- **THEN** zero matches use a forbidden Spanish entity prefix

### Requirement: createNamedQuery call sites use English names

Every `createNamedQuery("…")` call site under `backend-api` (main + test) MUST
use the English NamedQuery name matching the entity declaration. Call sites
MUST NOT use `Persona.*`. The entity declares `Person.*`.

#### Scenario: No Persona createNamedQuery mismatch

- **WHEN** the hygiene inventory scans `createNamedQuery("` under `backend-api`
- **THEN** zero matches start with `Persona.` and zero matches use a forbidden
  Spanish entity prefix

### Requirement: Method tails are English on Folio, Item, and Person

For prefixes that are already English (`Folio`, `Item`, `Person`), NamedQuery
method tails MUST NOT retain Spanish tokens that were part of the #1022
inventory (e.g. `Folio.findByNumero`, `findByAnio`, `findByPresupuesto`,
`findByIdPersona`, `findByNumeroIdentificacion`).

#### Scenario: Folio Item Person method tails are English

- **WHEN** the hygiene inventory inspects NamedQuery names with prefixes
  `Folio.`, `Item.`, or `Person.`
- **THEN** none of the forbidden Spanish method-tail tokens remain

### Requirement: JPQL bodies and schema are unchanged

This change MUST NOT alter JPQL `query = "…"` bodies, Flyway migrations, REST
contracts, DTOs, or frontend identifiers.

#### Scenario: Scope stays naming-only

- **WHEN** the change is reviewed
- **THEN** diffs are limited to NamedQuery name strings, matching
  `createNamedQuery` / mock strings, the hygiene test, OpenSpec artifacts, and
  CHANGELOG — with no Flyway or API contract edits
