# Honest Gate 1.6 when Issue label write is denied

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2221 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/2221_gh-issue-label-permission` |
| Gate 1 status | passed |

## Objetivo

Cursor Cloud ambient `gh` tokens can push and open PRs but often cannot add Issue
labels (`GraphQL: Resource not accessible by integration (addLabelsToLabelable)`).
Gate 1 task 1.6 and agent workflow assumed `gh issue edit --add-label` succeeded,
which either blocked agents or tempted a false "in progress". Document the required
`issues: write` / `addLabelsToLabelable` scope and teach an honest SKIP path.

## What Changes

- Durable docs: required GitHub App scopes for Issue labels; where the Owner grants
  them (Cursor Cloud App Issues Read & write, or `notaire-fleet` + `FLEET_APP_*`).
- `workspace/sdlc/add-issue-wip-label.sh` — resolve `status:in-progress`, attempt
  add-label, on 403 print **SKIPPED** and exit 0 (continue Gate 1; never fake WIP).
- Point agent workflow skill/rule + OpenSpec tasks template at the helper.
- Unittest: docs name scopes; 403 → honest SKIP; success → labeled.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Cloud / fleet tokens that mutate Issue labels MUST have Repository Issues Read & write (`issues: write`, GraphQL `addLabelsToLabelable`) | CU76 / #2221 / FLEET-IDENTITY | Made explicit |
| When the integration lacks that permission, Gate 1.6 MUST SKIP with an honest note and MUST NOT claim the Issue is in progress | CU76 / #2221 | New |
| Agents SHOULD use `add-issue-wip-label.sh` instead of raw `gh issue edit --add-label` for Gate 1.6 | CU76 / #2221 | New |

## Capabilities

### New Capabilities

- `gh-issue-label-permission`: Document required Issue-label scopes and honest Gate 1.6 skip when `addLabelsToLabelable` is denied.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | helper script + unittest |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (Owner may add `FLEET_APP_*` secrets offline)
- Dependencies: none

### Write set

- `workspace/sdlc/add-issue-wip-label.sh`
- `workspace/tests/test_add_issue_wip_label.py`
- `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md`
- `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md`
- `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md`
- `docs/openspec/schemas/notaire-sdlc/templates/tasks.md`
- `.claude/skills/ai-agent-workflow/SKILL.md`
- `.claude/rules/ai-agent-workflow.md`
- `CHANGELOG.md`
- `docs/openspec/changes/improve-2221-gh-issue-label-permission/**`

### Architecture review

Extends existing #1648 fleet-identity least-privilege story. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md` | Cloud vs fleet token; `issues: write` / `addLabelsToLabelable`; Owner grant; helper |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Secrets table: fleet App + honest SKIP when ambient token lacks label write |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Branch phase uses helper |
| `.claude/skills/ai-agent-workflow/SKILL.md` | Step 1.5 → helper |
| `.claude/rules/ai-agent-workflow.md` | Branch row → helper |
| OpenSpec tasks template | 1.6 wording |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

Product tests. Wilson. Portal/Context Lake. `[committer] required=true`. Foreman stage knobs. Reopening #2217 or #2218. Changing Cursor dashboard secrets from the agent.
