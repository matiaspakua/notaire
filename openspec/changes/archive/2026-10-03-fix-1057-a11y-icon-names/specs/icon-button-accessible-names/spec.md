<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Ensure every icon-only action button in the Notaire web dashboard exposes a
translated accessible name so assistive technologies and role-based selectors
can identify the control. Source: #1057; quality owner CU76 accessibility
validation; WCAG 2.1 SC 4.1.2 via `.claude/rules/ui-ux-design.md`.

## ADDED Requirements

### Requirement: Icon-only buttons have translated accessible names

Every icon-only `<Button>` in the dashboard (Lucide or `NotaireIcon` child with
no visible text) SHALL expose a translated accessible name via `aria-label`
(or equivalent name computation that Playwright
`getByRole('button', { name })` can resolve). Relying solely on an image `alt`
inside the button without an explicit button name MUST NOT be treated as
complete for this requirement.

#### Scenario: Edit action is named

- **WHEN** a user (or assistive technology) focuses an icon-only edit action
  on a listed page (personas, escrituras, pagos, usuarios, roles, or equivalent)
- **THEN** the control’s accessible name matches the translated edit label
  (for example Spanish “Editar”)

#### Scenario: Delete action is named

- **WHEN** a user focuses an icon-only delete action on a listed page
- **THEN** the control’s accessible name matches the translated delete label
  (for example Spanish “Eliminar”)

#### Scenario: Presupuesto resumen action is named

- **WHEN** a user focuses the icon-only presupuesto summary (Receipt) action
- **THEN** the control’s accessible name is a non-empty translated summary
  label (not an empty or generic “button”)

#### Scenario: Admin NotaireIcon row actions are named

- **WHEN** a user focuses edit or delete on administracion conceptos,
  documentos, or tramites row actions that render `NotaireIcon`
- **THEN** the Button itself has a translated `aria-label` (not only img `alt`)

### Requirement: Lint prevents unnamed icon-only buttons

The frontend lint configuration MUST enable a `jsx-a11y` rule that fails when
an icon-only button lacks an accessible name, so regressions are caught in CI.

#### Scenario: Unnamed icon button fails lint

- **WHEN** a developer adds a `<Button>` whose only content is an icon and
  omits `aria-label` / accessible name
- **THEN** `eslint` / frontend lint reports a jsx-a11y violation for that
  control

#### Scenario: Named icon button passes lint

- **WHEN** an icon-only `<Button>` includes a translated `aria-label`
- **THEN** lint does not report an accessible-name violation for that control

### Requirement: Playwright can locate actions by name

Automated E2E tests MUST be able to target the fixed actions with
`getByRole('button', { name })`, and previously skipped CU21 edit coverage
that was blocked only by missing names MUST be restored.

#### Scenario: Role-based selector finds edit on usuarios

- **WHEN** an authenticated admin opens Usuarios administration
- **THEN** at least one `getByRole('button', { name: /editar/i })` is visible
  for a data row

#### Scenario: CU21 edit flow is no longer skipped for naming

- **WHEN** the suite runs TS-0016 CU21 edit scenarios that were skipped solely
  because the edit control had no accessible name
- **THEN** those scenarios are active (not skipped for that reason) and can
  open the edit dialog via the named button
