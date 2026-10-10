# contracts

**Purpose:** the single list of seams between modules, so each module can change independently and a rename on one side fails a guard instead of production.

**Verify:** `bash contracts/verify.sh`.

## Contract (what other modules may rely on)

- [`seams.yaml`](seams.yaml): per seam, the provider, the consumers, the names both sides keep and the files that rely on them.
- The backend OpenAPI document `backend-api/openapi/openapi.yaml` is the HTTP contract (see seam `http-api`).
- [`api-reachability-allowlist.yaml`](api-reachability-allowlist.yaml): endpoints of that contract the frontend does not call, each with a reason. `tests/test_api_reachability.py` fails on a new endpoint without a UI consumer and on stale entries (issue #1250, CONSTITUTION section 4).

## Seams (what this module reads from outside)

- Every evidence file named in `seams.yaml`, read-only, by the guard.
- `backend-api/openapi/openapi.yaml` and the `frontend/src` sources (tests and `*.generated.ts`
  excluded), read-only, by the reachability guard.

## Must not

- Hold code or copies of another module's files.
- Be bypassed: a new dependency between modules is added to `seams.yaml` in the same PR.

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
