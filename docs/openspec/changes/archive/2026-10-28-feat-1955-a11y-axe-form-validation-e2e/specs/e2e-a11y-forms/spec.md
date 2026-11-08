## ADDED Requirements

### Requirement: Automated WCAG 2.1 AA axe scans

The Playwright suite SHALL run `@axe-core/playwright` with WCAG 2.1 A/AA tags on
login and featured dashboard routes. Non-allowlisted serious or critical
violations SHALL fail the run. The allowlist SHALL cite a GitHub issue per entry.

#### Scenario: Dashboard home axe scan

- **WHEN** an admin opens `/dashboard` and axe runs with wcag2aa
- **THEN** any non-allowlisted serious/critical violation fails the test

### Requirement: High-traffic form required-field E2E

People, budgets, deeds, and managements create dialogs SHALL show a visible
FormField error (`role="alert"`) when save is clicked with required fields empty,
and the dialog SHALL remain open.

#### Scenario: Empty budget create shows field errors

- **WHEN** the user opens New quote and clicks Create without filling required fields
- **THEN** at least one `role="alert"` is visible in the dialog
