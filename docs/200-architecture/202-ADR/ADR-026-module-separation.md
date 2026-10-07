# ADR-026: Module Separation Inside the Repository (Phase 1 of #1197)

**Status:** Accepted
**Date:** 2026-10-06
**Deciders:** Owner (issue #1292 / CU76)
**Related:** ADR-024 (repository topology), #1197, REPO-SPLIT-PLAN

## Context

The Owner's direction is granular repositories, each with one responsibility, unified by a Foreman
agent through explicit contracts, so independent agent fleets (security, infrastructure, knowledge
base, QA) can work in isolation. ADR-024 gates 1 and 2 require, per area, a standalone guard and
documented seams before any extraction. This ADR delivers them as folders first.

## Decision

1. **Modules** (SRP, one fleet each): `backend-api`, `frontend`, `infra`, `testing`, `local-ai`,
   `docs` (knowledge base) and `workspace` (the unifier). `security/` and `contracts/` follow in
   later slices. `backend-api` and `frontend` keep their names: they are already single-purpose and
   a rename touches the Maven module, Dockerfiles, compose files, 17 workflows and the doc links.
2. **Manifest:** `workspace/modules.yaml` is the only file the Foreman needs to find a module's
   path, responsibility, fleet, verify command and dependencies. `tests/test_modules_manifest.py`
   fails on a missing `MODULE.md` or `verify.sh`, an unknown dependency or a cycle.
3. **Module contract:** `MODULE.md` states purpose, the contract others may rely on, the seams the
   module reads and what it must not do. `verify.sh` runs that module's build, test, format and lint
   only, from any directory.
4. **Rule:** a module reads another only through its contract (`contracts/`, slice 2); only
   `workspace` reads all of them.
5. **Delivery:** one PR per slice (manifest, contracts, security, guard relocation, docs
   generators, OpenSpec into `docs/`, Foreman integration), each keeping `workspace/sdlc/preflight.sh`
   green; the full suite including Playwright E2E runs at the end. Code nothing needs any more moves
   to `deprecated/`, never deleted; the Owner decides later.
6. **OpenSpec location:** the OpenSpec CLI hard-codes the folder name `openspec`, so it moves to
   `docs/openspec/` and `docs/` becomes the OpenSpec root. Verified on a copy: from `docs/` the
   CLI lists changes and statuses; from the repository root it finds nothing. Every script, hook
   and agent instruction therefore runs `openspec` with `docs/` as the working directory.

## Consequences

- Extraction (Phase 2) becomes `git filter-repo` of one folder plus a pin in the manifest.
- Cross-area guards stay in `workspace/` until their areas are parameterised.
- Moving OpenSpec is the last slice because it touches every workflow, hook and script that
  names `docs/openspec/`.

## Navigation

- [ADR-024](ADR-024-repository-topology.md)
- [Plan](../../300-development/REPO-SPLIT-PLAN.md)
- [ADR index](README.md)
