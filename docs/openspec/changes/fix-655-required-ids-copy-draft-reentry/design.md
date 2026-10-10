# Design

## Context

#655 tracks request bodies that are not validated. After #1333/#1334/#1335 and the workflow PR (#1371), the empty-body probe on `main` still shows three 500s outside the open PRs.

## Goals / Non-Goals

Goal: these three endpoints answer 400 naming the missing fields. Non-goals: `PUT /tramites/{id}` and `PUT /tipo-tramite/{id}/workflow` accepting `{}` (next slice), `PUT /testimonio/{id}` with `{}` overwriting the number (next slice), the copy endpoint's empty 500 body on other failures (#579).

## Decisions

- Copy `number` is required too: the dialog labels it required, the column is NOT NULL, and a missing number silently stored 0.
- PUT `/copia/{id}` requires both fields because it replaces both columns (a null print date was a 500).
- The re-entry body is validated before the management lookup, so `{}` answers 400 even for an unknown management.

## Riesgos / Trade-offs

A client clearing the print date in the dialog now cannot save, instead of getting a 500.

## Testing Strategy

`RequiredRequestIdsIntegrationTest` (8 cases) observed failing 8/8 first (500/404 instead of 400; fields not required in the contract). Playwright `copias-required-fields.spec.ts` observed failing first (Save enabled with an empty number).

## Regression Strategy

Full backend verify, frontend tsc/eslint/vitest, testing verify, full Bruno, Playwright TS-0018, TS-0082 and the new copies spec.

## Playwright Strategy

`copias-required-fields.spec.ts`, TS-0018, TS-0082.

## Deployment Strategy

Backend and frontend together.

## Rollback Strategy

Revert the merge commit.
