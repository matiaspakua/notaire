## MODIFIED Requirements

### Requirement: The Pages app is checked on every pull request

A change that touches `github-page/` SHALL be checked by `bash workspace/sdlc/preflight.sh` (typecheck, lint, unit tests, production build, internal link check; `--full` adds the Playwright smoke suite) before the pull request is opened. `.github/workflows/pages-ci.yml` SHALL run those same checks on a version tag (`v*` / `notaire-v*`) and on `workflow_dispatch`, and SHALL NOT run them on `pull_request`. The workflow SHALL NOT deploy.

#### Scenario: A broken internal link

- **WHEN** a page links a route or `#anchor` that the static export does not contain
- **THEN** `npm run check:links` fails the Pages check in `pages-ci.yml` and in `preflight.sh`

#### Scenario: Pages CI is not a pull-request workflow

- **WHEN** `.github/workflows/pages-ci.yml` triggers are read
- **THEN** `pull_request` is absent, `push.tags` includes `v*` and `notaire-v*`, and the workflow does not grant `pages: write`
