<!-- Delta: Phase E documents that runtime Start-run demo depends on an approved
     workflow from the adopter demo path. No new runtime UI surface. -->

# factory-runtime-ui Specification (delta)

## MODIFIED Requirements

### Requirement: Approved-only workflow picker

The Start run workflow picker SHALL list only workflows whose governance state
is `approved`. Draft, defined, and validated workflows MUST be excluded from
production start options. Adopter demo seed (#2243) SHALL be the supported way
to materialize at least one approved workflow for local demos.

#### Scenario: approved-only workflow picker

- **WHEN** both an approved workflow and a draft workflow exist
- **THEN** the Start run HTML options include the approved workflow id and do
  not include the draft workflow id
