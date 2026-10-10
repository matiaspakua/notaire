# module-ownership-map

In-repo module ownership for #1197 Phase 0 is documented and diagrammed without splitting repositories.

## ADDED Requirements

### Requirement: Ownership document exists

The repository MUST contain `docs/300-development/MODULE-OWNERSHIP.md` that lists every module from
`workspace/modules.yaml` with path, fleet, responsibility, verify command, and dependencies.

#### Scenario: Manifest modules are listed

- **WHEN** an agent or human opens MODULE-OWNERSHIP.md
- **THEN** each key in `workspace/modules.yaml` appears with its responsibility and verify command

### Requirement: Mermaid dependency diagram

MODULE-OWNERSHIP.md MUST include a Mermaid diagram of module `depends_on` relationships consistent
with the manifest.

#### Scenario: Diagram present

- **WHEN** the ownership document is rendered on GitHub or Pages
- **THEN** a Mermaid `graph` (or equivalent) of module dependencies is present
