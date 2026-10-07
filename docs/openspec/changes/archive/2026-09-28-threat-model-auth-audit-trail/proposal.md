# Structured STRIDE threat model for authentication & audit-trail subsystem

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1028 |
| Use Case | CU84 (Login), CU73 (Registro de Auditoría) |
| Branch | `docs/1028_threat_model_auth_audit` |
| Gate 1 status | pending |

## Objetivo

The `secure-threat-modeling` skill is listed in CONSTITUTION.md §5 as a
first-class part of the AI SDLC, but an audit (issue #1028) found no
structured threat-model artifact anywhere in the repo. The existing
`docs/200-architecture/206-security/README.md` self-describes its own
"Data Classification & Threat Model" section as *"a lightweight risk
register, not a formal STRIDE exercise."* This change closes that gap for
the two highest-risk subsystems — authentication (login/lockout) and the
audit trail (`RegistroAuditoriaService`) — by producing a real STRIDE-based
threat model with numbered security requirements (`SR-*`), trust
boundaries, and a per-threat mitigation/owner/status table.

## What Changes

- Add `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md`: a
  STRIDE threat model covering the login/lockout flow (`LoginAttemptService`,
  JWT issuance) and the audit-trail flow (`AuditoriaAspect`,
  `RegistroAuditoriaService`), with a trust-boundary diagram (PlantUML) and a
  table of `SR-01`..`SR-NN` security requirements, each with a STRIDE
  category, current mitigation, and status (Mitigated / Partial / Open).
- Update `docs/200-architecture/206-security/README.md`'s "Data
  Classification & Threat Model" section to link to the new document instead
  of duplicating risk content, and to drop the "not a formal STRIDE exercise"
  caveat now that one exists for these two subsystems.
- No code or spec-level behavior changes — this is a documentation artifact
  formalizing threats and mitigations that already exist in code (or
  explicitly flagging ones that don't, e.g. no per-role authorization, GETs
  not audited).

## Reglas de negocio

This change introduces no new business rules and does not alter existing
ones. It formalizes, with `SR-*` IDs, security constraints already implied
by CU84 (login lockout behavior) and CU73 (audit trail integrity), and
makes explicit two gaps already known in code/docs but not formally tracked:
brute-force lockout is single-instance/in-memory only, and read (GET)
access to PII is not audited.

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Login lockout after repeated failed attempts | CU84, `LoginAttemptService` | Made explicit (SR-01) |
| Audit trail records acting user from JWT identity, never client-supplied header | CU73, `AuditoriaAspect` | Made explicit (SR-05) |

## Capabilities

### New Capabilities
None — this is a documentation-only change with no spec-level behavior
change. `skip_specs: true` is set in `.openspec.yaml`.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — (removed) |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none changed.
- Endpoints: none changed.
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: none.
- Dependencies: none.

### Architecture review

Purely additive documentation under the existing
`docs/200-architecture/206-security/` directory, following the pattern of
the sibling `SQL-INJECTION-PREVENTION.md` / `INPUT-VALIDATION-STRATEGY.md`
documents. No ADR required — this does not change architecture, it
documents risk against the existing one.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | New file: full STRIDE threat model for auth + audit-trail. |
| `docs/200-architecture/206-security/README.md` | Link to the new threat model, remove the "not a formal STRIDE exercise" caveat for these two subsystems. |
| `CHANGELOG.md` | Entry noting the new threat-model doc (not user-visible, but process-visible). |

## Out of Scope

- Threat models for other subsystems (Escritura/Testimonio legal documents,
  Presupuesto financial data) — tracked as a follow-up recommendation in
  issue #1028.
- Implementing the mitigations for gaps the threat model surfaces as "Open"
  (e.g., distributed rate limiting, GET auditing) — those become their own
  issues once triaged, per the Explore → Issue → Propose flow.
- Observability/runbook gaps found in the same audit (issue #1028) — tracked
  as follow-up, not part of this change.
