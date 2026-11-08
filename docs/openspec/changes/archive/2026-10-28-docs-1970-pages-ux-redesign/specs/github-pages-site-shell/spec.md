## ADDED Requirements

### Requirement: One site shell on every page

The GitHub Pages site SHALL render one header, one footer and one skip link on every page from the root layout, with
the primary entries AI Factory, Product, Technical docs and Releases, read from one site map. Sections SHALL NOT ship
their own headers.

#### Scenario: Technical docs are one click away

- **WHEN** a visitor is on any page, at any width
- **THEN** one activation of "Technical docs" in the header (or in the open mobile menu) opens `/docs/`

### Requirement: Accessible mobile menu

Below the large breakpoint the header SHALL show a menu button with `aria-expanded` and `aria-controls`. It SHALL open
a modal dialog that contains the primary entries and the current section's pages, keeps keyboard focus inside while
open, closes on Escape, and returns focus to the button.

#### Scenario: Keyboard user opens and closes the menu

- **WHEN** a visitor at 375 px opens the menu, presses Tab repeatedly and then Escape
- **THEN** focus never leaves the drawer while it is open, and after Escape the drawer is gone and the button has focus

### Requirement: Findable documentation

The Technical docs and the AI Factory SHALL show breadcrumbs, a sidebar of their pages, and previous/next links. The
site SHALL offer a static search over every page's title, summary and keywords.

#### Scenario: Search finds a page

- **WHEN** a visitor searches for "quality gates"
- **THEN** the first result links `/ai-sdlc/quality-gates/`

### Requirement: One design system

The site SHALL define its colours, type and radii once as design tokens and use one grey ramp across sections; the
AI Factory MAY use its own accent. Text SHALL meet WCAG 2.1 AA contrast, and the system cursor SHALL stay visible.

#### Scenario: Automated accessibility check

- **WHEN** axe (WCAG 2 A/AA) runs on the audited pages at 375 and 1280 px
- **THEN** it reports no violations, and no page scrolls horizontally at 375 px

### Requirement: The Pages app is checked on every pull request

A pull request that changes `github-page/` SHALL run typecheck, lint, unit tests, the production build, an internal
link check and the Playwright smoke suite, and `preflight.sh` SHALL mirror those checks.

#### Scenario: A broken internal link

- **WHEN** a page links a route or `#anchor` that the static export does not contain
- **THEN** `npm run check:links` fails the Pages CI job
