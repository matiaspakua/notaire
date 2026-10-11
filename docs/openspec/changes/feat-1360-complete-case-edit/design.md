# Design

## Context

`PUT /managements/{id}/complete-case` requires number, budgetId, notaryId, statusManagementId and typeProcedureId, rejects a status change (CU83), replaces `notes` with what is sent (adding a redirect note when a substitution applies) and defaults a missing `heading` to "Management". The list rows (`DtoManagementSummary`) carry none of the ids.

## Goals / Non-Goals

Goals: full edit from the list, prefilled; no data lost on save; a toast when the save redirects to a substitute. Non-goals: editing header/notes in the dialog, clearing the property, retiring `PUT /managements/{id}`.

## Decisions

- A dedicated read `GET /{id}/complete-case` in the PUT's own shape instead of widening `DtoManagementSummary` (used by the paged list and several screens) or chaining four reads in the UI.
- The read takes the first procedure, exactly as the PUT updates the first one, so what is shown is what is saved.
- The UI sends `heading` and `notes` back from the read, so the PUT does not reset them.
- The status select is shown but disabled in edit mode, with a hint pointing to "Cambiar estado".
- Prefill happens once per opened management by adjusting state during render (React's recommended alternative to setting state in an effect), so a background refetch never overwrites the user's edits. Save is disabled until the prefill is in.
- Redirect toast on edit: only when the response notes differ from the notes sent, because an old redirect note stays in `notes` and would otherwise trigger the toast on every later edit.

## Riesgos / Trade-offs

`PUT /managements/{id}` loses its only UI consumer; it stays (allowlisted bucket D) for API clients. Managements with several procedures still edit only the first one, as before.

## Testing Strategy

JUnit `ManagementCompleteCaseReadIntegrationTest` (values returned, 404, notary change reads back with notes kept); Vitest `useManagements.complete-case.test.tsx` (GET path, disabled without id, PUT body names and invalidation). Both written first (red commits).

## Regression Strategy

`bash backend-api/verify.sh`, `bash frontend/verify.sh`, full Bruno collection on the branch jar, chromium suite.

## Playwright Strategy

TS-0126: the edit dialog prefills number, notary, budget and procedure type and disables the status; changing the notary persists and keeps the notes; editing to a notary with an active substitution shows the redirect toast and stores the substitute. Observed failing on the pre-change build.

## Deployment Strategy

Backend and frontend ship together; no migration.

## Rollback Strategy

Revert the merge commit.
