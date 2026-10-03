# Capture CI merge and CodeQL fleet learnings

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1133 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-ci-merge-learnings-69d3` |
| Gate 1 status | passed |

## Objetivo

After #1134 landed `CI-MERGE-GATE.md` and `scripts/check-heavy-ci.sh`, the
autonomous merge cascade produced further process learnings that must live in
permanent fleet docs: never merge on light-only CI (including CI subscription
“all N checks success” while heavy workflows are still pending — seen on #1137
with 18 checks), always run `scripts/check-heavy-ci.sh` before merge, rebase
stale tips before inventing Budget/person fixes, and keep CodeQL advanced setup
XOR default setup.

## What Changes

- Extend `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` with
  rebase-first stale-PR diagnosis, docs-only Playwright note, and CodeQL
  cross-link (heavy gate via `bash scripts/check-heavy-ci.sh <pr>` already present).
- Index the same learnings in `README.md`, `ENVIRONMENT-CHECKLIST.md` §7, and
  `FLEET-ARCHITECTURE.md` §9.
- Add a merge-gate section to `docs/300-development/CI-PREFLIGHT.md`.
- Document CodeQL advanced vs default setup under
  `docs/200-architecture/208-devsecops/README.md` (`wait-for-processing: false`,
  `scripts/enable-gh-secure.sh --apply`).
- Harden `.claude/agents/cloud-foreman.md` with the same hard exclusions.
- Add this OpenSpec change folder (`skip_specs: true`).

No product code and no `local-ai/` changes. Workflow YAML / `enable-gh-secure.sh`
land separately (e.g. #1136); this change documents ops rules only.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No business rule — engineering process documentation for Cloud agents (CU76 QA infrastructure). | CU76 | Made explicit |

## Capabilities

### New Capabilities

None — docs-only. `skip_specs: true` is set in `.openspec.yaml`.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — (ops docs only; codeql.yml via #1136) |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | Extend CI-MERGE-GATE + index learnings |
| `docs/300-development/CI-PREFLIGHT.md` | yes | Merge-gate section |
| `docs/200-architecture/208-devsecops/README.md` | yes | CodeQL advanced vs default setup |
| `.claude/agents/cloud-foreman.md` | yes | Hard exclusions for heavy gate / rebase / CodeQL |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No application-architecture change. No ADR. Documentation only; Cloud path still
excludes `local-ai/`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Rebase-first, docs-only Playwright, CodeQL link |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Process learnings one-liner |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | §7 rows: heavy gate, stale tip, CodeQL |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | §9 bullets + related links |
| `docs/300-development/CI-PREFLIGHT.md` | Merge-gate section → `check-heavy-ci.sh` |
| `docs/200-architecture/208-devsecops/README.md` | CodeQL advanced vs default setup |
| `docs/300-development/README.md` | Index link to CI-MERGE-GATE |
| `.claude/agents/cloud-foreman.md` | Hard exclusions |
| `CHANGELOG.md` | n/a — not user-visible |
