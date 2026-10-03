<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Define how the Notaire web client presents backend mutation failures so users
see business-rule messages (and field errors when available) instead of generic
toasts on CRUD screens. Source exemplars: CU15 – Procesar pago; CU20/CU21
usuarios; surface area includes CU26–CU30 tablas base pages listed in #1054.

## ADDED Requirements

### Requirement: Shared mutation error presentation

The client SHALL provide a single shared handler that maps an `ApiError` (or
compatible thrown error) into a user-visible message for mutation failures,
covering all HTTP statuses that carry a parseable business body. Authenticated
session expiry (HTTP 401) remains owned by the session-expiry path (#1053) and
MUST NOT be presented as a generic mutation toast.

#### Scenario: Business validation message is toasted

- **WHEN** a mutation fails with an `ApiError` whose body includes a JSON
  `message` (or legacy `error`) field
- **THEN** the user sees that server text in the error toast (not only a
  generic page fallback string)

#### Scenario: Fallback when body has no message

- **WHEN** a mutation fails and the error body has no parseable business message
- **THEN** the shared handler shows the caller-supplied fallback string

#### Scenario: Authenticated 401 is not a mutation toast

- **WHEN** an authenticated mutation receives HTTP 401
- **THEN** the session-expiry handler runs (logout + `/login?expired=1`) and the
  mutation handler does not replace that with a generic save/delete toast

### Requirement: apiDelete throws ApiError

`apiDelete` MUST reject non-OK responses with `ApiError` (same contract as other
verbs) so callers and extractors can read status and body.

#### Scenario: Delete failure is an ApiError

- **WHEN** `apiDelete` receives a non-OK HTTP response
- **THEN** the promise rejects with an `ApiError` instance carrying status and body

### Requirement: Listed CRUD pages use the shared handler

All fifteen pages identified in #1054 MUST use the shared mutation error handler
for save/delete (and equivalent) mutation catch paths.

#### Scenario: Listed page surfaces server message

- **WHEN** a user triggers a failing mutation on any of the fifteen listed pages
  and the API returns a parseable business message
- **THEN** that page shows the server message via the shared handler

### Requirement: Inline field errors when field detail is present

When the API error message encodes field-level detail that maps to a control on
the active form, the client SHALL set that field’s `FormField` error (and
expose invalid state via `aria-invalid` or equivalent) in addition to or instead
of a generic-only toast.

#### Scenario: Field detail maps to FormField error

- **WHEN** a mutation fails with a message that identifies a form field (for
  example bean-validation style `nombre: must not be blank`) and that field is
  present on the form
- **THEN** the matching `FormField` shows the error text and the control is
  marked invalid for assistive tech

#### Scenario: Unmapped field detail still toasts

- **WHEN** a mutation fails with field-like detail that does not match any
  control on the active form
- **THEN** the user still sees the full server message in a toast
