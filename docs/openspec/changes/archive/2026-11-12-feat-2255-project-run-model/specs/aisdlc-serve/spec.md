## ADDED Requirements

### Requirement: Serve hosts project and run paths honestly

`aisdlc serve` SHALL expose `GET/POST /factory/projects` and
`GET/POST /factory/runs` (plus `GET /factory/runs/{id}` and
`GET /factory/runs/{id}/events`). POST SHALL require `--write`, CSRF, and write
scope. `POST /factory/runs/{id}/cancel` SHALL return HTTP 501 with a problem
detail naming the missing runner capability while `runner` is false.

#### Scenario: HTTP GET/POST projects and runs

- **WHEN** serve is started with `--write` and a valid write token + CSRF
- **THEN** POST/GET projects and runs succeed against the service layer

#### Scenario: cancel returns 501 with capability reason

- **WHEN** a client POSTs `/factory/runs/{id}/cancel` while capabilities.runner is false
- **THEN** the response is 501 with a problem code and detail explaining the runner gap
