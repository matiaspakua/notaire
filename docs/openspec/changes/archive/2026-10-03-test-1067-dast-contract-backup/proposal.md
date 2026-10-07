# Add DAST, API contract, and backup/restore tests

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1067 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure; CU78 – Security and Compliance; CU75 – Database Management and Migrations |
| Branch | `cursor/test-1067-dast-contract-backup-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue ahead of this pack |

## Objetivo

Production-readiness audit (2026-09) found three quality gaps still open: no
dynamic security scan of the running app (Trivy only), no OpenAPI contract
diff/gating on PRs, and no backup→restore verification (backup productization
itself is #256, still open). This change adds phased CI/test infrastructure for
OWASP ZAP baseline, committed OpenAPI with PR diff, and a gated
backup→restore→smoke path once #256 lands — without pretending #256 is done.

## What Changes

- **Phase A — DAST:** OWASP ZAP baseline (or equivalent baseline API scan)
  against the compose stack in CI; document runbook (ties to #281 guide intent);
  fail or warn policy decided in design (prefer fail on CRITICAL/HIGH after
  initial baseline allowlist if needed).
- **Phase B — API contract:** Commit a generated/exported OpenAPI artifact from
  springdoc; diff it on PRs (breaking-change detection). Schemathesis/Pact are
  optional stretch if timebox allows — OpenAPI commit+diff is the AC minimum.
- **Phase C — Backup/restore tests:** Scheduled or CI workflow that runs
  backup→restore→smoke **only after** #256 provides an automated backup
  mechanism; until then, ship a skipped/gated job + docs that unblock when #256
  merges (do not invent a parallel backup product).
- Update DevSecOps / testing permanent docs + CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Running API MUST be subject to a regular OWASP ZAP (or equivalent) baseline DAST in CI | CU78; ADR-006; #1067 AC; #281 | New (automation) |
| OpenAPI contract MUST be committed and diffed on PRs to catch breaking API changes | CU76; #1067 AC | New |
| Backup→restore→smoke MUST verify restoreability once automated backups exist (#256) | CU75; #1067 AC | New (gated on #256) |
| Trivy SCA remains; DAST complements it and does not replace it | CU78; existing ci.yml | Unchanged |

## Capabilities

### New Capabilities

- `dast-api-contract-backup-tests`: CI/test infrastructure for ZAP baseline
  DAST, OpenAPI commit+PR diff, and gated backup→restore→smoke verification.

### Modified Capabilities

- (none existing under `openspec/specs/` for these infra gates)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (light) | OpenAPI export script/config if needed; no product behavior change required |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | maybe | Compose networking notes for ZAP target; backup scripts only via #256 |
| CI/CD (`.github/workflows`) | yes | New/extended jobs: ZAP, OpenAPI diff, gated backup-restore |

### Surface area

- Entities: none
- Endpoints: scanned/diffed existing API; no intentional contract change
- Database (Flyway `V{n}`): none in this change; restore uses #256 artifacts
- Configuration / `.env`: may add non-secret ZAP/OpenAPI path knobs to
  `.env.example` if required
- Dependencies: CI actions (ZAP, openapi-diff / similar); no runtime Maven deps
  required beyond springdoc already present

### Architecture review

Aligns with ADR-006 (ZAP weekly/CI) and DevSecOps README backlog item “Add
OWASP ZAP”. Not a new product architecture — ADR optional only if choosing a
non-ZAP DAST or rewriting backup strategy (prefer no new ADR; cite ADR-006).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/208-devsecops/README.md` | Record ZAP + OpenAPI diff as implemented (not future-only) |
| `docs/300-development/303-testing/TEST-PLAN.md` | Add DAST / contract / backup-restore levels |
| `docs/200-architecture/209-deployment/README.md` | Link backup-restore verification gated on #256 |
| #281 guide (if still open) | Either land minimal ZAP guide here or explicitly keep #281 for prose guide while CI lands in #1067 |
| `CHANGELOG.md` | Infra/security testing entries |

## Out of Scope

- **#256** — implementing automated PostgreSQL backups (dependency for Phase C
  execution; this change only gates/tests)
- Full Schemathesis/Pact matrix (stretch; OpenAPI diff is the AC bar)
- Remediating every ZAP finding in the same PR (findings → Issues; baseline
  policy may allowlist initially with tracked follow-ups)
- Replacing Trivy
