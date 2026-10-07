# Raise JaCoCo Coverage Floor Thresholds

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1063 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `test/1063_raise_jacoco_branch_floor` |
| Gate 1 status | pending |

## Objetivo

The JaCoCo coverage floor thresholds in `backend-api/pom.xml` and
`.claude/rules/code-quality.md` are set to 70% line / 25% branch, but actual
coverage is 84.9% line / 74.0% branch. The floor values are too low and leave
unrealistic room for regression; raising the floor will drive better coverage
quality.

## What Changes

- `backend-api/pom.xml`: JaCoCo plugin `<minimum>0.70</minimum>` → `<minimum>0.80</minimum>`, BRANCH `<minimum>0.25</minimum>` → `<minimum>0.65</minimum>`
- `.claude/rules/code-quality.md`: `line coverage floor` 70→80, `branch coverage floor` 25→65
- `JacocoCoverageConfigConsistencyTest.java`: update expected threshold values
- `openspec/specs/coverage/spec.md`: new capability for code coverage configuration (ADDED)

## Reglas de negocio

None. This is a configuration quality improvement; it changes test quality gates,
not business behavior or data.

## Capabilities

### New Capabilities

- Code coverage configuration (ADDED)

### Modified Capabilities

None — this change introduces a new JaCoCo coverage configuration capability.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|-------------|
| `backend-api` | yes | JaCoCo thresholds in pom.xml; unit tests for config consistency |
| `frontend` | no | — |
| `infra` | no | — |
| CI/CD | yes | Quality gate thresholds updated |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway): none
- Configuration / `.env`: no
- Dependencies: none
- Breaking: no — only affects JaCoCo checks, not runtime behavior

## Documentation Impact

| Permanent document | What must change |
|--------------------|----------------|
| `.claude/rules/code-quality.md` | JaCoCo floor thresholds updated 70%/25% → 80%/65% |
| `CONSTITUTION.md` | JaCoCo row (line 496) floor 70%/25% → 80%/65% |
| `CHANGELOG.md` | n/a — internal CI configuration change |
| `.github/pull_request_template.md` | none — it states the ≥80% long-term target, not the floor |

## Out of Scope

- Raising line coverage floor above 80% (intermediate target only)
- DeedManagement class test coverage (separate gap at ~41%)
- Business package branch coverage (already at ~67%, close to 65% floor)
