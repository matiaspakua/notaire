# Milestones

Every GitHub issue has exactly one milestone. A milestone is either a closed
history cohort or an open delivery train. It is not a SemVer release; product
cuts are defined in [RELEASES.md](RELEASES.md).

The strings below are canonical. Generated counts belong in
`docs/github/milestone-snapshot.json`, not in this policy.

Titles follow [MILESTONE-NOMENCLATURE.md](MILESTONE-NOMENCLATURE.md):
version-first release trains, `H<n>` history stages, close-date cohorts,
and two continuous milestones (Backlog, Catalog).

## Open release trains

| Train | Window | Purpose |
|-------|--------|---------|
| `v0.3 · Hexagonal core & security (2026-11)` | 2026-10-13 → 2026-11-30 | Ports & Adapters epic #1693 including `jpa` retirement (#571–#580), critical and security work |
| `v0.4 · Product completeness (2026-12)` | 2026-10-13 → 2026-12-31 | Next.js product gaps and visual NFRs |
| `v0.5 · Quality & traceability (2027-01)` | 2027-01-01 → 2027-01-31 | Tests, documentation, traceability |
| `v0.6 · Platform & operability (2027-02)` | 2027-02-01 → 2027-02-28 | CI, observability, non-critical operability |
| `v1.0 · GA: legacy-free (2027-03)` | 2027-03-01 → 2027-03-31 | Phase 6 deprecation (#899) and the Owner's GA decision |

## Continuous

- `Backlog · Unscheduled`: low-priority work and Owner decisions, no due date.
- `Catalog · Requirements & use cases`: RF / RNF / CU anchors, no due date.

## Closed history

- Stages `H0 · Swing monolith (2014–2018)` through `H8 · Pre-release hardening (2026-09–2026-10)`.
  `H8` merges the former v1.0, v1.1 and Backlog priority buckets.
- Shipped cuts `v0.1 · First release cuts (2026-10)` and
  `v0.2 · Release identity & product forms (2026-10)`. Closed issues inside a
  cut window (first cut to the minor's last tag) belong to these.
- Cohorts `H · 2026 Q2 cohort: foundations & API`, `H · 2026 Q3 cohort: product build & UI/UX`,
  `H · 2026 Q4 cohort: cloud AI SDLC` for unmilestoned issues by close date.

History milestones are closed and must contain zero open issues. A new closed
issue keeps its release train, recording the cut that shipped it.

## Assignment precedence

`workspace/sdlc/milestone_policy.py` is executable policy:

1. RF / RNF / CU anchors without work labels → Catalog.
2. `priority:low` → Backlog. Security and critical work → v0.3.
3. `jpa` retirement (#571–#580, #584, #595) → v0.3; Phase 6 deprecation → v1.0.
   Product work → v0.4.
4. DevOps implementation → v0.6.
5. Tests, docs and traceability → v0.5.
6. Unclassified work → Backlog.
7. Closed issues: inside a cut window → v0.1 / v0.2; on a renamed milestone →
   its new title; without a milestone → the close-date cohort, or the current
   train after the last cut.

The policy never closes issues or decides whether stale work is valid,
duplicate, or obsolete. That product decision remains tracked by
[#1248](https://github.com/matiaspakua/notaire/issues/1248).

## Verification

```bash
python3 workspace/sdlc/apply_milestone_policy.py --check

gh api 'search/issues?q=repo:matiaspakua/notaire+is:issue+no:milestone&per_page=1' \
  --jq .total_count
```

Both checks must report zero missing assignments before a project-management
change is complete.
