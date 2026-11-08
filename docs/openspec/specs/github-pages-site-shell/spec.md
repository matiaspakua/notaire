# github-pages-site-shell Specification

## Purpose
The shared shell of the GitHub Pages site (`github-page/`): one header, navigation, search, document layout and design system for every section, and the checks that keep it working on pull requests. Source: #1970; owner CU76.

## Requirements

### Requirement: One site shell on every page

The GitHub Pages site SHALL render one header, one footer and one skip link on every page from the root layout, with
the primary entries AI Factory, Product, Security, Technical docs and Releases, read from one site map. Sections SHALL NOT ship
their own headers. The Security entry SHALL open `/security/`.

#### Scenario: Technical docs are one click away

- **WHEN** a visitor is on any page, at any width
- **THEN** one activation of "Technical docs" in the header (or in the open mobile menu) opens `/docs/`

#### Scenario: Security is a primary entry

- **WHEN** the site map is read
- **THEN** it contains a Security entry whose href is `/security/`

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

A change that touches `github-page/` SHALL be checked by `bash workspace/sdlc/preflight.sh` (typecheck, lint, unit tests, production build, internal link check; `--full` adds the Playwright smoke suite) before the pull request is opened. `.github/workflows/pages-ci.yml` SHALL run those same checks on a version tag (`v*` / `notaire-v*`) and on `workflow_dispatch`, and SHALL NOT run them on `pull_request`. The workflow SHALL NOT deploy.

#### Scenario: A broken internal link

- **WHEN** a page links a route or `#anchor` that the static export does not contain
- **THEN** `npm run check:links` fails the Pages check in `pages-ci.yml` and in `preflight.sh`

#### Scenario: Pages CI is not a pull-request workflow

- **WHEN** `.github/workflows/pages-ci.yml` triggers are read
- **THEN** `pull_request` is absent, `push.tags` includes `v*` and `notaire-v*`, and the workflow does not grant `pages: write`

### Requirement: The Product section has its own pages and sidebar

The Product section SHALL be a set of pages under `/product/` rendered by the shared document shell, with a sidebar
of the section's pages, breadcrumbs and previous/next links, like the Technical docs and the AI Factory. The header's
Product entry SHALL open `/product/`.

#### Scenario: Reader browses the product pages

- **WHEN** a reader opens `/product/story/`
- **THEN** the sidebar lists Overview, Story, Platform and Numbers, the breadcrumbs read Home › Product › Story, and
  previous/next link Overview and Platform

#### Scenario: Visitor lands on the home page

- **WHEN** a visitor opens `/`
- **THEN** the page links the Product, Technical docs and AI Factory sections

### Requirement: The Security section explains the public posture

The Security section SHALL be pages under `/security/` in the shared document shell, with a sidebar, breadcrumbs and
previous/next links. The section SHALL include an interactive STRIDE explorer and a toolchain cadence. Visible copy
SHALL say the posture is not a live scan. The route `/docs/security/` SHALL remain and SHALL link to `/security/`.

#### Scenario: A reader opens the threat explorer

- **WHEN** a reader opens `/security/threats/`
- **THEN** the page offers the six STRIDE properties and shows the selected property's requirement ids

#### Scenario: The old docs route still resolves

- **WHEN** a reader opens `/docs/security/`
- **THEN** the page links to `/security/`
