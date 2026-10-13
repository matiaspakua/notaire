# Design

## Context

Issue #579 reports that controllers bypass `GlobalExceptionHandler` with ad-hoc `catch (Exception)` blocks, giving an inconsistent error contract. A live probe on main (empty JSON body to every POST/PUT) showed that 11 of those controllers also return the raw exception text, including PostgreSQL row data. This slice removes the disclosure without changing status codes; aligning the status codes themselves (409 vs 400 vs 500) is the rest of #579.

## Goals / Non-Goals

Goal: no exception text reaches the client from these controllers, one error shape. Non-goals: changing status codes, removing the try/catch blocks (rest of #579), bean validation of the request bodies (#655).

## Decisions

Keep each site's status code: tests, Bruno and the documented 409 responses rely on them, and the global handler would turn some into 400, a contract change that belongs to the rest of #579. Keep `NotaireException` messages: they are written for users and some services use them for business rules. Log the full cause at WARN (409) or ERROR (500) so nothing is lost for operators.

## Riesgos / Trade-offs

A client that parsed the raw text loses it; the frontend never could (it only reads JSON `message`/`error`), and the workflow screens now get a readable `message`.

## Testing Strategy

`ControllerExceptionMessageLeakTest` (6) and `ErrorResponsesTest` (4) committed first; the controller test was observed failing 6/6, and the guard listed the 23 sites. The existing controller unit tests and the referential-integrity integration tests pass unchanged.

## Regression Strategy

`backend-api/verify.sh` (full suite and coverage), full Bruno run, oasdiff against main.

## Playwright Strategy

No UI change.

## Deployment Strategy

Backend only; no configuration or migration.

## Rollback Strategy

Revert the merge commit.
