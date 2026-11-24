## ADDED Requirements

### Requirement: Releases are cut only by the Owner

`release-please.yml` SHALL run only on `workflow_dispatch`. A push to `main` SHALL NOT open a release pull request or
create a tag. The runbook SHALL describe the manual cut: dispatch, merge the release PR, dispatch again.

#### Scenario: a push to main

- **WHEN** a commit lands on `main`
- **THEN** no Release Please run starts and no bot release PR is opened

#### Scenario: the Owner cuts a release

- **WHEN** the Owner dispatches Release Please, merges its PR and dispatches it again
- **THEN** the `vX.Y.Z` tag and GitHub Release exist and the tag workflows are dispatched
