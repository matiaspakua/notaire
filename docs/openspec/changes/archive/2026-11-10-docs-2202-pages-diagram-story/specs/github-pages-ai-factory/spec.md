## ADDED Requirements

### Requirement: The factory diagram matches the kit on main

The public factory diagram SHALL step one change from a GitHub Issue to a closed Issue. It SHALL say that the pull-request proof is `preflight.sh --full` on the agent machine, that product CI runs on a version tag, that the Foreman may merge a Worker pull request, that the Owner merges protected paths, and that Workers never merge. It SHALL say that `aisdlc foreman plan` does not execute agents and that the loaded plugins are openspec and example-method. It SHALL NOT say that only the Owner merges.

#### Scenario: Merge rule

- **WHEN** a visitor steps to the merge step
- **THEN** the text says Workers never merge and names the Foreman

### Requirement: Lead time is the snapshot, not a histogram

The results page SHALL show the lead-time median and p90 from `delivery-metrics.json`. It SHALL say that those two numbers are not a histogram of every pull request.

#### Scenario: Snapshot without samples

- **WHEN** the delivery snapshot has a median and a p90
- **THEN** the page shows both and says it is not a histogram
