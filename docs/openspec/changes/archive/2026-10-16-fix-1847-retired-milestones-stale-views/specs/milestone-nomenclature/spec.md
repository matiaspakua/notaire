## ADDED Requirements

### Requirement: Merged-away milestones and stale views do not linger

The apply run SHALL delete a milestone whose title is a rename source once its target exists and it holds no issue, and `--check` SHALL count each such milestone still present and each project view outside the view contract, except an old view name the run will still rename (#1847).

#### Scenario: Empty bucket merged into H8

- **WHEN** `History · v1.1 priority bucket` and H8 both exist and the bucket is empty
- **THEN** the apply run deletes the bucket, and `--check` reports it until then

#### Scenario: Bucket before its target exists

- **WHEN** the target milestone does not exist yet
- **THEN** the bucket is renamed, not retired

#### Scenario: Pre-taxonomy view

- **WHEN** the project has `Casos de Uso` or `RNF`, or `KANBAN` beside `Kanban`
- **THEN** `--check` reports each as a view to delete; `KANBAN` alone is not reported, because the run renames it
