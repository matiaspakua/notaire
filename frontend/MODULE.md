# frontend

**Purpose:** Next.js web client: UI, client-side validation, API calls.

**Verify:** `bash frontend/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- The pages and routes users see, exercised by `testing/e2e`
- Image `notaire-frontend`

## Seams (what this module reads from outside)

- The backend OpenAPI contract and `/api/v1` over HTTP (never Java classes)

## Must not

- Contain SQL, JDBC or business rules
- Import from `backend-api` sources

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
