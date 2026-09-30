## ADDED Requirements

### Requirement: The spec gate removes specs a skip_specs change left behind

When a non-code change sets `skip_specs: true`, the spec gate SHALL delete its
`specs/` folder before validation and log the repair.

#### Scenario: Leftover specs removed

- **WHEN** KIND is `ci`, `.openspec.yaml` sets `skip_specs: true` and `specs/ci/spec.md` exists
- **THEN** `specs/` is removed and `openspec validate --strict` runs on the rest

### Requirement: The spec gate unticks tasks outside groups 1-2

The spec gate SHALL untick every task whose group number is greater than 2.

#### Scenario: Premature tick removed

- **WHEN** `tasks.md` has `- [x] 4.1 Delete the Jenkinsfile`
- **THEN** it reads `- [ ] 4.1 Delete the Jenkinsfile` and 4.1 is reported as unticked

#### Scenario: Prerequisite tick kept

- **WHEN** `tasks.md` has `- [x] 1.1 GitHub Issue exists`
- **THEN** the line is unchanged and nothing is reported

### Requirement: Markdown repair fixes fence languages and table pipes

Before markdown lint, the harness SHALL give a bare opening code fence the
language `text` and append the missing trailing pipe to a table row.

#### Scenario: Bare opening fence

- **WHEN** a file has a code block opened by a bare fence
- **THEN** the opening fence reads ```` ```text ```` and the closing fence stays bare

#### Scenario: Table row without trailing pipe

- **WHEN** a table row reads `| Use Case | CU76`
- **THEN** it reads `| Use Case | CU76 |`

### Requirement: The worker's recursive searches skip dependency trees

The harness SHALL run the worker's shell with `grep`/`find` shims that prune
`node_modules`, `target`, `.git` and `.next` from recursive searches.

#### Scenario: Recursive grep skips node_modules

- **WHEN** the worker runs `grep -rl jenkins .` in a tree with `src/` and `node_modules/`
- **THEN** only the file under `src/` is listed
