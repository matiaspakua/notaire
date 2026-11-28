> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`infra/` is standalone-ready (#1179). `draclab/ai_sdlc` already holds the kit at
`aisdlc-v1.1.0` while Notaire still vendors 1.0.0. Issue #2512.

## Goals / Non-Goals

**Goals:** Publish `matiaspakua/infrastructure` with filtered history and a
multi-system contract; Notaire submodule at `infra/` plus `ops/system.yaml`;
pin `framework/ai-sdlc` to `aisdlc-v1.1.0`; update CI, docs and Pages.

**Non-Goals:** Full multi-tenant Sonar; external alert routing; other repo
extractions; public ai_sdlc.

## Decisions

1. **git submodule at `infra/`** pointing at `matiaspakua/infrastructure` (public).
   Same path keeps modules.yaml, stack scripts and most CI paths stable (vs
   `NOTAIRE_INFRA_DIR` stub-only from REPO-SPLIT-PLAN).
2. **System descriptor in the adopter** (`ops/system.yaml`) with
   `SYSTEM_DESCRIPTOR` consumed by infra `render-system` / start scripts.
3. **git submodule at `framework/ai-sdlc`** pinned to tag `aisdlc-v1.1.0`
   (private; same PAT/SSH pattern as RepoLens when that lands; until then token
   covers `draclab/*`).
4. **Kit release ownership** moves fully to `draclab/ai_sdlc`; Notaire
   `aisdlc-kit.yml` verifies only.
5. **filter-repo extract** on a fresh clone, then genericization commits on
   infrastructure `main`.

## Riesgos / Trade-offs

- [Private ai_sdlc empty in CI] → extend checkout-with-submodules token/SSH for `draclab/ai_sdlc`.
- [Infra tests hardcode notaire-*] → fixture descriptor equivalent to current Notaire; gradual rename.
- [Clone without recurse] → README/AGENTS document `submodule update --init --recursive`.
- [No LICENSE in Notaire] → add LICENSE to infrastructure product repo for the public extract.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| gitmodules points at infrastructure | unit | `workspace/tests/test_infra_submodule.py` |
| system descriptor matches app network/containers | unit | `workspace/tests/test_system_descriptor_seam.py` |
| gitmodules points at ai_sdlc release | unit | `workspace/tests/test_aisdlc_adoption.py` (extended) |
| module verify paths still work | unit/module | `infra/verify.sh`, `framework/ai-sdlc/verify.sh` |

- New unit tests: Python under `workspace/tests/`
- New integration tests: n/a
- Coverage impact: n/a (Python harness)

## Regression Strategy

- Existing tests affected: `test_aisdlc_adoption.py`, infra standalone tests (move with product), pages tests that exec `framework/ai-sdlc/bin/aisdlc`, image-pin tests that read `infra/observability/docker-compose.yml`.
- Update path/version expectations without weakening assertions.

## Playwright Strategy

n/a — no UI surface in the product app (GitHub Pages copy updates only).

## Deployment Strategy

Ships with the next Notaire release after submodule init is documented; infrastructure and kit already have their own remotes.

## Rollback Strategy

Tag `pre-extract-infra` on Notaire; restore vendored trees from that tag and archive/pin previous infrastructure SHA if needed.

## Migration Plan

1. Push infrastructure extract + genericization.
2. Replace Notaire `infra/` and `framework/ai-sdlc/` with submodules; add `ops/system.yaml`.
3. Developers: `git submodule update --init --recursive`.

## Open Questions

None.
