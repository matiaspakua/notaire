# persona-validation-error-toast Specification

## Purpose

Ensure the Personas dashboard surfaces backend validation messages on non-409
save failures (instead of a generic save error) and that Dedup-EDGE proves the
empty-identification path. Source: #945; CU17 / CU61.

## Requirements

### Requirement: Non-409 ApiError shows extractable backend message

When persona create or update fails with an `ApiError` whose status is not 409
and whose body yields a parseable message via `extractApiError` (or the shared
mutation-error helper), the UI MUST toast that message. The generic
`errorSave` string MUST be used only when no message is extractable.

#### Scenario: Backend 400 validation message is toasted

- **WHEN** the user submits the Persona form and the API responds with HTTP 400
  carrying a parseable validation message (e.g. blank identification)
- **THEN** a toast shows that backend message (or an equivalent user-visible
  form-level validation text matching Dedup-EDGE) and MUST NOT show only the
  generic save-error copy when a message was extractable

#### Scenario: Fallback when no message is extractable

- **WHEN** the save fails with an `ApiError` (non-409) or other error and
  `extractApiError` returns null
- **THEN** the UI shows the generic `errorSave` toast

### Requirement: HTTP 409 duplicate path stays localized

Duplicate-document conflicts MUST continue to use the localized
`duplicateDocument` toast (with optional view-existing action when an existing
person id is known), not raw English API text substituted via
`extractApiError`.

#### Scenario: 409 keeps curated duplicate toast

- **WHEN** persona create/update fails with HTTP 409 duplicate identification
- **THEN** the UI shows the localized duplicate-document message (and
  view-existing action when applicable), not a generic save error and not an
  unlocalized raw API string as the primary copy

### Requirement: Validation failure keeps the dialog open

On validation failure for create/edit, the Persona modal MUST remain open so
the user can correct fields.

#### Scenario: Dialog stays open after empty-DNI validation failure

- **WHEN** the user opens Nueva Persona, fills name fields, leaves DNI empty,
  and submits
- **THEN** validation feedback is visible (inline or toast per Dedup-EDGE) and
  the dialog remains visible

### Requirement: Dedup-EDGE E2E passes

The existing Playwright case `Dedup-EDGE` in
`TS-0015-personas-clientes-workflow.spec.ts` MUST pass on the change’s PR (and
remain passable on `main` after merge).

#### Scenario: Dedup-EDGE passes in the Playwright suite

- **WHEN** `npx playwright test` runs the TS-0015 suite including `Dedup-EDGE`
- **THEN** that test concludes successfully without being skipped or weakened
  below its validation-feedback assertions
