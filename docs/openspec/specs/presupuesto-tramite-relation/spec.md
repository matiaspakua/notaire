# presupuesto-tramite-relation Specification

## Purpose

Defines the association between `Budget` and `Procedure`: which side owns the
foreign key and the resulting cardinality, so the two entities have exactly
one consistent relation instead of the two contradictory ones found by
`docs/openspec/explore_functional_report.md` §3.5.

## Requirements

### Requirement: Budget-Procedure association ownership

The system SHALL model the association between `Budget` and `Procedure` with
`Procedure` owning the foreign key to `Budget` (column `fk_id_budget`; a
`Procedure` references at most one `Budget` through `fkIdBudget`), and
`Budget` SHALL expose the inverse as `procedureList` (a `Budget` may be
associated with zero or more procedures). The system SHALL NOT expose a
separate, reverse foreign key from `Budget` to a single `Procedure`.

#### Scenario: A Procedure is created and associated to a Budget

- **WHEN** a `Procedure` is created as part of a management referencing an
  existing `Budget` (CU02 – Iniciar Gestión)
- **THEN** the `Procedure` record stores `fk_id_budget` for that `Budget`,
  and that `Budget`'s `procedureList` includes the new `Procedure`

#### Scenario: A Budget is associated with more than one Procedure

- **WHEN** two different procedures are each created referencing the same
  `Budget`
- **THEN** both procedures persist successfully and that `Budget`'s
  `procedureList` contains both

#### Scenario: A Procedure belongs to at most one Budget

- **WHEN** a `Procedure` that already references `Budget` A is updated to
  reference `Budget` B
- **THEN** the `Procedure`'s `fkIdBudget` is replaced with `Budget` B, and
  the `Procedure` no longer appears in `Budget` A's `procedureList`

#### Scenario: Budget no longer exposes a single-Procedure reference

- **WHEN** a `Budget` is read through the API (CU45 – Modificar presupuesto,
  or any read of a `Budget`)
- **THEN** its representation does not contain a single procedure foreign
  key; the association is `procedureList` on `Budget` and `fkIdBudget` on
  `Procedure`
