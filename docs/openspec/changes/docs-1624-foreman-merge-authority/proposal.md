# One merge-authority rule: the Foreman merges under the Foreman merge rule

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1624 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1624_foreman-merge-authority` |
| Gate 1 status | passed |

## Objetivo

The Constitution said only the Owner merges, while the foreman playbooks said the
Foreman merges. Both happened in practice, including merges with red checks. On
2026-10-13 the Owner chose option (b): the Foreman merges under a rule. The
`protect-main` ruleset now enforces the required checks (#1040). This is a
**Constitution amendment** (§12), so the Owner must merge it.

## What Changes

- CONSTITUTION §5 steps 20–21: a new **Foreman merge rule**. Gate 4, §9, §10 (merge authority, Roles) and §12 enforcement are aligned with it.
- The agent card, `ai-agent-workflow.md`, `cloud-foreman.md` and `CI-MERGE-GATE.md` state the same rule.
- `local-ai/sdlc/foreman.sh` `merge_and_close` runs `check-heavy-ci.sh` before `gh pr merge`.
- `workspace/tests/test_merge_authority.py` guards the rule and its consistency across the docs.
- `.claude/rules/hooks.md` and the devsecops README now describe the ruleset as live.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The Foreman merges a Worker PR only with all required checks green and heavy CI passing | Owner decision on #1624 | Changed (was Owner-only) |
| The Owner merges Constitution, `sdlc-exception` and harness/merge-path PRs | CONSTITUTION §12 | Made explicit |
| Workers never merge | CONSTITUTION §10 | Unchanged |

## Capabilities

### New Capabilities

### Modified Capabilities
- `ai-sdlc-enforcement`: adds the Foreman merge rule requirement.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| Governance docs / `local-ai` harness | yes | Rule text; one gate line in `foreman.sh` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Governance change recorded as a Constitution amendment (allowed by #1624 instead of an ADR).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | Foreman merge rule; §5, Gate 4, §9, §10, §12 |
| `CONSTITUTION-AGENT-CARD.md` | One rule line |
| `.claude/rules/ai-agent-workflow.md` | Steps 20–21, Never list |
| `.claude/agents/cloud-foreman.md` | Merge conditions and Owner-only PRs |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | "Who may merge" |
| `.claude/rules/hooks.md`, `docs/200-architecture/208-devsecops/README.md` | Ruleset is live |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Auto-merge bots or GitHub auto-merge.
- Changing which checks are required (ruleset desired JSON unchanged).
