# Design

## Context

`IdentificationTypeController` was a thin CRUD over `IdentificationTypeRepository`. `DELETE` called `deleteById` inside a `try` that mapped any exception to 409, but `IdentificationType.personList` was `@OneToMany(cascade = CascadeType.ALL)`, so Hibernate removed the people first and nothing failed. On H2 the red test saw 204 and a deleted person. The person form's DNI default is found by name (slice 1), and nothing stopped a second "dni" row.

## Goals / Non-Goals

Goals: an admin page with the estados-gestion pattern; deleting a used type is refused and never touches people; duplicate names are refused. Non-goals: validating `characters` in the API, enforcing `maxLength` in the person form, a version check on PUT.

## Decisions

- The in-use check (`existsBy…`, no list load) runs before the delete `try` and throws `NotaireException(409)`, so `GlobalExceptionHandler` returns an `ErrorResponse` with the reason and the handler signatures stay typed (no new loose returns for `ControllerSignatureArchitectureTest`).
- The cascade is removed as well, so a future code path that removes a type cannot delete people; the in-use check is the user-facing rule.
- Name uniqueness is checked in the controller over the (5-row) catalog with `trim().equalsIgnoreCase`, excluding the row being edited. No DB unique index: legacy rows are not guaranteed clean and a migration is not needed for the rule.
- The page shows the API's in-use reason through `useDeleteError` (#1345) and a localized "name exists" text for a 409 on save, instead of the English server text.
- `characters` is required in the form (1–99) because the PostgreSQL column is NOT NULL.

## Riesgos / Trade-offs

A client that deleted a used type now gets 409 instead of silently losing people; that is the intent. The uniqueness check is not race-proof under concurrent admins (acceptable for a 5-row admin table).

## Testing Strategy

JUnit `IdentificationTypeCatalogIntegrationTest` (in-use delete 409 keeps type and person, unused delete 204, duplicate name in another case 409, rename to another's name 409 and keeping its own name 200) and `SimpleControllersTest` 409 paths; Vitest `identification-types-admin.test.tsx` (list, create trimmed, required fields, duplicate → localized error, edit PUT, delete, in-use toast, admin index link). Written first (red commits).

## Regression Strategy

`bash backend-api/verify.sh`, `bash frontend/verify.sh`, full Bruno collection on the branch jar, full chromium suite.

## Playwright Strategy

TS-0139: index link; create, edit length, delete; in-use delete shows the in-use toast, the row and the person stay; " dni " is rejected and the catalog keeps one DNI; EMPLEADO is redirected from the page. Four of five observed failing on the pre-change build (the redirect already held).

## Deployment Strategy

Backend and frontend ship together; no migration.

## Rollback Strategy

Revert the merge commit.
