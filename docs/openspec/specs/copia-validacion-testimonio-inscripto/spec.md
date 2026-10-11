# copia-validacion-testimonio-inscripto Specification

## Purpose

Stops a copy from being issued for a testimony that was already filed and
registered, protecting the integrity of the notarial protocol
described in CU87.

## Requirements

### Requirement: Reject a copy of a testimony that is already registered
The system SHALL reject creating a copy when the source testimony
has at least one movement marked as registered.

#### Scenario: Testimony with a registered movement
- **WHEN** creating a copy is requested for a source testimony that has
  a `TestimonyMovement` with `registered = true`
- **THEN** the system rejects the request with `409 Conflict` and does not
  create the copy

#### Scenario: Testimony with no registered movements
- **WHEN** creating a copy is requested for a source testimony that has
  no `TestimonyMovement` with `registered = true`
- **THEN** the copy is created as usual (behavior unchanged)

#### Scenario: Testimony with no recorded movements
- **WHEN** creating a copy is requested for a source testimony that has
  no recorded `TestimonyMovement`
- **THEN** the copy is created as usual (behavior unchanged)
