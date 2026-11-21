# Harness report refuses unknown adapter names

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2385 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2385_harness_report_unknown_adapter` |
| Gate 1 status | pending |

## Objetivo

`aisdlc harness report --run <id> --event <event> --adapter <name>` accepts any adapter name, even one `aisdlc harness list` does not return. This allows fabricated adapter ids to be recorded in the evidence ledger, undermining the hash-chain honesty guarantee.

## What Changes

`harness report` validates the `--adapter` argument against the list returned by `harness.list_adapters()`. When the adapter id is not found, the command exits 2 with a `KitError` (code `unknown_adapter`) that names the known adapter ids and writes no ledger record. A known adapter continues to work unchanged.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Harness report refuses adapter ids not returned by harness list | #2385 | New |

## Capabilities

### New Capabilities

### Modified Capabilities

- `harness-port`: harness report must validate adapter id against the list before writing a ledger record.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `framework/ai-sdlc/aisdlc/**`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/docs/HARNESS-REPORTING.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/fix-2385-harness-report-unknown-adapter/**`

### Architecture review

No architecture change. Adds a validation guard to an existing CLI command path.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/docs/HARNESS-REPORTING.md` | State the rule: harness report refuses unknown adapter ids (exit 2, `unknown_adapter`) |
| `framework/ai-sdlc/CHANGELOG.md` | Fix entry for #2385 |

## Out of Scope

- Registering adapters.
- Changing `harness list` output.
