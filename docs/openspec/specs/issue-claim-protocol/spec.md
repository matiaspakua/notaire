# issue-claim-protocol Specification

## Purpose
Two fleet agents never own the same issue or conflicting lanes, and a dead agent cannot hold a lane forever.

## Requirements

### Requirement: Claims are decided by the earliest marker

`workspace/sdlc/claim-issue.py` SHALL refuse catalog anchors, roadmap epics, `blocked` and `needs-info` issues, and issues whose derived lane conflicts with an open `in-progress` issue. Otherwise it SHALL post a claim marker, re-read the comments after a settle delay, and grant the claim only when its marker is the earliest since the last release. A losing agent SHALL withdraw its marker and exit non-zero.

#### Scenario: Uncontested claim wins and marks the board

- **WHEN** one agent claims a free issue
- **THEN** it exits 0, the issue gets `in-progress`, and Status becomes In Progress (or a handoff line is printed)

#### Scenario: Losing a race withdraws and fails

- **WHEN** another marker is earlier than this agent's
- **THEN** this agent deletes its marker, adds no label, and exits 1

#### Scenario: Busy lane refuses before posting

- **WHEN** an open `in-progress` issue holds a conflicting lane
- **THEN** no marker is posted and the script exits 1

### Requirement: Stale claims are released

`claim-issue.py --release-stale --hours N` SHALL remove `in-progress`, set Status to Todo and post a release marker on every open in-progress issue whose active claim is older than N hours and that has no open pull request.

#### Scenario: Stale claim without open PR

- **WHEN** the claim is older than N hours and no PR is open
- **THEN** it is released. A claim with an open PR, or a younger claim, is kept

### Requirement: Claims require readiness and read both label names

`claim-issue.py` SHALL refuse an issue that carries neither `status:ready` nor its legacy name `ready-for-dev`, and SHALL treat `status:blocked` / `status:needs-info` like `blocked` / `needs-info`. It SHALL write `status:in-progress` once the repository has that label and `in-progress` before, and SHALL read and release both names (#1860).

#### Scenario: Unready issue is refused

- **WHEN** an agent claims an open issue with no `status:ready` or `ready-for-dev` label
- **THEN** no marker is posted and the script exits 1 naming readiness

#### Scenario: Either ready name is accepted

- **WHEN** the issue carries `ready-for-dev`, or `status:ready` with `area:backend`
- **THEN** the claim proceeds as before

#### Scenario: New hold names refuse

- **WHEN** the issue carries `status:blocked` or `status:needs-info`
- **THEN** the claim is refused

#### Scenario: Written WIP label follows the repository

- **WHEN** the repository has `status:in-progress`
- **THEN** the claim writes `status:in-progress`, otherwise `in-progress`
