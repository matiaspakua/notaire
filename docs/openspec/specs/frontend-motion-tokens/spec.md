# frontend-motion-tokens Specification

## Purpose
Shared frontend motion timing (`theme/motion.ts` / `--motion-*`), including
DataTable row and mobile-card entrance on the token contract.

## Requirements

### Requirement: DataTable row entrance uses motion tokens

`DataTable` desktop rows and mobile cards SHALL enter with `motion.duration.base`, `motion.ease.standard`, `motion.distance.sm` for the Y rise, and `staggerDelay` for per-index delay. Sources SHALL NOT hardcode 0.3s duration, Material ease `[0.4, 0, 0.2, 1]`, a 6px rise, or a stagger cap of 0.3s. Reduced motion SHALL leave rows untransformed via the existing `MotionConfig reducedMotion="user"` path (no new config in this change).

#### Scenario: Row transition uses token duration and ease

- **GIVEN** the shared motion tokens in `theme/motion.ts`
- **WHEN** `rowTransition(i)` is evaluated for a DataTable row
- **THEN** duration equals `motion.duration.base`
- **AND** ease equals `motion.ease.standard`
- **AND** delay equals `staggerDelay(i)`

#### Scenario: Row rise uses distance.sm

- **GIVEN** a DataTable row entrance animation
- **WHEN** the initial Y offset is read
- **THEN** it equals `motion.distance.sm` (4px), not 6

#### Scenario: Hardcoded legacy timing is rejected

- **WHEN** the DataTable unit suite runs
- **THEN** it fails if duration 0.3 or Material ease `[0.4, 0, 0.2, 1]` returns on the row transition helper
