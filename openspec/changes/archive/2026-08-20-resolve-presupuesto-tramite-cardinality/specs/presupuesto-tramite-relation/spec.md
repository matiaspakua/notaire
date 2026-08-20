## Purpose

Defines the association between Presupuesto and Tramite: which side owns the foreign
key and the resulting cardinality, so the two entities have exactly one consistent
relation instead of the two contradictory ones found by
`openspec/explore_functional_report.md` §3.5.

## ADDED Requirements

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

## REMOVED Requirements

### Requirement: Presupuesto references a single Tramite
**Reason**: This reverse relation contradicted the Tramite→Presupuesto relation above
and was populated only by the deprecated legacy write path, never by the live modern
case-creation flow (CU02). Keeping both relations left the true cardinality undefined,
risking a future change silently deciding it by accident.
**Migration**: Any code or client that read a Presupuesto's single "tramite" field must
instead query Tramites by their Presupuesto reference. The schema migration that drops
the underlying column verifies it contains no non-null values first; if it finds any,
the migration fails and stops rather than silently discarding data.

#### Scenario: Migration refuses to drop the column if data would be lost
- **WHEN** the schema migration runs and finds at least one existing row with a
  non-null legacy Presupuesto→Tramite reference
- **THEN** the migration fails without dropping the column, surfacing the conflicting
  rows for manual review before it can be re-run
