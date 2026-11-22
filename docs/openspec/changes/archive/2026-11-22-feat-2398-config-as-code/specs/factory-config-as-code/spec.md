## ADDED Requirements

### Requirement: Config file tree listing

The kit SHALL expose `GET /factory/config/files` returning a JSON array of rows, each
describing a config root file or directory with `path` (relative to repo root), `kind`
(one of `profile`, `catalog`, `workflow`, `project`, `policy`, `context`, `skill`),
`size` (bytes), and `media_type` (derived from extension: `application/toml`,
`application/json`, `text/yaml`, `text/markdown`, `text/plain`). The response SHALL
require Bearer `read` scope.

#### Scenario: tree listing returns config roots

- **WHEN** a client sends `GET /factory/config/files` with a valid Bearer read token
- **THEN** the response is a JSON array containing rows for the profile file, catalogs
  directory, workflows, projects directory, policy file, context files, and skills directory

#### Scenario: tree listing requires read scope

- **WHEN** a client sends `GET /factory/config/files` without a Bearer token
- **THEN** the response is 401 problem+json

### Requirement: Config file read with redaction

The kit SHALL expose `GET /factory/config/files/{path}` returning JSON
`{path, kind, media_type, size, content, redacted}` where `content` is the file text
with secrets redacted. The `redacted` boolean SHALL be true when any secret pattern was
found and replaced. The response SHALL require Bearer `read` scope.

#### Scenario: file read returns redacted content

- **WHEN** a client requests `GET /factory/config/files/.aisdlc/aisdlc.toml` with a
  valid Bearer read token and the file contains a value matching `SECRET_VALUES`
- **THEN** the response contains the file text with the secret replaced by `***` and
  `redacted` is true

#### Scenario: file read with no secrets

- **WHEN** a client requests a config file that contains no secret patterns
- **THEN** the content is returned verbatim and `redacted` is false

#### Scenario: file read requires read scope

- **WHEN** a client sends `GET /factory/config/files/{path}` without a Bearer token
- **THEN** the response is 401 problem+json

### Requirement: Path traversal refusal

The kit SHALL refuse any config file path that attempts directory traversal. Paths
containing `..`, absolute paths (starting with `/`), encoded separators (`%2f`, `%5c`),
or leading dots SHALL receive HTTP 400 with code `invalid_id`.

#### Scenario: traversal with .. is refused

- **WHEN** a client requests `GET /factory/config/files/..%2F..%2Fetc%2Fpasswd`
- **THEN** the response is 400 problem+json with code `invalid_id`

#### Scenario: absolute path is refused

- **WHEN** a client requests `GET /factory/config/files/etc/passwd`
- **THEN** the response is 400 problem+json with code `invalid_id`

#### Scenario: encoded separator %2f is refused

- **WHEN** a client requests `GET /factory/config/files/.aisdlc%2F..%2Fetc`
- **THEN** the response is 400 problem+json with code `invalid_id`

#### Scenario: encoded separator %5c is refused

- **WHEN** a client requests `GET /factory/config/files/.aisdlc%5C..%5Cetc`
- **THEN** the response is 400 problem+json with code `invalid_id`

### Requirement: Symlink escape refusal

The kit SHALL never follow a symlink that resolves outside the repository root. When a
config file path contains a symlink whose target escapes the repo, the request SHALL
receive HTTP 400 with code `invalid_id`.

#### Scenario: symlink escape is refused

- **WHEN** a config root contains a symlink pointing outside the repository root and a
  client requests that path
- **THEN** the response is 400 problem+json with code `invalid_id`

### Requirement: Catalog delete with optimistic concurrency

The kit SHALL expose `DELETE /factory/catalogs/{kind}/{id}` that removes a catalog entry
with optimistic concurrency control. The request SHALL require Bearer `write` scope,
`X-AISDLC-CSRF` matching the bearer token, and `If-Match` header containing the expected
version number. If `If-Match` is missing, the response SHALL be 428. If the version does
not match the current version, the response SHALL be 409 with code `version_conflict`.
On success, the entry is removed and an evidence record is appended (kind `catalog.delete`,
containing actor, kind, id, version, and digest).

#### Scenario: delete with matching If-Match succeeds

- **WHEN** a client sends `DELETE /factory/catalogs/skills/my-skill` with valid Bearer
  write scope, matching CSRF, and `If-Match: 3` matching the current version
- **THEN** the entry is removed, the response is 200, and an evidence record of kind
  `catalog.delete` is appended

#### Scenario: delete with stale If-Match returns 409

- **WHEN** a client sends `DELETE /factory/catalogs/skills/my-skill` with `If-Match: 2`
  but the current version is 3
- **THEN** the response is 409 problem+json with code `version_conflict` and the entry
  is not removed

#### Scenario: delete without If-Match returns 428

- **WHEN** a client sends `DELETE /factory/catalogs/skills/my-skill` without an
  `If-Match` header
- **THEN** the response is 428 problem+json with code `if_match_required`

#### Scenario: delete requires write scope

- **WHEN** a client sends `DELETE /factory/catalogs/skills/my-skill` with only `read`
  scope
- **THEN** the response is 403 problem+json with code `forbidden`

#### Scenario: delete requires CSRF

- **WHEN** a client sends `DELETE /factory/catalogs/skills/my-skill` with write scope
  but without `X-AISDLC-CSRF` header
- **THEN** the response is 403 problem+json with code `csrf_required`

#### Scenario: delete unknown entry returns 404

- **WHEN** a client sends `DELETE /factory/catalogs/skills/nonexistent` with valid auth
  and If-Match
- **THEN** the response is 404 problem+json with code `not_found`

#### Scenario: delete appends evidence record

- **WHEN** a catalog delete succeeds
- **THEN** the evidence ledger contains a new record with kind `catalog.delete`, the
  actor, the catalog kind, the entry id, the version that was deleted, and its digest

### Requirement: CLI parity for config files and catalog delete

The kit SHALL provide `aisdlc config files [--json]` listing the config tree and
`aisdlc config show <path>` showing a config file's redacted text. The existing
`aisdlc catalog remove` command SHALL accept an optional `--if-match <version>` flag
for optimistic concurrency parity with the HTTP endpoint.

#### Scenario: CLI config files lists tree

- **WHEN** the user runs `aisdlc config files --json`
- **THEN** the output is a JSON array of config root rows matching the HTTP response

#### Scenario: CLI config show displays file

- **WHEN** the user runs `aisdlc config show .aisdlc/aisdlc.toml`
- **THEN** the output is the file text with secrets redacted

#### Scenario: CLI catalog remove with --if-match

- **WHEN** the user runs `aisdlc catalog remove skills my-skill --if-match 3` and the
  current version is 3
- **THEN** the entry is removed and an evidence record is appended
