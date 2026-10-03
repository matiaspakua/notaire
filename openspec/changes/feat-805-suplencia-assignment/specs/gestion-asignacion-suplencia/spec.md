<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## MODIFIED Requirements

### Requirement: Redirigir la asignación de gestión al suplente activo

The system SHALL assign a management to the substitute (`fkIdSubstitute`) of an
active `Substitution` when the requested notary is the substituted notary
(`fkIdSubstituted`) and the management date falls within
`dateStart`–`dateEnd`, per CU22 / CU02 / RF-89 / RF-115 — including plain
`POST`/`PUT /api/v1/gestiones` as well as complete-case paths already covered
by #836.

#### Scenario: Creación de gestión sin suplencia activa

- **WHEN** a management is created with a notary that has no active
  substitution for the management date
- **THEN** the system assigns the management to the requested notary

#### Scenario: Creación de gestión con suplencia activa

- **WHEN** a management is created with a notary that has an active
  substitution as the substituted notary for the management date
- **THEN** the system assigns the management to that substitution's substitute
  instead of the requested notary

#### Scenario: Edición de gestión con suplencia activa

- **WHEN** an existing management is edited changing the notary to one that
  has an active substitution as the substituted notary for the management date
- **THEN** the system assigns the management to that substitution's substitute
  instead of the requested notary

#### Scenario: Plain POST create redirects under active substitution

- **WHEN** a client creates a management via plain `POST /api/v1/gestiones`
  with `notaryPersonId` of a notary covered by an active substitution for
  `dateStart`
- **THEN** the persisted `fkIdNotaryPerson` is the substitute, not the
  requested notary

#### Scenario: Plain PUT update redirects under active substitution

- **WHEN** a client updates a management via plain `PUT /api/v1/gestiones/{id}`
  setting `notaryPersonId` to a notary covered by an active substitution for
  the management date
- **THEN** the persisted `fkIdNotaryPerson` is the substitute, not the
  requested notary

### Requirement: Registrar el redireccionamiento en la gestión

The system SHALL record in the management notes that assignment was redirected
by an active substitution, identifying the requested notary and the assigned
substitute — including when redirection happens on plain POST/PUT paths.

#### Scenario: Observaciones registran el redireccionamiento

- **WHEN** the system assigns a management to the substitute instead of the
  requested notary
- **THEN** the management notes identify the requested notary and the assigned
  substitute

#### Scenario: Plain path notes record redirection

- **WHEN** plain POST or plain PUT redirects notary assignment under an active
  substitution
- **THEN** the persisted notes contain the redirection text identifying both
  notaries
