> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`docs/200-architecture/206-security/README.md` contains an informal risk
register that self-flags as not a formal STRIDE exercise. No `SR-*` IDs,
trust-boundary diagram, or per-threat mitigation/status table exist
anywhere in the repo, despite CONSTITUTION.md §5 listing
`secure-threat-modeling` as a first-class part of the SDLC. This change is
documentation-only: it produces the missing artifact for the two
highest-risk subsystems (login/auth, audit trail).

## Goals / Non-Goals

**Goals:**
- Produce a real STRIDE threat model for login/auth and the audit trail.
- Assign `SR-*` IDs to security requirements so future changes can
  reference and test against them.
- Explicitly mark each threat's status (Mitigated / Partial / Open) instead
  of leaving gaps implicit in prose.

**Non-Goals:**
- Implementing fixes for "Open" items (e.g., distributed rate limiting,
  GET-request auditing) — those are follow-up issues.
- Covering every subsystem (Escritura, Presupuesto) — out of scope, tracked
  as follow-up in issue #1028.

## Decisions

- **Scope to auth + audit-trail, not the whole system**: these are the two
  subsystems already flagged as highest-risk in the existing informal
  register (credential/PII exposure, audit-trail tampering) and have a
  bounded, well-understood surface (`LoginAttemptService`,
  `AuditoriaAspect`, `RegistroAuditoriaService`), making a first threat
  model tractable to review rather than another aspirational wall of text.
- **STRIDE over LINDDUN**: STRIDE fits the technical attack surface here
  (spoofing/tampering/DoS on login, tampering/repudiation on the audit
  trail); LINDDUN (privacy-focused) is a better fit for a future PII-data
  model of `Persona`, deferred as a follow-up.
- **Format as Markdown + embedded PlantUML** (existing pattern in
  `docs/200-architecture/204-diagrams/`), not a new tool — keeps it in the
  same review/diff workflow as everything else in `docs/`.

## Riesgos / Trade-offs

- [Scope creep into a full-system threat model, stalling the PR] →
  Mitigation: hard-scoped to two subsystems in the proposal; other
  subsystems explicitly deferred as follow-up issues.
- [Threat model becomes stale as code changes] → Mitigation: `SR-*` IDs
  are designed to be referenced from future PRs touching
  `LoginAttemptService` / `AuditoriaAspect`, making drift visible in review
  rather than silent.
- [Document says "Open" for known gaps but nothing forces fixing them] →
  Mitigation: each "Open" item is cross-referenced to a follow-up
  recommendation in issue #1028, so it is tracked, not just written down.

## Testing Strategy

n/a — this is a documentation artifact, not executable behavior. No
Acceptance Criteria / scenarios are introduced (`skip_specs: true`).
Validation is via `bash scripts/validate-sdlc-plan.sh` (plan completeness)
and manual technical review of the threat-model content in PR review.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| n/a - no spec deltas (skip_specs: true) | — | — |

## Regression Strategy

- Existing tests affected: none — no code changes.
- Full suite command: `mvn verify -pl backend-api` (run to confirm no
  incidental drift from repo state, not because this change touches code).
- Legacy paths at risk: none.

## Playwright Strategy

n/a - no UI surface. This change adds one Markdown document and edits
another; no frontend behavior changes.

## Deployment Strategy

- Flyway migration required: no.
- Deployment order / coupling: none — documentation merges independently.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): n/a — no runtime behavior change.

## Rollback Strategy

- Revert safe: yes — a plain `git revert` of the doc commit is safe, no
  downstream state depends on the document's existence.
- Database rollback: none needed.
- Data written under the new behavior after revert: none.
- Blast radius if rollback is delayed: none — worst case is the gap
  documented in issue #1028 remains open longer.

## Migration Plan

n/a — single-step documentation change, no staged rollout.
