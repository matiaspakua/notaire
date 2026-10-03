<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Ensure dashboard shell, dashboard home, and the shared Table component resolve
colors through the Notaire design system (CU76 / RF #78) rather than hardcoded
`#RRGGBB` literals in application and UI component sources.

## ADDED Requirements

### Requirement: Audited dashboard and table surfaces use theme tokens

The dashboard layout, dashboard home page, and shared `Table` UI components
SHALL NOT embed `#RRGGBB` color literals. Colors MUST resolve through
`theme.colors.*` / `theme.semantic.*` style props or semantic Tailwind classes
already wired to design-system tokens (`bg-background`, `text-foreground`,
`text-muted-foreground`, `text-primary`, `text-destructive`, and equivalents).
Opacity intent on table header and row hover/selected states MUST be preserved
via color-mix, rgba derived from a token, or Tailwind opacity utilities — not
by inventing new hex values outside `frontend/src/theme/tokens.ts`.

#### Scenario: Dashboard layout has no hardcoded hex

- **WHEN** the Vitest hex-hygiene suite scans `frontend/src/app/dashboard/layout.tsx`
- **THEN** it finds no `#RRGGBB` literals (issue-number references like `#960` are ignored)

#### Scenario: Dashboard home page has no hardcoded hex

- **WHEN** the Vitest hex-hygiene suite scans `frontend/src/app/dashboard/page.tsx`
- **THEN** it finds no `#RRGGBB` literals

#### Scenario: Shared table UI has no hardcoded hex

- **WHEN** the Vitest hex-hygiene suite scans `frontend/src/components/ui/table.tsx`
- **THEN** it finds no `#RRGGBB` literals and table header/hover opacity remains expressed via token-based opacity (not new hex)

#### Scenario: Audited globs remain hex-free outside theme

- **WHEN** the Vitest hex-hygiene suite scans `frontend/src/app/**` and `frontend/src/components/**` excluding `theme/`
- **THEN** no file under those globs contains a `#RRGGBB` color literal (excluding issue refs)

#### Scenario: Visual parity across viewports

- **WHEN** an authenticated user views the dashboard home (and a page using `Table`) at 320px, 768px, and 1024px
- **THEN** layout, contrast hierarchy, and interactive affordances remain equivalent to pre-change appearance (no redesign)
