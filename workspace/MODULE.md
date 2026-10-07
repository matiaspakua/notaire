# workspace

**Purpose:** The unifier: the module manifest and the guards that read more than one module.

**Verify:** `bash workspace/verify.sh` (module checks only; the full gate is `bash workspace/sdlc/preflight.sh`).

## Contract (what other modules may rely on)

- `workspace/modules.yaml`, read by the Foreman through `python3 workspace/modules.py` (`list`, `affected <path>...`, `verify <module>|--all`) and by the manifest guard
- `workspace/stack/`: run the whole system (`start.sh`, `stop.sh`, `logs.sh`, `start-all.sh`, `setup-pgadmin.sh`); each anchors itself to the repository root
- `workspace/sdlc/`: the SDLC gates (`preflight.sh`, `run_pipeline.sh`, `validate-sdlc-plan.sh`, the `check-*` gates, `seed-openspec-change.sh`, `install-git-hooks.sh`, `validate-cu-api-matrix.py`); `workspace/ci/`: the CI report generators
- Its operational tooling: `scripts/` (preflight, run_pipeline, CI guards that read workflows) and `.github/` (workflows), declared as `extra_paths` in the manifest

## Seams (what this module reads from outside)

- Every other module, through its `verify.sh` and its contract files

## Must not

- Hold product code or per-module guards

Manifest entry: [`workspace/modules.yaml`](modules.yaml). Rationale: ADR-026.
