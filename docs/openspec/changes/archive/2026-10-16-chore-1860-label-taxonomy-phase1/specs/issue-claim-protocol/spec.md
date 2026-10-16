## ADDED Requirements

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
