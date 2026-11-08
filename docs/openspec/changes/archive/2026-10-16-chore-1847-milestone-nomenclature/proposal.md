# Version-first milestone nomenclature

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1847 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1847_milestone_nomenclature` |
| Gate 1 status | passed |

## Objetivo

Milestone titles were dated (`2026-11 · Release blockers`) while releases are
semver, so no one could tell which milestone a cut advances. The Owner
approved a version-first nomenclature on 2026-10-16 so the board tells the
project's history, current train and future releases in one reading.

## What Changes

- Open trains become `vX.Y · <Theme> (<YYYY-MM>)`: v0.3 … v0.6 and v1.0.
- History becomes `H0`…`H8` stages plus `H ·` close-date cohorts. The three
  priority buckets merge into `H8`. A new, empty `H0 · Swing monolith`.
- Shipped cuts get closed `v0.1` and `v0.2` trains; closed issues inside a cut
  window move there.
- `Backlog · Unscheduled` and `Catalog · Requirements & use cases` are
  continuous, with no due date; RF/RNF/CU anchors move to Catalog.
- `jpa` retirement (#571–#580, #584, #595) moves into v0.3.
- **BREAKING (tooling):** `train_for()` now reads the `vX.Y · ` prefix. `0.2.z`
  maps to v0.2, not to the old release-blocker train.
- The apply script also syncs descriptions and due dates, creates closed
  milestones, and merges renamed buckets.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| One milestone per minor; every patch advances it | Owner decision #1847 | Changed |
| History milestones are `H<n> ·` or `H ·` cohorts, closed | Owner decision #1847 | Changed |
| Closed issues in a cut window belong to that shipped train | Owner decision #1847 | New |
| Catalog anchors live on Catalog, never on a train | Owner decision #1847, RELEASES.md | Changed |
| Backlog and Catalog have no due date | Owner decision #1847 | New |

## Capabilities

### New Capabilities

- `milestone-nomenclature`: titles, states, due dates and descriptions of every milestone.

### Modified Capabilities

- `product-release`: a semver maps to the train with its `vX.Y · ` prefix.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github/workflows`) | no | – |
| `workspace` | yes | `milestone_policy.py`, `apply_milestone_policy.py`, `product_release.py` and their tests |
| `docs` | yes | project-management policy, constitution §11, SAD, ADR-028, SBOM, snapshots |
| `github-page` | yes | roadmap diagram and history grouping by `H` prefix; `releases.json` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Process-only change in the existing policy/apply split (pure policy, one
GitHub-facing script). ADR-028 is amended in place for rule 4; no new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md` | new: patterns, rules, template, map |
| `docs/300-development/305-project-management/MILESTONES.md` | trains, history, precedence |
| `docs/300-development/305-project-management/RELEASES.md` | version → train rule |
| `docs/300-development/305-project-management/README.md` | index entry |
| `CONSTITUTION.md` §11 | milestone sentence |
| `docs/200-architecture/201-SAD/sad.md`, ADR-028, `docs/300-development/SBOM.md`, `docs/github/README.md` | titles |
| `CHANGELOG.md` | Unreleased entry |

## Out of Scope

- Applying the renames on GitHub (Owner-run script after merge; the agent token cannot edit milestones).
- Board views, labels and Status: the next steps of #1847's parent effort.
