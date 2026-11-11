# ui-reduced-motion — delta (#2073)

## Purpose

Define the global CSS `prefers-reduced-motion` policy: remove transform travel
while keeping focus/color/opacity state transitions at `duration.fast`.

## ADDED Requirements

### Requirement: Reduced motion removes travel, keeps state transitions

When `prefers-reduced-motion: reduce` is set, the product CSS SHALL NOT apply a
global `0.01ms` kill to every `transition-duration`. Transform travel (slide,
scale, rotate) SHALL be removed. Focus-ring, color, and opacity state changes
SHALL use `duration.fast` (120ms / `--motion-duration-fast`). The
MotionConfig JS path SHALL keep `duration.instant` / `reducedMotion="user"`. The
login seal (`login-mark`) SHALL still report `transform: none` under reduced
motion.

#### Scenario: State transitions use duration.fast

- **WHEN** `prefers-reduced-motion: reduce` applies
- **THEN** the reduced-motion CSS block does not set a global
  `transition-duration: 0.01ms`
- **AND** color/opacity/focus-related transitions use
  `var(--motion-duration-fast)` (chosen over an instant cut; recorded in test)

#### Scenario: Transform travel is removed

- **WHEN** `prefers-reduced-motion: reduce` applies
- **THEN** CSS transitions under that media query do not animate `transform`
- **AND** decorative press scale on `.apple-button:active` is `transform: none`

#### Scenario: Login seal has no transform under reduced motion

- **WHEN** the user prefers reduced motion
- **THEN** the login seal path still reports `transform: none`
  (`MotionConfig reducedMotion="user"` / #2069)
