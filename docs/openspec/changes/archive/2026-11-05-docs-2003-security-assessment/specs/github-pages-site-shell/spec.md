## MODIFIED Requirements

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

## ADDED Requirements

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
