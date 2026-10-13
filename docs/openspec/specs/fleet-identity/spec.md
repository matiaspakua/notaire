# fleet-identity Specification

## Purpose
Fleet agents act as a least-privilege GitHub App so they can write labels and board status without human handoffs.
## Requirements
### Requirement: Fleet agents mint a GitHub App installation token

`workspace/sdlc/fleet-token.sh` SHALL sign an RS256 App JWT (lifetime at most 10 minutes) from `FLEET_APP_ID` and `FLEET_APP_PRIVATE_KEY`, exchange it for an installation token for the repository, and print an `export GH_TOKEN=…` line. Without those variables, it SHALL print nothing on stdout, warn on stderr, and exit 0.

#### Scenario: JWT is signed by the App key

- **WHEN** the script runs with `--jwt-only` and App credentials
- **THEN** it prints an RS256 JWT whose `iss` is the App id, whose lifetime is at most 600 s, and whose signature verifies with the App public key

#### Scenario: Without App credentials it falls back loudly

- **WHEN** the variables are absent
- **THEN** stdout is empty, stderr says the fleet App is not configured, and the exit code is 0

