## ADDED Requirements

### Requirement: Pages deploys in its own job

`deploy-github-page.yml` SHALL upload the Pages artifact in a `build` job and deploy it in a `deploy` job that needs
`build` and owns the `github-pages` environment.

#### Scenario: a push to main

- **WHEN** a commit lands on `main`
- **THEN** the `deploy` job starts after `build` uploaded the artifact and deploys it on the first attempt
