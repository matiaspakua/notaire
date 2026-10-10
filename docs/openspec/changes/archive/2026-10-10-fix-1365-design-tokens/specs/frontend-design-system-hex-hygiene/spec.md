# frontend-design-system-hex-hygiene — delta

## Purpose

Design-system colour hygiene.

## ADDED Requirements

### Requirement: One design-token source for colour

`frontend/src/app/globals.css` `:root` SHALL be the single runtime source of colour, mapped to utilities by Tailwind `@theme`, and SHALL declare semantic `success`, `warning` and `info` tokens with foregrounds. `theme/tokens.ts` SHALL mirror those values and SHALL be the only place the brand hex `#0071E3` is written. Sources in `app/`, `components/`, `lib/` and `hooks/` SHALL NOT use raw Tailwind palette utilities, numeric scales on semantic tokens or `[#hex]` arbitrary colours. There SHALL be no dark-mode block until dark mode is designed.

#### Scenario: Raw palette guard

- **WHEN** the Vitest design-tokens suite scans the frontend sources
- **THEN** it finds no raw palette utility such as `bg-blue-50` or `from-emerald-500`

#### Scenario: Tokens stay in sync

- **WHEN** `--primary`, `--destructive`, `--success`, `--warning` or `--info` changes in `globals.css`
- **THEN** the suite fails until `tokens.ts` mirrors it

#### Scenario: Brand primary everywhere

- **WHEN** an administrator opens /dashboard/personas
- **THEN** the primary button and the active navigation item render rgb(0, 113, 227)

#### Scenario: One module-tile style

- **WHEN** the dashboard renders its module grid
- **THEN** every tile has the same `bg-primary/10 text-primary` style and no gradient

#### Scenario: No dark mode

- **WHEN** the stylesheet is loaded
- **THEN** it has no `.dark` block and no source uses `dark:` utilities
