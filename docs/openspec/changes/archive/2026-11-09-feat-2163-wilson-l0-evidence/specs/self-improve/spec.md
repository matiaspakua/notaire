## MODIFIED Requirements

### Requirement: Wilson autonomy score
The system SHALL compute a Wilson lower bound (95%) over **required** gate outcomes
from evidence ledger runs with `stage=signal` (the stage local preflight records).
Skip and todo statuses SHALL NOT count as passes and SHALL be excluded from the
Wilson trial count (skip ≠ pass). Autonomy level SHALL default to **L0** and SHALL
NOT report L3 (or L2) from this command. On the first incident (required gate fail
or an explicit `incident` flag on a counted run), autonomy SHALL demote to **L1**.

#### Scenario: Autonomy requires sample and Wilson floor
- **WHEN** fewer than 30 evaluable required-gate outcomes exist in `signal` runs
- **THEN** autonomy status is `no-evaluable` and level is 0
- **WHEN** at least 30 required-gate passes yield Wilson lower bound ≥ 0.80 and no incidents
- **THEN** autonomy reports L0-eligible (`status=ok`, `level=0`) — never level 3
- **WHEN** any required-gate fail or incident is recorded in the sample
- **THEN** autonomy is demoted (`status=demoted`, `level=1`) regardless of raw pass rate

#### Scenario: Skip does not inflate Wilson trials
- **WHEN** a signal run contains only skipped required=false or skip-status gates
- **THEN** those gates are not counted as successes or trials for autonomy
