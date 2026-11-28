> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`security/` and `testing/` are still vendored. Empty destination repos exist.
Issue #2514. Hybrid C and docs option 3 are Owner decisions from the extract plan.

## Goals / Non-Goals

**Goals:** Publish `notaire-security` (submodule + program docs + fleet contract)
and `notaire-testing` (reusable workflows + suites); wire Notaire callers/pins;
document four-fleet MULTI-TEAM-CONTRACT; keep threat models in product.

**Non-Goals:** Bruno/pg-integration/unit move; CodeQL/Trivy relocation; backend/
frontend split; bulk issue transfer.

## Decisions

1. **git submodule at `security/`** → `matiaspakua/notaire-security` (same path keeps
   `modules.yaml`, `run_gates.py` and CI intact).
2. **Docs option 3:** program/procedure docs move to the security repo; STRIDE/
   threat-model/diagrams/auth-SQLi guides stay under `206-security/`.
3. **Testing via reusable workflows** (not a content submodule): Notaire callers
   `uses: matiaspakua/notaire-testing/.github/workflows/<name>.yml@<sha>` with
   `app-ref`; stub `testing/README.md` points at the remote.
4. **Seam env vars** `NOTAIRE_APP_ROOT` / `NOTAIRE_TESTING_ROOT` for migrations,
   coverage report and CU-API matrix tooling.
5. **filter-repo** on fresh clones; rollback tags `pre-extract-security` and
   `pre-extract-testing`.

## Riesgos / Trade-offs

- [Private security submodule empty in CI] → extend checkout-with-submodules (same
  token pattern as ai-sdlc/repolens if the security repo stays private; public
  remotes init anonymously).
- [Workflow pin drift] → COMPATIBILITY.md in testing repo; pin bump is a deliberate
  Notaire PR.
- [Broken catalog links after doc move] → update catalog.yml and leave stubs/links
  in 206-security README.
- [Clone without recurse] → README/AGENTS document `submodule update --init --recursive`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| gitmodules points at notaire-security | unit | `workspace/tests/test_security_submodule.py` |
| module verify runs from security path | unit/module | `security/verify.sh` + submodule test |
| threat-model paths remain in product | unit | `workspace/tests/test_security_docs_seam.py` |
| program docs live in security repo (or stub points there) | unit | `workspace/tests/test_security_docs_seam.py` |
| Notaire e2e/database workflows pin notaire-testing | unit | `workspace/tests/test_testing_workflow_pin.py` |
| testing tree is stub-only locally | unit | `workspace/tests/test_testing_workflow_pin.py` |
| NOTAIRE_*_ROOT seams resolve | unit | `workspace/tests/test_testing_roots_seam.py` |

- New unit tests: Python under `workspace/tests/`
- New integration tests: n/a
- Coverage impact: n/a (Python harness)

## Regression Strategy

- Existing tests affected: any that assume a vendored `testing/` tree or absolute
  paths into moved program docs; preflight/CU-API matrix scripts.
- Update path/root expectations without weakening assertions.

## Playwright Strategy

n/a — no product UI surface change in this PR (E2E caller becomes a pin; suites
move to the testing repo). Record "n/a — topology/packaging only".

## Deployment Strategy

Ships with the next Notaire release after submodule init and workflow pin are
documented; satellite repos have their own remotes and CI.

## Rollback Strategy

Tags `pre-extract-security` and `pre-extract-testing` on Notaire; restore vendored
trees from those tags and drop submodule/pins if needed.

## Migration Plan

1. Push notaire-security extract + program docs + contract + CI.
2. Replace Notaire `security/` with submodule; slim 206-security; update catalog.
3. Push notaire-testing extract + reusable workflows + COMPATIBILITY.
4. Replace Notaire `testing/` with stub; pin caller workflows; parameterize roots.
5. MULTI-TEAM-CONTRACT + docs/Pages; developers: `git submodule update --init --recursive`.

## Open Questions

None.
