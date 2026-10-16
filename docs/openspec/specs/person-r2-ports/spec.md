# person-r2-ports Specification

## Purpose
TBD - created by archiving change refactor-1881-person-r2-ports. Update Purpose after archive.

## Requirements

### Requirement: Person R2 ports are entity-free
The person application slice MUST NOT depend on `business.*` entities or Spring Data repositories via entity-speaking ports. Ops adapters own entity-bound helpers locally. Controllers call inbound/ops ports only.

#### Scenario: R2 baseline shrinks for person
- **WHEN** HexagonArchitectureTest runs against the updated tree
- **THEN** the ~8 person R2 baseline edges for PersonUseCase, PersonRepositoryPort, and PersonService are gone

#### Scenario: Live OpsPorts remain view-safe
- **WHEN** PersonController lists, creates, updates, or deletes people
- **THEN** it uses ManagePeopleUseCase / QueryPeopleUseCase backed by PersonOpsPort (views only); PersonService lives under adapter.out.persistence.person
