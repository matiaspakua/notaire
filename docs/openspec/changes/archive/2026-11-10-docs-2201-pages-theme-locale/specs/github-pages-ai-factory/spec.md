## ADDED Requirements

### Requirement: A newcomer can read the factory in order

The AI Factory overview SHALL list a reading order that starts at the overview and ends at the playbook, with each step linking a real factory page. The order SHALL move from what the factory is, through one change and the gates, to the reusable kit.

#### Scenario: The path is complete

- **WHEN** the reading order is read
- **THEN** every step href is a factory page that exists, the first href is `/ai-sdlc/`, and the last href is `/ai-sdlc/playbook/`

### Requirement: The framework page separates shipped behavior from planned work

The framework page SHALL describe kit commands that exist on `main` and SHALL label work that is not done as planned. Plugin ids shown as loaded SHALL be the `allow` list in `.aisdlc/aisdlc.toml`. Planned inventory rows SHALL be read from `.aisdlc/migration-inventory.toml`. The page SHALL NOT say that a missing bubblewrap probe means the process is sandboxed, and SHALL NOT present Spec Kit or AWS AI-DLC as loaded plugins.

#### Scenario: Loaded plugins match the profile

- **WHEN** the site is built
- **THEN** the loaded plugin names equal the allow list in `.aisdlc/aisdlc.toml`

#### Scenario: Planned moves stay planned

- **WHEN** the migration inventory marks `workspace/sdlc` as planned
- **THEN** the framework page lists that path under planned work
