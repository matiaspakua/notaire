# Design — Cursor Cloud AI SDLC foreman fleet

## Context

`local-ai/sdlc/` already runs a phased Constitution loop on a local model, but that
runtime is macOS/oMLX/Codex-specific. Cursor Cloud needs its own foreman/specialist
configuration that reuses Constitution gates and `.claude/skills/` without depending
on `local-ai/`.

## Goals / Non-Goals

- Goal: version cloud foreman + specialist defs, handoff contracts, model
  recommendations, env checklist, and a pre-issue validation plan.
- Non-goal: pick or implement the first product GitHub issue in this change.
- Non-goal: modify `local-ai/` or `.aisdlc/project.yml`.

## Decisions

- **`skip_specs: true`**: docs/agent-config only; no product requirement deltas.
- **Reuse existing specialists** (`java-architect`, `devops-engineer`, `security-auditor`,
  `code-reviewer`, `sync_issues_and_code`) via `fleet-manifest.yaml` rather than duplicating them.
- **Cost-to-value models**: Sonnet medium for orchestration/implement; Opus medium for
  architecture/security; Composer for sync/CI-fix; upgrade path documented per role.
- **Mechanical gates remain authoritative**: OpenSpec validate, `validate-sdlc-plan.sh`,
  `preflight.sh`, `gh pr checks` — never trust agent prose alone.

## Riesgos / Trade-offs

- Cloud environment gaps (Maven/Docker/OpenSpec CLI) can block validation GO even when
  the fleet docs are complete — tracked in ENVIRONMENT-CHECKLIST.md.
- Branch name uses Cloud Agent prefix `cursor/…-6890` rather than
  `<type>/<issue>_…`; issue #1114 records the association.

## Testing Strategy

No application test surface. Verification (Gate 2 equivalent):

- Artifact presence checks from `VALIDATION-PLAN.md` step A.
- Manifest coherence: every `agent_file` and skill directory exists.
- `bash scripts/validate-sdlc-plan.sh --list` and `bash scripts/preflight.sh --list`.
- Exclusion check: cloud agents must not invoke `local-ai/sdlc/foreman.sh`.

## Regression Strategy

No application code changes. Confirm `sdlc-process.yml` accepts this PR once the
OpenSpec change folder is present (`openspec/changes/cursor-cloud-ai-sdlc-fleet/`).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Nothing is deployed to production runtime. Merge makes the fleet config available to
every clone and to Cursor Cloud agents reading the repo.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None for this change. Environment.json wiring is owned by the sibling env-mapping task.
