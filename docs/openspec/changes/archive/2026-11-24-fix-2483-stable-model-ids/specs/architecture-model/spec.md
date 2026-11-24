## ADDED Requirements

### Requirement: Generated model data is identical in every checkout

`npm run generate` SHALL produce byte-identical `github-page/lib/architecture-model.json` and Mermaid files in any
checkout of the same commit; no generated identifier may depend on the checkout path.

#### Scenario: Two worktrees generate the same data
- **WHEN** `npm run generate` runs in two worktrees of the same commit
- **THEN** both write the same `architecture-model.json`, and relationship finding ids read `source -> target: title`
