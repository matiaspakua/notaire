# ui-motion — delta

## Purpose

Enter/exit motion of overlays.

## MODIFIED Requirements

### Requirement: Dialogs animate in and out

Dialogs and confirm dialogs SHALL fade and scale in, SHALL animate out before unmounting, and SHALL show no perceptible motion under prefers-reduced-motion.

#### Scenario: Enter

- **WHEN** a dialog opens
- **THEN** its content and overlay run an animation

#### Scenario: Exit

- **WHEN** a dialog closes with Escape
- **THEN** it stays mounted with data-state=closed running an animation, then unmounts

#### Scenario: Reduced motion

- **WHEN** the user prefers reduced motion
- **THEN** the dialog animation lasts under 50ms
