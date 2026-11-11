# dashboard-home-module-grid Specification

## Purpose
Home dashboard module cards keep token chrome, drop decorative icon scale, and
limit San Francisco Blue accent wells so the grid does not repeat the sidebar
with one tint.

## Requirements

### Requirement: Module icon wells do not scale on hover

Home module cards in `data-testid="dashboard-modules"` SHALL keep the design-token
card radius and typography. Their icon wells MUST NOT apply a decorative hover
scale (`scale-110` or equivalent transform scale).

#### Scenario: Module icon wells have no scale-110

- **GIVEN** the dashboard home module grid source
- **WHEN** module icon well classes are inspected
- **THEN** they do not include `scale-110`

### Requirement: At most one San Francisco Blue module well

Among home module icon wells, at most one MAY use San Francisco Blue
(`bg-primary/10` with `text-primary`). When the existing expirations alert is
the accent surface, every module well MUST use a mist well instead
(`bg-secondary` matching mist `#F5F5F7`).

#### Scenario: At most one SF Blue module well

- **GIVEN** the dashboard home module grid source
- **WHEN** module icon well classes are counted
- **THEN** the number of wells with both `bg-primary/10` and `text-primary` is ≤ 1

#### Scenario: Non-accent wells use mist

- **GIVEN** the dashboard home module grid source
- **WHEN** non-accent module icon wells are inspected
- **THEN** each uses `bg-secondary` (mist) rather than `bg-primary/10`

### Requirement: Module links stay keyboard-reachable

Every home module card SHALL remain a focusable link to its destination so
keyboard users can reach the same destinations as pointer users.

#### Scenario: Module links remain keyboard-reachable

- **GIVEN** the dashboard home page renders the module grid
- **WHEN** module anchors inside `dashboard-modules` are queried
- **THEN** at least one module link is present and has an `href`
