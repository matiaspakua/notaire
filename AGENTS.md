# AGENTS.md — Notaire Agent Reference

Single entry point for coding agents. `CLAUDE.md` imports this file.

## Authority

**[`CONSTITUTION.md`](CONSTITUTION.md) is highest process authority.**  
Always-loaded digest: [`CONSTITUTION-AGENT-CARD.md`](CONSTITUTION-AGENT-CARD.md).  
Load the full Constitution when deciding gates, exceptions, or DoD.

**Principles (CONSTITUTION §2):** KISS — Keep It Simple; bounded Boy Scout — leave
touched code cleaner in the same PR without behavior change; **self-improve while
delivering (P12)** — feed friction into gates, skills, docs and follow-on Issues
(see full Constitution). Same duty applies in `framework/ai-sdlc`.

## Mandatory workflow (load on change)

Before any code change:

1. Confirm GitHub Issue + Use Case.
2. OpenSpec Gate 1 from `docs/` (`openspec` + `bash workspace/sdlc/validate-sdlc-plan.sh`).
3. Branch `<type>/<#>_description` from updated `main`.
4. TDD — failing tests first, then implement.
5. Full suite + Playwright for UI. A PR is ready when `bash workspace/sdlc/preflight.sh --full` has passed locally. GitHub does not re-run that suite on the pull request (#2001). Before a PR, `bash workspace/sdlc/run_pipeline.sh` (security phase: `python3 security/run_gates.py --profile local`; it includes `preflight.sh --full`). A preflight run without `--full` is not sufficient to open a PR. `--full` runs the suites the branch touches; `--all` runs every suite (#2413).
6. Permanent docs + Conventional Commits + PR (`Closes #…`). Owner coverage **target >90%** across the pyramid (#2046); enforced JaCoCo/Vitest floors are raise-only and may lag — never lower without ADR; never claim 90% while floors lag.

Operational detail (load when implementing): `.claude/rules/ai-agent-workflow.md` and
`.claude/skills/ai-agent-workflow/SKILL.md`.

## On-demand rules (do not always-load)

| When touching | Load |
|---------------|------|
| Any Java/backend | `.claude/rules/programming.md`, `.claude/rules/code-quality.md`, `.claude/skills/java/SKILL.md`, `.claude/skills/backend/SKILL.md` |
| Flyway / schema | `.claude/rules/database-migrations.md`, `.claude/skills/flyway/SKILL.md` |
| Frontend / forms / theme | `.claude/rules/ui-ux-design.md`, `.claude/skills/frontend-design/SKILL.md`, vendored `.claude/skills/impeccable/SKILL.md` (pbakaus/impeccable 4.8.1, Apache-2.0) |
| Any backend/frontend structure, ports, adapters | `.claude/rules/hexagonal-architecture.md`, `.claude/skills/hexagonal-arch/SKILL.md` (ADR-030) |
| Refactors | `.claude/rules/refactoring.md` |
| CI / Docker / observability | `.claude/agents/devops-engineer.md`, `.claude/skills/devops/SKILL.md` |
| Security review | `.claude/agents/security-auditor.md`, `.claude/skills/secure-threat-modeling/SKILL.md` |
| OpenSpec propose/apply | `.claude/skills/openspec-*/SKILL.md` |
| Milestones, roadmap, lanes | `.claude/agents/project-manager.md`, `.claude/skills/project-management/SKILL.md`, `docs/300-development/305-project-management/` |

All agent fleets share the Roadmap, KANBAN, Agent queue, and derived-lane
claim protocol documented in `docs/300-development/305-project-management/`;
do not create a runtime-specific backlog or `lane:*` labels.

General hygiene (short): `.claude/rules/general.md` — load at session start if needed;
prefer skills over pasting full rule bodies into context.

## Repository knowledge (repolens)

RepoLens is the default mechanism for code knowledge and navigation in this
factory. The kit reaches it through the `[knowledge]` command (`aisdlc knowledge`
and the MCP server). It does not import the package.

Route the lookup. Do not send every question to RepoLens.

- You do not know the file or symbol: `repolens search` first. If the top hit is not usable, `rg`, then `repolens miss "<query>"`.
- You know the exact symbol, path, or string: `rg` first. A search costs about 2s here; a direct `rg` is usually under 0.2s and was as accurate or better on exact lookups in the factory loop.
- You need callers of a known symbol: `repolens impact <symbol>`. If it returns no production file, `rg` and `repolens miss "<symbol> callers"`.
- After a merge or pull, run `bash framework/repolens/bin/repolens refresh --background` if the git hooks are not installed. A query that still sees a stale index starts that same refresh and answers immediately. It does not wait for the refresh.

Sample a paired grep-vs-repolens locate every third implemented issue, and on every miss. Do not pair them on every issue. Numbers: `framework/repolens/bench/results/factory-kpis.json` and `factory-loop-comparison.md`. Health: `bash framework/repolens/bin/repolens doctor`. Usage: `… report` (#2298). Refresh benches, snapshots and factory metrics: `python3 workspace/ci/refresh-factory-evidence.py` (`--check` for staleness, #2469).

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
Hex-fleet do/don’t (#1693): `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md`
(+ `MAC-LOCAL-VS-CI.md`).

## AI SDLC framework (#1958)

Stack-agnostic kit `framework/ai-sdlc/` (Notaire profile `.aisdlc/aisdlc.toml`):
`python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml gate --list`.
Baselines only shrink (`… ratchet`); the skills catalog is linted (`… skills`).

## Context budget (#1259)

Always-loaded set must stay ≤8,000 tokens (bytes/4) per
`python3 workspace/ci/agent-context-budget.py --max-tokens 8000`.
Do not re-add bulk `@` rule imports or `alwaysApply: true` on large skills.
