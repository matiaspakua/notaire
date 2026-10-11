# Canonical domain lifecycle and engineering method

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #921 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/lifecycle-engineering-docs-853b` |
| Gate 1 status | passed |

## Objetivo

The domain lifecycle was described in three places that disagreed (seeded workflow,
legacy activity diagram, and the services), and the engineering method from requirement
to operation had no single process document. This change records both, as one slice of
the documentation audit in #921. It does not close that audit.

## What Changes

- Add `docs/200-architecture/203-design/CICLO-DE-VIDA.md`: value stream, grouped use-case
  map, state machines, sequences, component view, traceability, and work-item versus
  domain state.
- Add `docs/300-development/INGENIERIA.md`: artifact ids, bidirectional traceability,
  the delivery boxes, QA and DevSecOps by design, and the 12-factor appraisal.
- Point the business, architecture, SAD, ADR-014, diagram, development, AI SDLC, and
  delivery-board indexes at those files.
- Record the OpenSpec change for this pull request, and make Process Checks diff the
  merge parent instead of a stale event base SHA. A path that landed on `main` after
  the PR opened was being counted as this PR's OpenSpec change.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A management moves only along the seeded workflow; archiving is allowed from Documentación completa | CU83, V10/V41 | Made explicit |
| Signing a deed does not by itself move the management | CU16, `deeds.status` | Made explicit |
| Testimony, registration draft, submitted document, budget, and payment each have their own state, separate from the management node | CU85, CU06, CU10, CU72, CU15, CU47 | Made explicit |
| A pull request without its own OpenSpec change needs a human `sdlc-exception` label | Constitution §12, CU76 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`; this change documents behaviour that already exists)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `docs` | yes | Lifecycle and engineering method, plus indexes |
| CI/CD (`.github/workflows`) | yes | `sdlc-process.yml` resolves the diff base from the merge commit |
| `workspace` | yes | `check-sdlc-exception.sh` and its unit test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No new ADR. ADR-014 already owns the workflow engine; this change points it at the
lifecycle document instead of adding a second diagram. ADR-027 (Mermaid) is followed.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/CICLO-DE-VIDA.md` | New canonical lifecycle |
| `docs/300-development/INGENIERIA.md` | New engineering method |
| SAD, ADR-014, and the indexes listed in the change | Link the canonical files |
| `CHANGELOG.md` | Note the Process Checks base fix under Unreleased |

## Out of Scope

The rest of #921: a full inventory of every document, security and SRE completeness,
and the prioritized documentation roadmap. Those stay on the open issue.
