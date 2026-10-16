# fe-workflows-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1792-fe-workflows-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Workflows admin and workflow hooks are hexagonal

The workflows administration pages and `useWorkflow` / `useManagementWorkflow` hooks MUST NOT import `@/lib/api-client`. HTTP adapters MUST be the only modules for this surface that call api-client. ESLint suppressions for the moved files MUST be removed.

#### Scenario: workflow hooks via use cases
- **WHEN** hooks list or mutate workflow definitions, nodes, or transitions
- **THEN** calls go through composition → use cases → HTTP gateway

#### Scenario: workflows pages via use cases
- **WHEN** the admin validates or loads graph data for activate/edit
- **THEN** operations use composition-wired use cases without importing api-client in the page

#### Scenario: management workflow trace via use cases
- **WHEN** the dashboard loads a management workflow trace
- **THEN** calls go through composition without importing api-client in the hook

#### Scenario: suppressions shrink
- **WHEN** architecture-boundaries tests run
- **THEN** workflows pages and workflow hooks are absent from eslint-suppressions.json
