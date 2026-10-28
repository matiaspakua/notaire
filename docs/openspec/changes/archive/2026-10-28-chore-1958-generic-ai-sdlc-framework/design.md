> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Evidence gathered for #1958: 2,182 commits, 954 merged PRs and 985 issues; process PRs were 35% of merged PRs in 2026-08 and 16% in 2026-10; bug issues per merged PR fell from 0.78 (2026-07) to 0.09 (2026-10). `local-ai/AUDIT.md` §4 already sketched a layered, adapter-driven kit that was never built beyond `.aisdlc/project.yml`. The Pages AI Factory section (#1934) explains the process but not how to reuse it.

## Goals / Non-Goals

**Goals:** a kit any repository can adopt with one profile and one policy document; Notaire runs it as first adopter; two prose rules become mechanical; a portfolio case study on Pages.
**Non-Goals:** replacing OpenSpec, the local-AI harness or any CI workflow; changing product code.

## Decisions

1. **Python stdlib only, TOML profile.** `tomllib` ships with Python 3.11+, CI already has Python, and zero dependencies makes the kit portable to any stack.
2. **Adapt, do not replace (P10).** Notaire's profile calls the existing scripts; the kit adds only checks that are missing (baseline ratchet, skills lint).
3. **Gate = command + exit code + expect.** Same contract as `local-ai/AUDIT.md` §4.3; the kit never trusts an agent's claim.
4. **Evidence is append-only JSONL, git-ignored by default.** Auditable locally, cheap, and adopters can choose to publish it.
5. **Ratchet compares against the base revision through `git show`.** Detects hand-added lines that the ArchUnit/ESLint tests cannot see.
6. **Report lives in `docs/300-development/306-ai-sdlc-framework/`** next to the cloud fleet docs; the kit README is the adoption guide.

## Riesgos / Trade-offs

- [`origin/main` unavailable in a shallow clone] → the ratchet fails closed (exit 2) naming the missing base revision; Process Checks checks out with `fetch-depth: 0`.
- [Skills lint fails on legacy skills] → the three skills without description gain one in this PR.
- [Pages build size] → no new dependencies; history is a committed JSON snapshot with provenance.

## Testing Strategy

`framework/ai-sdlc/tests/` (unittest, temp git repositories, no network) covers every scenario; `workspace/tests/test_aisdlc_adoption.py` covers the Notaire profile and wiring; `workspace/tests/test_pages_ai_sdlc.py` covers the new pages.

## Regression Strategy

Process self-tests (`python3 -m unittest discover` over every module), `preflight.sh --fast`, `github-page` static build.

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
