# architecture-model Specification

## Purpose

TBD - created by archiving change docs-2483-likec4-architecture-model. Update Purpose after archive.

## Requirements

### Requirement: One LikeC4 model is the source of the architecture views

The repository SHALL keep a LikeC4 model under `docs/200-architecture/210-architecture-model/`, pinned to an exact
`likec4` version, that describes the system context, the containers, both hexagons, the security controls, the
test pyramid and delivery pipeline, and the development, production and staging deployments. The model SHALL be
built from the code and SHALL tag each measured gap as a finding linked to its GitHub Issue.

#### Scenario: The model validates

- **WHEN** `npm run validate` runs in the model folder
- **THEN** LikeC4 reports no syntax or semantic error

#### Scenario: Findings carry their Issue

- **WHEN** an element or relationship is tagged `#finding`
- **THEN** it links a `https://github.com/matiaspakua/notaire/issues/<n>` URL that the Pages findings table shows

### Requirement: Markdown and Pages diagrams are generated from the model

`npm run generate` SHALL write one Mermaid file per view, the GitHub Pages data file and every
`<!-- likec4-view: <id> -->` block in the documentation; `npm run check` and `docs/tests/test_architecture_model.py`
SHALL fail when a copy drifts from the model.

#### Scenario: A hand edit of a generated diagram fails

- **WHEN** a Mermaid block between `likec4-view` markers differs from `generated/mermaid/<id>.mmd`
- **THEN** `python3 docs/tests/test_architecture_model.py` fails and names the document and the view

#### Scenario: A sequence view becomes a sequence diagram

- **WHEN** a dynamic view declares `variant sequence`
- **THEN** the generated Mermaid is a numbered `sequenceDiagram` with return arrows

### Requirement: GitHub Pages publishes the model

The Pages build SHALL publish the interactive LikeC4 site under `/architecture-model/` with hash routing and a
docs page `/docs/architecture/model/` (English and Spanish) that renders every view and links it into the
interactive site; the Architecture, Security and Testing docs pages SHALL show their views from the same data.

#### Scenario: A view links into the interactive model

- **WHEN** the docs page renders the `containers` view
- **THEN** it links `/architecture-model/#/view/containers/`
