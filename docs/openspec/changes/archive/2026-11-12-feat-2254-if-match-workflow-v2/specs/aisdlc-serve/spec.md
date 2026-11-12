## ADDED Requirements

### Requirement: Opt-in If-Match writes on serve

When started with `--write`, `aisdlc serve` SHALL accept mutating catalog PUT
requests that present Bearer scope ≥ `write`, header `X-AISDLC-CSRF` matching
the bearer token, and `If-Match` equal to the current entity version. Without
`--write`, mutating methods SHALL continue to answer 405. Owner-override approve
paths SHALL require scope `owner` when exposed.

#### Scenario: PUT with If-Match succeeds under --write

- **WHEN** serve runs with `--write` and a client PUTs a catalog entry with valid
  Bearer write scope, CSRF header, and matching If-Match
- **THEN** a new immutable revision is stored and returned

#### Scenario: PUT without --write remains 405

- **WHEN** serve runs without `--write`
- **THEN** PUT/POST/PATCH/DELETE still receive 405 problem+json

#### Scenario: missing CSRF or insufficient scope is refused

- **WHEN** a mutating request lacks `X-AISDLC-CSRF` or presents only `read` scope
- **THEN** the host responds with problem+json (`forbidden` or
  `csrf_required`) and does not write
