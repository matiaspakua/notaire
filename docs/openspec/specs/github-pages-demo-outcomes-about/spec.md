# github-pages-demo-outcomes-about Specification

## Purpose
TBD - created by archiving change feat-1985-pages-demo-metrics-about. Update Purpose after archive.

## Requirements

### Requirement: Read-only demo of the real frontend

The Pages site SHALL serve the real Notaire frontend at `/demo/`, answering its API calls from recorded fixtures.
Every demo page SHALL show a banner reading "Demo – sample data, read-only". POST, PUT, PATCH and DELETE requests
SHALL be refused without any network request.

#### Scenario: Visitor opens the demo

- **WHEN** a visitor opens `/notaire/demo/` and signs in with any user name and password
- **THEN** the dashboard shows the seeded sample data and the read-only banner

#### Scenario: Visitor tries to save a change

- **WHEN** the app sends a POST, PUT, PATCH or DELETE to `/api/**`
- **THEN** the shim answers 403 "Demo is read-only" and no request leaves the browser

#### Scenario: The app asks for data that was not recorded

- **WHEN** the app sends a GET that has no fixture
- **THEN** the shim answers 404 and never 401, so the visitor is not sent out of the demo

### Requirement: Fixtures are reproducible and hold no secrets

The fixtures SHALL be produced by a documented script from the seeded local stack and SHALL contain no tokens,
passwords or hashes.

#### Scenario: Fixtures are checked

- **WHEN** the tests read `github-page/demo/fixtures.json`
- **THEN** no value looks like a JWT and every secret-looking key holds `"demo"`

### Requirement: Delivery outcomes come from GitHub with their window and method

The site SHALL show delivery outcomes only from `lib/delivery-metrics.json`, with the date range and the method. A
metric the GitHub data cannot support SHALL be shown as unavailable with its reason, never as a number.

#### Scenario: Reader opens the results page

- **WHEN** a reader opens `/ai-sdlc/results/`
- **THEN** the outcomes section shows the snapshot's lead time, throughput, change-failure proxy, issues closed and
  releases, the window dates and how each value is computed

#### Scenario: A metric is not derivable

- **WHEN** the snapshot marks a metric unavailable
- **THEN** the page prints "not available" and the reason instead of a value

### Requirement: About uses sourced facts only

The About section SHALL render only facts that name a source. Placeholders SHALL be kept in `lib/about.json` and
never rendered.

#### Scenario: Reader opens the About page

- **WHEN** a reader opens `/about/`
- **THEN** every fact links or names its source and no placeholder text appears
