# gestion-asignacion-suplencia Specification

## Purpose

Makes recording a substitution (CU22) affect the assignment of new
managements, instead of remaining an isolated record that nothing consults
(CU59, RF-89).

## Requirements

### Requirement: Redirect management assignment to the active substitute

The system SHALL assign a management to the substitute (`fkIdSubstitute`) of an
active `Substitution` when the requested notary is the substituted notary
(`fkIdSubstituted`) and the management date falls within
`dateStart`–`dateEnd`, per CU22 / CU02 / RF-89 / RF-115 — including plain
`POST`/`PUT /api/v1/managements` as well as complete-case paths already covered
by #836.

#### Scenario: Creating a management with no active substitution

- **WHEN** a management is created with a notary that has no active
  substitution for the management date
- **THEN** the system assigns the management to the requested notary

#### Scenario: Creating a management with an active substitution

- **WHEN** a management is created with a notary that has an active
  substitution as the substituted notary for the management date
- **THEN** the system assigns the management to that substitution's substitute
  instead of the requested notary

#### Scenario: Editing a management with an active substitution

- **WHEN** an existing management is edited changing the notary to one that
  has an active substitution as the substituted notary for the management date
- **THEN** the system assigns the management to that substitution's substitute
  instead of the requested notary

#### Scenario: Plain POST create redirects under active substitution

- **WHEN** a client creates a management via plain `POST /api/v1/managements`
  with `notaryPersonId` of a notary covered by an active substitution for
  `dateStart`
- **THEN** the persisted `fkIdNotaryPerson` is the substitute, not the
  requested notary

#### Scenario: Plain PUT update redirects under active substitution

- **WHEN** a client updates a management via plain `PUT /api/v1/managements/{id}`
  setting `notaryPersonId` to a notary covered by an active substitution for
  the management date
- **THEN** the persisted `fkIdNotaryPerson` is the substitute, not the
  requested notary

### Requirement: Record the redirection on the management

The system SHALL record in the management notes that assignment was redirected
by an active substitution, identifying the requested notary and the assigned
substitute — including when redirection happens on plain POST/PUT paths. The
persisted notes start with
`Management redirected by active substitution`.

#### Scenario: Notes record the redirection

- **WHEN** the system assigns a management to the substitute instead of the
  requested notary
- **THEN** the management notes identify the requested notary and the assigned
  substitute

#### Scenario: Plain path notes record redirection

- **WHEN** plain POST or plain PUT redirects notary assignment under an active
  substitution
- **THEN** the persisted notes contain
  `Management redirected by active substitution` and identify both notaries
