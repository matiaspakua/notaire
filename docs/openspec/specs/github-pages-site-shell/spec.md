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

### Requirement: Theme follows the visitor and can be chosen

The shell SHALL offer light, dark, and system theme. The default SHALL follow `prefers-color-scheme` until the visitor chooses. The choice SHALL be stored in `localStorage` under `notaire.theme` and applied on the document element before first paint. A theme change SHALL NOT animate when `prefers-reduced-motion: reduce` is set. Text and controls SHALL meet WCAG 2.1 AA contrast in both themes.

#### Scenario: Dark theme is saved

- **WHEN** a visitor chooses dark and reloads the page
- **THEN** the document element has `data-theme="dark"` before the body is painted and the choice remains dark

#### Scenario: Reduced motion

- **WHEN** the visitor's system asks for reduced motion
- **THEN** the theme change does not run a color transition

### Requirement: Language follows the visitor and can be chosen

The shell SHALL offer English, Spanish, and the system language. The default SHALL follow the browser language until the visitor chooses. The choice SHALL be stored in `localStorage` under `notaire.lang`. Every page in the public site map — the shell, the home page, the product, security, the AI Factory, about, and every technical-docs page — SHALL render in the selected language. That includes sidebar titles, anatomy figure plate copy and steps, Mermaid sentence labels, and Diagram captions on those routes. Identifiers (class names, commands, file paths) and the English technical terms Issue, pull request, gate, OpenSpec, Flyway, commit, kit and Owner SHALL stay in English. A route that is not in that set SHALL stay in English and, when Spanish is selected, SHALL say that it is still in English. Spanish copy SHALL come from one dictionary keyed by the English sentence.

#### Scenario: Spanish shell

- **WHEN** a visitor selects Spanish
- **THEN** the primary navigation shows "Documentación técnica" for `/docs/` and the home page heading is in Spanish

#### Scenario: Spanish product page

- **WHEN** Spanish is selected and the visitor opens `/product/`
- **THEN** the page heading is in Spanish and the page does not say that it is still in English

#### Scenario: Spanish hexagonal page

- **WHEN** Spanish is selected and the visitor opens `/docs/hexagonal/`
- **THEN** the page heading is in Spanish and the page does not say that it is still in English

#### Scenario: Anatomy figure follows Spanish

- **WHEN** a visitor selects Spanish and opens `/product/anatomy/`
- **THEN** the anatomy plate title and the step readout are Spanish, and the page does not show the English-notice banner

#### Scenario: Mermaid sentence labels follow Spanish

- **WHEN** a visitor selects Spanish and opens a factory page that renders a Mermaid diagram with sentence labels
- **THEN** those sentence labels are Spanish while commands and file paths in the same diagram stay as in the repository

#### Scenario: Playwright checks Spanish headings on anatomy and lifecycle

- **WHEN** Pages Playwright runs the Spanish preference suite at 375 and 1280 px in light and dark
- **THEN** it finds a Spanish heading on product anatomy and on the factory lifecycle pages, and those pages do not show the English-notice banner

### Requirement: The product story does not invent a measurement

The product story SHALL list each AI tool by the role it played. It SHALL NOT show a percentage of AI work unless a committed snapshot stores that share. The numbers page SHALL say the legacy `jpa` package and `JpaAdministrator` were retired, and SHALL NOT say that package is still being migrated.

#### Scenario: No unmeasured share

- **WHEN** a visitor opens the product story
- **THEN** the tools section has no "80%" share

#### Scenario: Legacy JPA is retired

- **WHEN** a visitor opens the product numbers page
- **THEN** the legacy JPA note says the package was retired and cites #576

### Requirement: Security Identity matches shipped MFA

The public security posture SHALL describe TOTP MFA as shipped. It SHALL say the default policy is OPTIONAL and that ENFORCE can require MFA for ADMIN and NOTARY. It SHALL NOT claim there is no second factor.

#### Scenario: Identity summary

- **WHEN** a visitor opens the security overview
- **THEN** the Identity posture names TOTP MFA and OPTIONAL / ENFORCE

### Requirement: Release history matches the product version on main

The Pages release snapshot SHALL use the root version from `.release-please-manifest.json`. Local preflight SHALL run the Pages `npm run build` so that file is refreshed before the export is checked.

#### Scenario: Current tag

- **WHEN** the release-please manifest root version is `X.Y.Z`
- **THEN** the Pages release history product version is `X.Y.Z` and its first tag is `vX.Y.Z`
