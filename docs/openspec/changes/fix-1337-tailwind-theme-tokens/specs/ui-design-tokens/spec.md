# ui-design-tokens — delta

## Purpose

Semantic colour utilities resolve to the design-system tokens.

## MODIFIED Requirements

### Requirement: Semantic colour classes render the token colours

Every shadcn colour token SHALL be registered as a Tailwind v4 `--color-*` theme colour so `bg-*`, `text-*`, `border-*` and `ring-*` utilities emit CSS, and the primary colour SHALL give white text at least 4.5:1.

#### Scenario: Tokens registered

- **WHEN** globals.css is read
- **THEN** an `@theme inline` block maps the 19 tokens

#### Scenario: Primary button

- **WHEN** `/login` renders
- **THEN** the submit button background is #0071E3 with white text

#### Scenario: Active nav and delete

- **WHEN** `/dashboard/personas` renders
- **THEN** the current nav item has the primary pill and delete icons are red
