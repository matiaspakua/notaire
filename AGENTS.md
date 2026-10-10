# AGENTS.md — Notaire Agent Reference

Single entry point for coding agents. `CLAUDE.md` imports this file.

## Authority

**[`CONSTITUTION.md`](CONSTITUTION.md) is highest process authority.**  
Always-loaded digest: [`CONSTITUTION-AGENT-CARD.md`](CONSTITUTION-AGENT-CARD.md).  
Load the full Constitution when deciding gates, exceptions, or DoD.

## Mandatory workflow (load on change)

Before any code change:

1. Confirm GitHub Issue + Use Case.
2. OpenSpec Gate 1 from `docs/` (`openspec` + `bash workspace/sdlc/validate-sdlc-plan.sh`).
3. Branch `<type>/<#>_description` from updated `main`.
4. TDD — failing tests first, then implement.
5. Full suite + Playwright for UI; `bash workspace/sdlc/preflight.sh` before push.
6. Permanent docs + Conventional Commits + PR (`Closes #…`).

Operational detail (load when implementing): `.claude/rules/ai-agent-workflow.md` and
`.claude/skills/ai-agent-workflow/SKILL.md`.

## On-demand rules (do not always-load)

| When touching | Load |
|---------------|------|
| Any Java/backend | `.claude/rules/programming.md`, `.claude/rules/code-quality.md`, `.claude/skills/java/SKILL.md`, `.claude/skills/backend/SKILL.md` |
| Flyway / schema | `.claude/rules/database-migrations.md`, `.claude/skills/flyway/SKILL.md` |
| Frontend / forms / theme | `.claude/rules/ui-ux-design.md`, `.claude/skills/frontend-design/SKILL.md` |
| Refactors | `.claude/rules/refactoring.md` |
| CI / Docker / observability | `.claude/agents/devops-engineer.md`, `.claude/skills/devops/SKILL.md` |
| Security review | `.claude/agents/security-auditor.md`, `.claude/skills/secure-threat-modeling/SKILL.md` |
| OpenSpec propose/apply | `.claude/skills/openspec-*/SKILL.md` |

General hygiene (short): `.claude/rules/general.md` — load at session start if needed;
prefer skills over pasting full rule bodies into context.

## Modules

```bash
python3 workspace/modules.py list
python3 workspace/modules.py affected <path>
python3 workspace/modules.py verify <module>|--all
```

See `workspace/modules.yaml` and each module’s `MODULE.md`.

## Cloud fleet

Cursor Cloud AI SDLC: `docs/300-development/304-ai-sdlc-cloud/`.  
Foreman: `.claude/agents/cloud-foreman.md`. Does not use `local-ai/`.

## Context budget (#1259)

Always-loaded set must stay ≤8,000 tokens (bytes/4) per
`python3 workspace/ci/agent-context-budget.py --max-tokens 8000`.
Do not re-add bulk `@` rule imports or `alwaysApply: true` on large skills.
