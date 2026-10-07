# frontend-eslint-blocking Specification

## Purpose

Make frontend ESLint a hard quality gate in CI (and keep local preflight
aligned) now that #701 is closed, including enabled `jsx-a11y` rules.
Source: #1048; owner CU76 – Quality Assurance and Testing Infrastructure.

## Requirements

### Requirement: Frontend CI ESLint is blocking

The Frontend CI workflow SHALL run `npm run lint` (or equivalent
`eslint src --max-warnings=0`) such that any ESLint error or warning fails the
job. The ESLint step MUST NOT set `continue-on-error: true`. Comments that
justify advisory lint “until #701” MUST be removed or rewritten to reflect the
closed issue.

#### Scenario: ESLint step fails the job on lint errors

- **WHEN** `npm run lint` exits non-zero during the Frontend CI TypeScript Check
  job
- **THEN** the ESLint step and job fail (the workflow does not continue as if
  lint succeeded)

#### Scenario: Clean tree passes blocking ESLint

- **WHEN** the frontend sources comply with the project ESLint config
- **THEN** `npm run lint` exits 0 and the Frontend CI ESLint step succeeds

#### Scenario: Obsolete #701 continue-on-error comment removed

- **WHEN** a reviewer inspects `.github/workflows/frontend-ci.yml` ESLint step
- **THEN** there is no `continue-on-error: true` on that step and no comment
  stating lint is report-only until #701

### Requirement: Preflight mirrors blocking ESLint

`workspace/sdlc/preflight.sh` MUST document and execute frontend ESLint as a blocking
gate consistent with CI (same max-warnings / lint script semantics).

#### Scenario: Preflight maps ESLint as blocking in CI

- **WHEN** a developer reads the preflight local→CI mapping for frontend eslint
- **THEN** it states that Frontend CI ESLint is blocking (not advisory / not
  “until #701”)

#### Scenario: Preflight and CI share max-warnings=0 semantics

- **WHEN** preflight runs frontend eslint and CI runs `npm run lint`
- **THEN** both enforce zero warnings (`--max-warnings=0` or the package `lint`
  script that embeds that flag)

### Requirement: jsx-a11y rules are enabled under the blocking gate

The frontend ESLint configuration MUST enable `jsx-a11y` rules (via
`eslint-config-next` core-web-vitals and/or explicit rules) so accessibility
violations fail the same blocking lint gate.

#### Scenario: jsx-a11y rules are enabled

- **WHEN** `frontend/eslint.config.mjs` is evaluated
- **THEN** jsx-a11y rules from the Next.js ESLint preset (and any project
  tightenings) are active for `src/**`

#### Scenario: a11y violation fails blocking lint

- **WHEN** source under `frontend/src` contains a jsx-a11y violation covered by
  the enabled rules
- **THEN** `npm run lint` reports the violation and exits non-zero
