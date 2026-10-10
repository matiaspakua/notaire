# Design

## Context

next-intl is wired everywhere, but some literals were never moved to the catalogs.

## Goals / Non-Goals

Goal: no hardcoded UI strings and a guard against regressions. Non-goals: translating backend data (status names, folio types) or backend error messages.

## Decisions

A regex scan instead of an ESLint plugin (eslint-plugin-i18next is not a dependency); comments are stripped and generics are excluded by only matching text followed by a closing tag or an expression. Spanish catalog values stay identical so existing Spanish locators keep working.

## Riesgos / Trade-offs

The scan can miss literals built with template strings; reviewers still check those.

## Testing Strategy

hardcoded-ui-strings.test.ts (60 offenders), breadcrumb.test.tsx and the TS-0040 #1354 cases failed first (test commits).

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0040 #1354 cases in EN and ES.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
