# <!-- change title -->

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #<!-- issue number --> |
| Use Case | <!-- CU-XX / RF-XX / RNF-XX --> |
| Branch | `<type>/<issue-number>_<description>` |
| Gate 1 status | <!-- pending / passed --> |

## Objetivo

<!-- Why this change is needed: 1-2 sentences on the problem or opportunity.
     What problem does this solve? Why now? -->

## What Changes

<!-- Describe what will change. Be specific about new capabilities, modifications,
     or removals. Mark breaking changes with **BREAKING**. -->

## Reglas de negocio

<!-- The business rules this change introduces, alters, or makes explicit. State
     each rule in one sentence. The normative, testable form of each rule belongs
     in the delta spec as a Requirement + Scenarios; here, name them and cite the
     Use Case or business document they come from.
     If the rule is new and no Use Case covers it, say so - a missing Use Case
     must be created before Gate 1 passes. -->

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| <!-- rule --> | <!-- CU-XX, RF-XX, or "none - Use Case required" --> | <!-- --> |

## Capabilities

### New Capabilities
<!-- Capabilities being introduced. Use kebab-case for path segments you introduce
     (e.g., user-auth or identity/user-auth) that follow the project's existing
     spec organization. Each creates specs/<capability-path>/spec.md. -->
- `<capability-path>`: <brief description of what this capability covers>

### Modified Capabilities
<!-- Existing capabilities whose REQUIREMENTS are changing (not just implementation).
     Only list here if spec-level behavior changes. Each needs a delta spec file.
     Use the exact existing path under openspec/specs/. Leave empty if no requirement
     changes. A change with no capabilities at all (pure refactor, tooling, docs)
     must set `skip_specs: true` in its .openspec.yaml - openspec validate rejects
     a zero-delta change without that marker. Do not invent a requirement just to
     satisfy validation. -->
- `<existing-capability-path>`: <what requirement is changing>

## Impact Analysis

### Módulos afectados

<!-- Mark every module this change touches. Delete rows that do not apply, but do
     not delete the table: an empty table means the analysis was not done. -->

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | <!-- yes/no --> | <!-- --> |
| `frontend` | <!-- yes/no --> | <!-- --> |
| `frontend-swing` | <!-- yes/no --> | <!-- --> |
| `notaire-shared` | <!-- yes/no --> | <!-- --> |
| `infra` / observability | <!-- yes/no --> | <!-- --> |
| CI/CD (`.github/workflows`) | <!-- yes/no --> | <!-- --> |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities:
- Endpoints:
- Database (Flyway `V{n}`):
- Configuration / `.env`:
- Dependencies:

### Architecture review

<!-- Does this follow the existing architecture (repository over legacy jpa, design
     system tokens, Flyway as single source of truth)? If the change is
     architectural, an ADR under docs/200-architecture/202-ADR/ is required - name it
     here. -->

## Documentation Impact

<!-- Which PERMANENT documentation must be updated before merge (Gate 3).
     Permanent docs are the single source of truth; this proposal is not.
     Do not duplicate their content here - name the file and what changes in it.
     Mark documents to be archived into docs/archive/. -->

| Permanent document | What must change |
|--------------------|------------------|
| <!-- e.g. docs/100-business/102-use-cases/CU15....md --> | <!-- --> |
| `CHANGELOG.md` | <!-- user-visible change entry, or "n/a - not user visible" --> |

## Out of Scope

<!-- What this change explicitly does not do, and where it is tracked instead. -->
