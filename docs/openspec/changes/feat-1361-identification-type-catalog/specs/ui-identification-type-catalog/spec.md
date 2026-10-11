# ui-identification-type-catalog — delta

## Purpose

ADMIN CRUD UI for identification types (DNI, CUIT, passport, …) so RF-53 base tables are manageable in-app and #1250 bucket-C allowlist entries for this catalog can be removed.

## ADDED Requirements

### Requirement: Admin identification type catalog

The system SHALL expose an ADMIN-only page at `/dashboard/administration/identification-types` that lists identification types and allows create, edit and delete against `GET/POST/PUT/DELETE /api/v1/identification-types`, linked from the administration index; a non-admin SHALL not remain on the page; delete conflicts SHALL show the backend reason.

#### Scenario: Admin lists identification types

- **WHEN** an ADMIN opens `/dashboard/administration/identification-types`
- **THEN** the table shows types from `GET /api/v1/identification-types` including seeded DNI

#### Scenario: Admin creates an identification type

- **WHEN** an ADMIN creates a type with a unique name (and optional characters)
- **THEN** `POST /api/v1/identification-types` succeeds and the new row appears in the table

#### Scenario: Admin edits an identification type

- **WHEN** an ADMIN edits a type they created and saves a new name
- **THEN** `PUT /api/v1/identification-types/{id}` succeeds and the table shows the new name

#### Scenario: Admin deletes an unused identification type

- **WHEN** an ADMIN deletes a type that no person references
- **THEN** `DELETE /api/v1/identification-types/{id}` succeeds and the row disappears

#### Scenario: Delete in use shows a reason

- **WHEN** delete fails because the type is still referenced (API 409)
- **THEN** the UI shows the backend conflict reason (or the shared delete-error toast from #1345)

#### Scenario: Non-admin cannot open the catalog

- **WHEN** a non-admin authenticated user opens `/dashboard/administration/identification-types`
- **THEN** they are redirected to `/dashboard?forbidden=1` with the access-denied message and no create control
