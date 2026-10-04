## Purpose

Extend the gestión workflow trace and animated dashboard tracker so the
post-firma circuit includes linear testimonio statuses and represents the
unbounded reingreso loop as a secondary `TestimonyMovement` timeline
(strategy b from #841) — without forcing reingreso into mutually exclusive
`WorkflowNode`s.

## ADDED Requirements

### Requirement: Management statuses for the post-firma circuit through withdrawal
The system SHALL represent "Testimonio Generado", "Testimonio Ingresado a
Inscripción", and "Testimonio Retirado" as their own `ManagementStatus` rows,
reachable from "Gestión con Escritura Firmada" on the standard
`WorkflowDefinition`.

#### Scenario: Generating testimony advances management status
- **WHEN** testimony is generated for a signed deed on a gestión whose procedure
  type uses the standard `WorkflowDefinition`
- **THEN** the gestión status becomes "Testimonio Generado"

#### Scenario: Submitting testimony for inscription advances management status
- **WHEN** a testimony in status "Testimonio Generado" is submitted for inscription
- **THEN** the gestión status becomes "Testimonio Ingresado a Inscripción"

#### Scenario: Withdrawing testimony advances management status
- **WHEN** an inscribed testimony is withdrawn
- **THEN** the gestión status becomes "Testimonio Retirado"

### Requirement: Workflow trace includes testimony movements for the gestión
`GET /api/v1/gestiones/{id}/workflow-trace` SHALL include, alongside the
existing node trace, the `TestimonyMovement` rows associated with the gestión
when present, without changing the shape of `nodes` / `transitions` /
`nodeStatuses` already consumed by the UI.

#### Scenario: Gestión with in-progress testimony includes movements in the trace
- **WHEN** the trace is requested for a gestión whose testimony has at least one
  `TestimonyMovement`
- **THEN** the response includes those movements in chronological order, each
  with entry date, exit date (if any), and whether it returned observed
  (`dateExit != null && !registered`)

#### Scenario: Gestión without testimony omits movements
- **WHEN** the trace is requested for a gestión whose deed is not yet signed
- **THEN** the response has no testimony movements (empty/absent list) and the
  existing node trace behaves exactly as before this change

#### Scenario: A reingreso appends a movement without dropping prior ones
- **WHEN** a testimony that already has entry/observation movements re-enters
  inscription
- **THEN** the trace includes the new movement together with all prior movements
  of the same testimony, in chronological order

### Requirement: Animated diagram shows reingreso count for the current testimony
The animated workflow diagram SHALL show, on the "Testimonio Ingresado a
Inscripción" node, the reingreso count of the current testimony when that
count is greater than zero.

#### Scenario: Testimony with reingresos shows count on the inscription node
- **WHEN** the gestión trace includes a testimony with 2 movements marked
  returned-observed
- **THEN** the "Testimonio Ingresado a Inscripción" node shows a reingreso
  indicator with count 2

#### Scenario: Testimony without reingresos hides the indicator
- **WHEN** the gestión trace includes a testimony with a single entry movement
  and no observations
- **THEN** the "Testimonio Ingresado a Inscripción" node shows no reingreso
  indicator

#### Scenario: Workflow without post-firma nodes degrades safely
- **WHEN** the trace is requested for a gestión whose assigned
  `WorkflowDefinition` lacks the post-firma testimonio nodes
- **THEN** the animated diagram falls back to current behavior (no those nodes,
  no movement timeline) without a visible error
