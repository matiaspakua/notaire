> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

OpenSpec 1.14 resolves project schemas only under a directory named `openspec` relative to the resolved root. Notaire relocated that tree to `docs/openspec/` (ADR-026 / #1292). From the repo root the CLI reports `no_openspec_root` and only packaged `spec-driven`. `seed-openspec-change.sh --create` already cds into `docs/`.

## Goals / Non-Goals

**Goals:** Unblock agents with the smallest honest fix: document and skill-point the proven cwd/seed path; lock it with a unittest.
**Non-Goals:** Root symlink; Constitution Gate 1 changes; inventing a second schema tree; #2221 labels; product code.

## Decisions

1. Document + skill pointer instead of registering a root `openspec/` path — ADR-026 and `test_no_openspec_folder_at_the_repository_root` forbid a root folder; a symlink would fight that guard for little gain.
2. Prefer `seed-openspec-change.sh --create` as the agent-facing command because it already wraps `cd docs && openspec new change --schema notaire-sdlc` and copies templates.
3. Keep vendor `openspec-propose` edits minimal (cwd note) knowing `openspec update` may refresh vendor skills; durable text also lives in ai-agent-workflow + NOTAIRE-ADAPTATIONS.

## Riesgos / Trade-offs

Vendor skill overwrite on `openspec update` — mitigate by also documenting in project-owned ai-agent-workflow and NOTAIRE-ADAPTATIONS. Agents still running openspec from repo root will fail until they follow the pointer — that failure is the honesty we want.

## Testing Strategy

Extend `workspace/tests/test_openspec_location.py`: assert `openspec schemas --json` from `docs/` includes `notaire-sdlc`; keep root-folder absence; optional assertion that skill/doc strings mention `seed-openspec-change.sh` / `cd docs`. Observe RED before doc/skill edits if assertions are added first.

## Regression Strategy

Existing location tests and validate-sdlc-plan / seed self-tests must stay green. No backend/frontend surface.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs/skills/tests only; no runtime deploy.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
