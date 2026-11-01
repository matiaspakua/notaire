## ADDED Requirements

### Requirement: Playwright mobile project matches real mobile specs

The Playwright `mobile` project MUST match existing `*mobile*.spec.ts` files under
`testing/e2e/tests`, MUST NOT reference a missing `mobile-viewport.spec.ts`, and the
`chromium` project MUST exclude those mobile specs. Every named project MUST list at
least one test.

#### Scenario: Mobile project does not reference the missing viewport file
- **GIVEN** `testing/e2e/playwright.config.ts`
- **WHEN** the config is inspected
- **THEN** it does not contain `mobile-viewport.spec.ts` and the mobile `testMatch` is
  `**/*mobile*.spec.ts`

#### Scenario: Chromium excludes mobile specs
- **GIVEN** the chromium project in `playwright.config.ts`
- **WHEN** the config is inspected
- **THEN** it declares a `testIgnore` that includes mobile specs

#### Scenario: Every named project lists at least one test
- **GIVEN** projects smoke, health, chromium and mobile
- **WHEN** `playwright test --list --reporter=list` runs
- **THEN** each project name appears with a count greater than zero
