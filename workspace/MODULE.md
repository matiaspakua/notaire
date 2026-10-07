# workspace

**Purpose:** The unifier: the module manifest and the guards that read more than one module.

**Verify:** `bash workspace/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- `workspace/modules.yaml`, read by the Foreman and by the manifest guard

## Seams (what this module reads from outside)

- Every other module, through its `verify.sh` and its contract files

## Must not

- Hold product code or per-module guards

Manifest entry: [`workspace/modules.yaml`](modules.yaml). Rationale: ADR-026.
