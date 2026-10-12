## Purpose

Places every Notaire issue on a dated delivery train or a history era, and tells
agents which work they may pick in parallel.

## ADDED Requirements

### Requirement: Open issues sit on a scheduled train
The scheduling policy MUST assign every open issue to exactly one of the six
scheduled milestones: release blockers, frontend product, quality and traceability,
platform and operability, legacy retirement, or backlog.

#### Scenario: Security non-functional requirement is a release blocker
- **WHEN** an open issue is RNF-12 (security and privacy)
- **THEN** the policy assigns the release-blockers milestone

#### Scenario: Functional requirement is frontend product scope
- **WHEN** an open issue is a functional requirement or a use case
- **THEN** the policy assigns the frontend-product milestone

#### Scenario: Critical legacy defect is pulled forward
- **WHEN** an open issue is priority critical, including a jpa defect
- **THEN** the policy assigns the release-blockers milestone rather than legacy retirement

#### Scenario: Low priority does not enter an earlier train
- **WHEN** an open issue is priority low
- **THEN** the policy assigns the backlog milestone

### Requirement: Closed issues without a milestone join a history era
The policy MUST leave a closed issue on its current milestone and MUST group a
closed issue that has none by its close date.

#### Scenario: Close date selects the era
- **WHEN** a closed issue has no milestone and was closed before 2026-07-01
- **THEN** the policy assigns the 2026 Q2 history milestone
- **WHEN** it was closed from 2026-07-01 through 2026-09-30
- **THEN** the policy assigns the 2026 Q3 history milestone
- **WHEN** it was closed on or after 2026-10-01
- **THEN** the policy assigns the 2026 Q4 history milestone

### Requirement: Agents pick one lane at a time
Catalog anchors and epics MUST NOT be pickable. A cross-lane issue MUST block
the frontend and backend lanes. A security lane MUST block the backend lane.

#### Scenario: Requirements stay off the pick queue
- **WHEN** an issue has a requirements or use-case label and no work-type label
- **THEN** its lane is catalog even if it also has area labels

#### Scenario: Cross work blocks both product lanes
- **WHEN** an in-progress issue is on the cross lane
- **THEN** the lanes it blocks are cross, frontend, and backend
