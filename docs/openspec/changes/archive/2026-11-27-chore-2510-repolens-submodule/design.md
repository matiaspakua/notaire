> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Product exported with git subtree split (full framework/repolens history) to https://github.com/draclab/repolens (private, MIT). Issue #2510.

## Goals / Non-Goals

**Goals:** One product home for RepoLens; Notaire submodule at the same path; local and CI checkouts can initialize the private submodule.
**Non-Goals:** Public release, PyPI, API changes, relocating adopter benches.

## Decisions

1. **git submodule at framework/repolens.** Same path keeps modules.yaml, preflight and harness wiring stable.
2. **HTTPS submodule URL + read-only deploy key for CI.** Developers keep HTTPS; Actions uses an SSH deploy key secret to fetch the private product.
3. **OpenSpec capability stays code-knowledge-graph.** Packaging change, not a new retrieval behaviour.

## Riesgos / Trade-offs

- [Private submodule empty in CI] → composite checkout + REPOLENS_SUBMODULE_SSH_KEY deploy key.
- [Clone without --recurse-submodules] → README and AGENTS document `submodule update --init`.

## Testing Strategy

`RepolensHarnessWiringTest` / `RepolensFactoryProfileTest` plus a submodule `.gitmodules` assertion; `bash framework/repolens/verify.sh` after init.

## Regression Strategy

python3 -m unittest workspace.tests.test_repolens_integration; bash framework/repolens/verify.sh

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
