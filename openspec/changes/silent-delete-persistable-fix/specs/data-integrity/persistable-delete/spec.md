<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Guarantees that Spring Data JPA's `isNew()` check for every `@Entity` with an
optimistic-locking `version` column is derived from the entity's identifier,
never from `version`, so that `delete`/`deleteById` on an existing row always
emits the real `DELETE` instead of silently no-op'ing.

## ADDED Requirements

### Requirement: Entity deletion must not be silently skipped
Any `@Entity` in `com.licensis.notaire.negocio` with a `private int version`
field annotated `@Version` SHALL implement `Persistable<Integer>` with an
`isNew()` override based on whether its `@Id` field is `null`, so that
`JpaRepository#delete`/`#deleteById` on a row that exists in the database
always issues a `DELETE` statement.

#### Scenario: Deleting an existing entity removes the row
- **WHEN** an entity is loaded in one transaction (its `version` is `0`
  because it has never been updated) and then deleted via
  `repository.deleteById(id)` in a separate transaction
- **THEN** a subsequent `repository.findById(id)` in a third transaction
  returns empty — the row is actually gone from the database

#### Scenario: isNew() reflects identity, not version
- **WHEN** `isNew()` is called on an entity instance whose `@Id` field is not
  `null` (regardless of the value of `version`)
- **THEN** `isNew()` returns `false`

#### Scenario: isNew() is true only for a genuinely new, unsaved entity
- **WHEN** `isNew()` is called on a freshly constructed entity instance whose
  `@Id` field has not been set (`null`)
- **THEN** `isNew()` returns `true`

### Requirement: Bidirectional cascade collections do not mask a delete
`@OneToMany(cascade = CascadeType.ALL)` bidirectional associations in the
affected entities SHALL NOT cause Hibernate to re-insert or otherwise revert a
`DELETE` issued against the owning entity.

#### Scenario: Deleting a parent with cascaded children removes both
- **WHEN** an entity with a bidirectional `cascade = CascadeType.ALL` child
  collection is deleted
- **THEN** both the parent row and its child rows are absent from the database
  after the transaction commits
