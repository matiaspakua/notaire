# AI SDLC — Cursor Cloud Agent Fleet

Configuration for Notaire's **Cursor Cloud** autonomous SDLC path:
issue → OpenSpec Gate 1 → TDD → implement → preflight/CI → PR → review → merge.

| Document | Purpose |
|----------|---------|
| [`FLEET-ARCHITECTURE.md`](FLEET-ARCHITECTURE.md) | Foreman + specialist roles, model recommendations, handoffs, local-ai exclusion, process learnings |
| [`ENVIRONMENT-CHECKLIST.md`](ENVIRONMENT-CHECKLIST.md) | Toolchain/secrets checklist for Cloud `environment.json` + Saved-card and ops pitfalls |
| [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md) | **Never merge on light-CI-only green** — wait for Integration, Coverage, Unit, Bruno, Playwright |
| [`VALIDATION-PLAN.md`](VALIDATION-PLAN.md) | Prove the fleet is ready before picking the first GitHub issue |
| [`fleet-manifest.yaml`](fleet-manifest.yaml) | Machine-readable role → skills → model map |

**Process learnings (must follow):** `Closes #<issue>` (not only `Issue: #N`); never commit PR Validation wiki reports onto PR heads with `[skip ci]`; host-network compose; run `bash .cursor/install.sh` until the Environment card is Saved (`openspec`+`bc` from that script); prefer `scripts/seed-openspec-change.sh` for Gate 1; **do not merge when only light CI (~12 checks) is green** — wait for heavy CI + Playwright ([CI-MERGE-GATE](CI-MERGE-GATE.md)). Details in [ENVIRONMENT-CHECKLIST §7](ENVIRONMENT-CHECKLIST.md) and [FLEET-ARCHITECTURE §9](FLEET-ARCHITECTURE.md).

**Agent definitions** (loadable by Cursor / Claude-compatible agents):

| Agent | Path |
|-------|------|
| Cloud Foreman (orchestrator) | [`.claude/agents/cloud-foreman.md`](../../../.claude/agents/cloud-foreman.md) |
| OpenSpec Planner | [`.claude/agents/openspec-planner.md`](../../../.claude/agents/openspec-planner.md) |
| Backend Implementer | [`.claude/agents/backend-implementer.md`](../../../.claude/agents/backend-implementer.md) |
| Frontend Design | [`.claude/agents/frontend-design.md`](../../../.claude/agents/frontend-design.md) |
| Testing / QA | [`.claude/agents/testing-qa.md`](../../../.claude/agents/testing-qa.md) |
| Existing specialists | [`.claude/agents/`](../../../.claude/agents/) (`java-architect`, `devops-engineer`, `security-auditor`, `code-reviewer`, `sync_issues_and_code`, `efficiency_config_agent`) |

## Authority

- Process: [`CONSTITUTION.md`](../../../CONSTITUTION.md)
- Specs: OpenSpec schema `notaire-sdlc` (`openspec/`)
- Skills catalog: [`.claude/skills/`](../../../.claude/skills/)
- Agent index: [`AGENTS.md`](../../../AGENTS.md)

## Explicit non-goals

- **Not** the macOS `local-ai/` runtime (oMLX / Codex / `local-ai/sdlc/foreman.sh`).
- **Not** product feature implementation from GitHub issues — fleet setup only until validation passes.

## Navigation

- [← Development docs](../)
- [CI Preflight](../CI-PREFLIGHT.md)
- [OpenSpec adaptations](../../../openspec/NOTAIRE-ADAPTATIONS.md)
