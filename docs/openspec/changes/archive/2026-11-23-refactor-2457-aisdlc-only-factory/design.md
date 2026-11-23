> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

aisdlc ships its contract as `openapi/factory-control.openapi.yaml` and its dashboard says "Factory Control" in the title, i18n and docs. The external Factory Control (`~/workspace/factory-control`) is a different product. agentic-os, gentle-ai and Factory Control are cited as the source of several rules (protected paths, write set, evidence chain) and compared in `CROSS-ASSESSMENT-2026-11.md`.

## Goals / Non-Goals

**Goals:** One factory in Notaire: aisdlc. No name collision between the kit and the external product. A guard keeps it that way.
**Non-Goals:** Changing any HTTP path, schema or behaviour of the kit. Renaming GitHub milestones.

## Decisions

Brand: **AI SDLC Factory** (Owner, 2026-11-23). Contract file: `aisdlc-factory.openapi.yaml`; the HTTP surface is unchanged, so the contract version stays and the kit CHANGELOG records the renamed packaged path. Rules keep their content and lose the external citation: the rule stands by its own reason. The assessment is history, so it moves to `docs/000-archive/` (CONSTITUTION §8). The guard skips `docs/000-archive/`, archived OpenSpec changes and `CHANGELOG.md` (released history).

## Riesgos / Trade-offs

An adopter that reads the packaged contract by its old path breaks: mitigated by the CHANGELOG entry; `aisdlc contract` resolves the path for callers. Missed occurrences: the guard test catches them.

## Testing Strategy

Red first: `workspace/tests/test_no_external_factory.py` (no tracked file names agentic-os or `factory-control`), kit tests for the contract path (`test_contract.py`) and the packaged file (`test_packaging.py`), dashboard unit test for the brand. Then `bash framework/ai-sdlc/verify.sh`, `bash framework/ai-sdlc/factory-dashboard/verify.sh`, `python3 workspace/sdlc/check-factory-boundaries.py`.

## Regression Strategy

`aisdlc contract check`, `aisdlc serve` contract tests, dashboard unit and e2e suites, the factory boundary copy check.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
