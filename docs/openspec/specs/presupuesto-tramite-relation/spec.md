# presupuesto-tramite-relation Specification

## Purpose

Defines the association between Presupuesto and Procedure: which side owns the foreign
key and the resulting cardinality, so the two entities have exactly one consistent
relation instead of the two contradictory ones found by
`docs/openspec/explore_functional_report.md` §3.5.

## Requirements

### Requirement: Presupuesto-Procedure association ownership
The system SHALL model the association between Presupuesto and Procedure with Procedure
owning the foreign key to Presupuesto (a Procedure references at most one Presupuesto),
and Presupuesto SHALL expose the inverse as a collection of associated Tramites (a
Presupuesto may be associated with zero or more Tramites). The system SHALL NOT expose
a separate, reverse foreign key from Presupuesto to a single Procedure.

#### Scenario: A Procedure is created and associated to a Presupuesto
- **WHEN** a Procedure is created as part of a gestión referencing an existing
  Presupuesto (CU02 – Iniciar Gestión)
- **THEN** the Procedure record stores a reference to that Presupuesto, and that
  Presupuesto's associated-Procedure collection includes the new Procedure

#### Scenario: A Presupuesto is associated with more than one Procedure
- **WHEN** two different Tramites are each created referencing the same Presupuesto
- **THEN** both Tramites persist successfully and that Presupuesto's
  associated-Procedure collection contains both

#### Scenario: A Procedure belongs to at most one Presupuesto
- **WHEN** a Procedure that already references Presupuesto A is updated to reference
  Presupuesto B
- **THEN** the Procedure's reference is replaced with Presupuesto B, and the Procedure no
  longer appears in Presupuesto A's associated-Procedure collection

#### Scenario: Presupuesto no longer exposes a single-Procedure reference
- **WHEN** a Presupuesto is read through the API (CU45 – Modificar presupuesto, or any
  read of a Presupuesto)
- **THEN** its representation does not contain a single "tramite" field; the
  association is only observable from the Procedure side
