## ADDED Requirements

### Requirement: Spanish pages carry no English prose

Every translated route SHALL read in Spanish without English prose, apart from quoted Issue titles and real command
output. Text nodes and the `aria-label`, `title`, `alt` and `placeholder` attributes under `#main` SHALL be localised,
including content rendered after the page mounts.

#### Scenario: a route read in Spanish

- **WHEN** `e2e/spanish-coverage.spec.ts` reads a translated route with the Spanish preference
- **THEN** no line of English prose remains

#### Scenario: a table's label and late content

- **GIVEN** a table labelled `Table: Role, Who` and a node added after the first pass
- **WHEN** the page is localised to Spanish
- **THEN** the label reads `Tabla: Rol, Quién` and the late node is translated

### Requirement: Published numbers come from their sources

The Pages metrics SHALL read the coverage floors from `pom.xml` and `vitest.config.ts`, the Owner's target, and the AI
tools configured in the repository from `metrics.json`; copy that counts stations, agents, gates or phases of the
factory line SHALL match its data; the roadmap snapshot SHALL be refreshable without changing GitHub.

#### Scenario: coverage floors

- **WHEN** `generate-github-page-metrics.sh` runs
- **THEN** `coverageFloors` equals the minimums in `pom.xml` and `vitest.config.ts`, and `coverageTarget` is 90

#### Scenario: the factory line copy

- **WHEN** a station is added
- **THEN** `factory-line-counts.test.ts` fails until the copy says the new count

#### Scenario: a read-only roadmap refresh

- **WHEN** `apply_milestone_policy.py --snapshot` runs
- **THEN** both snapshot files are written and nothing is posted to GitHub
