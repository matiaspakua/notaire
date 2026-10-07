# Seed OpenSpec templates on setup; reject unfilled sections

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1108 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/chore-1108-openspec-template-seed-30a2` |
| Gate 1 status | passed |

## Objetivo

In the spec phase, agents often write `proposal.md`, `design.md`, `tasks.md` and
`traceability.md` from scratch. Local models (Qwen3-Coder on #1049, gpt-oss on
\#1062) repeatedly omitted required sections or whole files, and
`validate-sdlc-plan.sh` failed every attempt. The schema already ships templates
with every mandatory heading and `<!-- ... -->` guidance, but `openspec new
change` only creates `.openspec.yaml` — it does not copy those templates into the
change folder. Cursor Cloud / generic OpenSpec agents hit the same gap.

## What Changes

- Add `scripts/seed-openspec-change.sh`: after `openspec new change` (or for an
  existing empty change), copy the four `notaire-sdlc` templates into the change
  folder when absent, and fill known values (Issue, Use Case, Branch, change name).
- Strengthen `scripts/validate-sdlc-plan.sh` so Gate 1 rejects a `##` section
  whose body is still only a leftover template `<!-- ... -->` comment, naming
  the file and heading.
- Document the seed → fill → validate path in permanent OpenSpec / specification
  docs (generic path; not `local-ai/`).
- Self-tests under `scripts/tests/` cover seeding and leftover-comment rejection.
- Archive the stale active change for CLOSED issue #1062 so the validator can
  pass on all active changes (same learning as #1070) — already landed on `main`
  via #1113; this branch rebased onto that archive and does not re-archive it.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A notaire-sdlc change folder SHALL start from the schema templates, not a blank page | CU76 / CONSTITUTION Gate 1 | New |
| An incomplete plan with leftover template HTML comments SHALL fail Gate 1 | CONSTITUTION §6 Gate 1 | Made explicit |
| Known Issue / Use Case / Branch values MAY be pre-filled by the seeder | CU76 | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: template seeding after `openspec new change`, and
  leftover-comment rejection in `validate-sdlc-plan.sh`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | preflight already runs `validate-sdlc-plan.sh` |
| `scripts/` + `openspec/` | yes | seed script, validator, docs, schema instructions |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (bash + existing `openspec` CLI)

### Architecture review

Tooling / process only. Follows existing agent-agnostic gate pattern
(`validate-sdlc-plan.sh`). No ADR. Does **not** depend on `local-ai/` runtime.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `openspec/NOTAIRE-ADAPTATIONS.md` | Document seed script + leftover-comment check |
| `docs/300-development/templates/specification-template.md` | Seed step in producing a Specification |
| `openspec/schemas/notaire-sdlc/schema.yaml` | Instructions: fill seeded files; keep headings |
| `openspec/specs/ai-sdlc-enforcement/spec.md` | Fold accepted delta on archive |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Changing the local-ai `phase_setup` / `03-spec.md` harness (tracked for that
  stack separately; this PR implements the **generic** OpenSpec equivalent).
- Auto-filling prose sections beyond known Issue / Use Case / Branch / change name.
- Inventing delta specs for pure docs when `skip_specs: true` applies.
