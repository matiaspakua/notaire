## ADDED Requirements

### Requirement: Triage may name the surface its tests live on

When no Files to Edit path is under a surface root, the change's surface SHALL be
the `TEST_SURFACE` from `triage.env`, if the adapter defines that surface.

#### Scenario: Fallback used

- **WHEN** Files to Edit lists only `docs/x.csv` and `TEST_SURFACE` is `backend`
- **THEN** the change's surface is `backend`

#### Scenario: Path surface wins

- **WHEN** Files to Edit lists `frontend/a.tsx` and `TEST_SURFACE` is `backend`
- **THEN** the change's surface is `frontend`

#### Scenario: Unknown fallback ignored

- **WHEN** Files to Edit lists only `docs/x.csv` and `TEST_SURFACE` is `mobile`
- **THEN** the change's surface is `none`
