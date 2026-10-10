# Design

## Context

#1054 surfaced backend messages for create and update; delete was left behind. 409 bodies are either JSON `ErrorResponse` or plain text (PersonController); `ManagementController` returns 409 with no body.

## Goals / Non-Goals

Goal: every delete failure says why. Non-goals: disabling the confirm button from a pre-check inside the dialog (confirm-dialog UX issue), and links such as "Ver gestiones".

## Decisions

The translated in-use text is the title, so the toast is always in the active locale; the server reason, often English, is the description. Plain-text bodies count only when short and not HTML or JSON. Workflow node and transition deletes keep their curated fallbacks, which already explain the 409.

## Riesgos / Trade-offs

A 409 that isn't about references would also read as "in use". All current delete 409s are reference conflicts.

## Testing Strategy

`delete-error.test.ts` (helper missing, keys missing, 20 handlers toasting the generic error, so red) and `TS-0105` (no warning toast) written first.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

`TS-0105`: a stubbed 409 on deleting a person shows the in-use warning with the server reason and not the generic error; a 404 says the record no longer exists.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
