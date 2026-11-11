## ADDED Requirements

### Requirement: Read-only stdlib HTTP host

The kit SHALL provide `aisdlc serve [--port N]` that binds a stdlib HTTP server
(`http.server` / `wsgiref` family only) and serves the factory OpenAPI **GET**
paths from the service layer. Mutating methods SHALL receive HTTP 405 with
`application/problem+json`.

#### Scenario: GET factory paths succeed with bearer token

- **WHEN** `aisdlc serve` runs on an ephemeral port with a valid Bearer token
- **THEN** each declared factory GET path returns 200 JSON assembled from the
  service layer (config, plugins, harness, report, catalogs, workflows,
  capabilities)

#### Scenario: mutating methods are refused

- **WHEN** a client sends POST, PUT, PATCH, or DELETE to a factory path
- **THEN** the host responds 405 with problem+json and a stable `code`

### Requirement: Loopback bind and remote guard

The host SHALL bind `127.0.0.1` by default. A non-loopback bind SHALL be refused
unless `--allow-remote` is set and a serve token is already configured.

#### Scenario: default loopback

- **WHEN** serve starts without an explicit non-loopback host
- **THEN** it listens on `127.0.0.1`

#### Scenario: remote bind refused

- **WHEN** serve is asked to bind a non-loopback address without `--allow-remote`
  or without a configured token
- **THEN** it exits non-zero and does not listen

### Requirement: Bearer token file and scopes

The host SHALL authenticate with `Authorization: Bearer` using a token stored in
`.aisdlc/serve.token` (mode 0600). The file SHALL be created on first successful
serve start if missing. The plaintext token SHALL never appear in logs or error
bodies. Scopes are `read` < `write` < `owner`; this slice requires at least
`read` for GET.

#### Scenario: missing or invalid token yields 401

- **WHEN** a GET lacks a Bearer token or presents an invalid token
- **THEN** the response is 401 problem+json

#### Scenario: token file mode and creation

- **WHEN** serve starts and `.aisdlc/serve.token` is absent
- **THEN** the host creates it with mode 0600 and does not print the secret

### Requirement: Honest capabilities

`GET /factory/capabilities` SHALL report `approvals_live`, `runner`,
`forge_write`, isolator `sandbox`, and `enforces_committer` exactly as the ports
report them, using honest `false` when not proven.

#### Scenario: capabilities honesty

- **WHEN** capabilities is fetched on a default test profile without live ports
- **THEN** `approvals_live`, `runner`, `forge_write`, and `isolator.sandbox` are
  false (or report false equivalents) and `enforces_committer` matches the
  Isolator probe

### Requirement: Safe ids and redaction

Path ids SHALL be validated to block traversal. No response or log line SHALL
contain a plaintext credential.

#### Scenario: traversal id rejected

- **WHEN** a client requests a catalog or workflow id containing `/`, `\\`, or a
  leading `.`
- **THEN** the host responds 400 problem+json with code `invalid_id`

#### Scenario: secret redaction

- **WHEN** responses and serve logs are inspected after requests that could
  include secret-shaped profile values
- **THEN** no plaintext credential appears

### Requirement: Static mount without UI

The host SHALL expose a static asset mount point for future kit UI and SHALL NOT
ship a Phase D SPA in this slice.

#### Scenario: static mount empty

- **WHEN** a client GETs the static mount root
- **THEN** the host responds successfully from the mount (empty or placeholder)
  without shipping a factory SPA
