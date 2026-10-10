# frontend-motion-tokens — delta

## Purpose

Shared motion timing for the frontend.

## ADDED Requirements

### Requirement: One motion token system

`frontend/src/app/globals.css` SHALL define the motion durations (fast 120ms, base 180ms, slow 240ms, exit 180ms, page 160ms) and easings (standard, emphasized, exit) once, `theme/motion.ts` SHALL mirror them for motion/react, and sources SHALL NOT use numeric Tailwind durations or transition every property. Route changes SHALL NOT wait for an exit animation, staggered entrances SHALL be capped at 6 items x 30ms, and reduced motion SHALL leave no transform.

#### Scenario: Tokens stay in sync

- **WHEN** a `--motion-*` value changes in globals.css
- **THEN** the Vitest motion-tokens suite fails until theme/motion.ts mirrors it

#### Scenario: No ad-hoc durations

- **WHEN** the suite scans app/, components/, theme/, lib/ and hooks/
- **THEN** it finds no `duration-NNN`, `transition-all` or `transition: all`

#### Scenario: Fast route change

- **WHEN** a user clicks a sidebar link
- **THEN** the new page's content is fully opaque within a median of 300ms

#### Scenario: Dialog timing

- **WHEN** a dialog opens
- **THEN** it animates for 240ms with the emphasized easing

#### Scenario: Reduced motion

- **WHEN** the user prefers reduced motion
- **THEN** page content ends without a transform and dialog animations are near-instant
