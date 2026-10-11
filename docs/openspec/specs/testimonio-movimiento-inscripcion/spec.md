# testimonio-movimiento-inscripcion Specification

## Purpose

Provides the movement circuit of a testimony through the property
registry: presenting it for registration (CU11), recording the
registration, withdrawing it (CU12), and re-entering it (CU44).

## Requirements

### Requirement: Enter a testimony for registration

The system SHALL allow recording the entry of a verified testimony at the
property registry for registration, storing `dateEntry`, per CU11 and
RF-30/RF-92. The path is
`POST /api/v1/testimony-movements/{idTestimony}/enter-registration`.

#### Scenario: A successful entry records the entry date

- **WHEN** the notary (Escribano) presents a verified testimony for
  registration
- **THEN** the system records `dateEntry` on the testimony movement

#### Scenario: Reject entry of a testimony already entered and not withdrawn

- **WHEN** the notary (Escribano) attempts to present for registration a
  testimony that already has a movement with `dateEntry` set and no
  `dateExit`
- **THEN** the system rejects the operation and responds with an error
  stating that the testimony is already in registration

#### Scenario: Reject entry of an unverified testimony

- **WHEN** the notary (Escribano) attempts to present for registration a
  testimony that has not yet been verified
- **THEN** the system rejects the operation and responds with an error
  stating that the testimony must be verified

### Requirement: Record registration of the testimony

The system SHALL allow marking an entered testimony as registered,
recording `dateRegistration`, per RF-32. The path is
`POST /api/v1/testimony-movements/{idTestimony}/register-inscription`.

#### Scenario: A successful record marks the testimony registered with a date

- **WHEN** the notary (Escribano) records registration of a testimony that
  has `dateEntry` set and is not yet `registered`
- **THEN** the system sets `registered` and records `dateRegistration`

#### Scenario: Reject recording registration without a prior entry

- **WHEN** the notary (Escribano) attempts to record registration of a
  testimony that has no movement with `dateEntry` set
- **THEN** the system rejects the operation and responds with an error
  stating that the registration entry is missing

### Requirement: Withdraw a registered testimony

The system SHALL allow withdrawing a testimony that is already
`registered`, recording `dateExit` and `cardNumber`, per CU12 and RF-33.
The path is `POST /api/v1/testimony-movements/{idTestimony}/withdraw`.

#### Scenario: A successful withdrawal records the exit date and carton number

- **WHEN** the notary (Escribano) withdraws a testimony with `registered`
  set, providing `cardNumber`
- **THEN** the system records `dateExit` and `cardNumber` on the testimony
  movement

#### Scenario: Reject withdrawal of a testimony that is not registered

- **WHEN** the notary (Escribano) attempts to withdraw a testimony that is
  not yet `registered`
- **THEN** the system rejects the operation and responds with an error
  stating that the testimony is not registered

### Requirement: Re-enter a withdrawn testimony

The system SHALL allow re-entering a testimony that was already withdrawn,
creating a new movement without losing the previous movement's history, per
CU44 and RF-33. The path is
`POST /api/v1/testimony-movements/{idTestimony}/reenter`.

#### Scenario: A successful re-entry creates a new movement and keeps history

- **WHEN** the notary (Escribano) re-enters a testimony whose most recent
  movement has `dateExit` set
- **THEN** the system creates a new testimony movement with its own
  `dateEntry`, leaving the previous movement unchanged

#### Scenario: Reject re-entry of a testimony that was not withdrawn

- **WHEN** the notary (Escribano) attempts to re-enter a testimony whose
  most recent movement has no `dateExit`
- **THEN** the system rejects the operation and responds with an error
  stating that the testimony was not withdrawn
