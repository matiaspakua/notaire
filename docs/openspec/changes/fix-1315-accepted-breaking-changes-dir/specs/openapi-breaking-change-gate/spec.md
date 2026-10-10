# openapi-breaking-change-gate — delta

## Purpose

OpenAPI breaking changes fail CI unless accepted by the Owner; accepted entries do not outlive their pull request, and accepting one does not conflict with other open pull requests.

## MODIFIED Requirements

### Requirement: Accepted breaks are listed per pull request

Each pull request SHALL list its accepted breaks in its own `backend-api/openapi/accepted-breaking-changes.d/<issue>-<slug>.txt`, each entry under a `# #<issue>` comment; the single `accepted-breaking-changes.txt` SHALL NOT exist.

#### Scenario: Empty directory

- **WHEN** no `*.txt` file exists
- **THEN** the check passes and the ignore list is empty

#### Scenario: Live entry of the pull request

- **WHEN** a pull request adds a file whose entry matches a current breaking change
- **THEN** the check passes and the entry is written to the ignore list

#### Scenario: Stale or malformed entry

- **WHEN** an entry in a pull request's file matches no current break, lacks an issue comment, or the file name does not start with the issue number
- **THEN** the check fails and names the file and entry

#### Scenario: Legacy list

- **WHEN** `accepted-breaking-changes.txt` exists
- **THEN** the check fails with a migration hint

### Requirement: Merged files are never ignored

A file unchanged on the base ref SHALL NOT be written to the ignore list; it SHALL be reported for deletion and deleted with `--prune`.

#### Scenario: Merged file

- **WHEN** a file exists unchanged on the base ref, even if the revision re-introduces the same break
- **THEN** the check passes, the entry is not ignored and the file is listed as merged

#### Scenario: Prune

- **WHEN** the check runs with `--prune`
- **THEN** merged files are deleted and the pull request's files are kept

### Requirement: Same gate locally and in CI

`openapi-contract.yml` SHALL run the checker before the breaking diff and pass only its assembled list to `--err-ignore`; `preflight.sh` SHALL do the same and prune only with `--fix`.

#### Scenario: Wiring

- **WHEN** the workflow and preflight are inspected
- **THEN** both assemble with `workspace/sdlc/check-accepted-breaking-changes.py --write-ignore` and diff with that file
