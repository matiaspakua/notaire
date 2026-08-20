# presupuesto-tramite-relation Specification

## Purpose

Defines the association between Presupuesto and Tramite: which side owns the foreign
key and the resulting cardinality, so the two entities have exactly one consistent
relation instead of the two contradictory ones found by
`openspec/explore_functional_report.md` §3.5.

## Requirements

### Requirement: Presupuesto-Tramite association ownership
The system SHALL model the association between Presupuesto and Tramite with Tramite
owning the foreign key to Presupuesto (a Tramite references at most one Presupuesto),
and Presupuesto SHALL expose the inverse as a collection of associated Tramites (a
Presupuesto may be associated with zero or more Tramites). The system SHALL NOT expose
a separate, reverse foreign key from Presupuesto to a single Tramite.

#### Scenario: A Tramite is created and associated to a Presupuesto
- **WHEN** a Tramite is created as part of a gestión referencing an existing
  Presupuesto (CU02 – Iniciar Gestión)
- **THEN** the Tramite record stores a reference to that Presupuesto, and that
  Presupuesto's associated-Tramite collection includes the new Tramite

#### Scenario: A Presupuesto is associated with more than one Tramite
- **WHEN** two different Tramites are each created referencing the same Presupuesto
- **THEN** both Tramites persist successfully and that Presupuesto's
  associated-Tramite collection contains both

#### Scenario: A Tramite belongs to at most one Presupuesto
- **WHEN** a Tramite that already references Presupuesto A is updated to reference
  Presupuesto B
- **THEN** the Tramite's reference is replaced with Presupuesto B, and the Tramite no
  longer appears in Presupuesto A's associated-Tramite collection

#### Scenario: Presupuesto no longer exposes a single-Tramite reference
- **WHEN** a Presupuesto is read through the API (CU45 – Modificar presupuesto, or any
  read of a Presupuesto)
- **THEN** its representation does not contain a single "tramite" field; the
  association is only observable from the Tramite side
