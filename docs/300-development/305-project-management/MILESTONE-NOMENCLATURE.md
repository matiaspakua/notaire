# Milestone nomenclature

Approved by the Owner on 2026-10-16 ([#1847](https://github.com/matiaspakua/notaire/issues/1847)).
A milestone title shows **where the work sits in the project's life**:
history, shipped cut, current train or future train. Executable source:
`workspace/sdlc/milestone_policy.py`. Release mapping:
`train_for()` in `workspace/release/product_release.py`.

## Patterns

| Kind | Pattern | Example | State | Due date |
|------|---------|---------|-------|----------|
| Release train | `v<MAJOR>.<MINOR> · <Theme> (<YYYY-MM>)` | `v0.3 · Hexagonal core & security (2026-11)` | open until the Owner closes it | end of window |
| Shipped cut | same pattern, closed | `v0.2 · Release identity & product forms (2026-10)` | closed | last tag date |
| History stage | `H<n> · <Stage> (<start>–<end>)` | `H2 · REST API migration (2026-05)` | closed | end of window |
| History cohort | `H · <YYYY> Q<n> cohort: <theme>` | `H · 2026 Q3 cohort: product build & UI/UX` | closed | end of quarter |
| Continuous | `Backlog · Unscheduled` | – | open | none |
| Catalog | `Catalog · Requirements & use cases` | – | open | none |

`·` is U+00B7 with one space on each side. The date in a train title is the
target month. It is a hint for people; the due date is the contract.

## Rules

1. **Version → milestone.** There is one milestone per minor. Every patch
   `vX.Y.z` belongs to `vX.Y · …`. `train_for()` finds the title by its
   `vX.Y · ` prefix and fails closed when no train exists. Adding a minor is
   an explicit change to this page, the policy and `RELEASE_TRAINS`.
2. **Meaning.** A milestone is a release train with one dominant theme. Themes
   that span trains are epics (`roadmap` label + sub-issues), not milestones.
3. **Closing.** The Owner closes a train when its exit criterion holds; the
   `vX.Y.0` tag ships it, and patches can follow. Open work left over is moved
   to the next train with a comment, never left on a closed milestone.
4. **Backlog.** `Backlog · Unscheduled` holds `priority:low` and Owner
   decisions. Scheduling an issue means moving it into a `vX.Y` train.
5. **Catalog.** RF / RNF / CU anchors without work labels live on `Catalog`.
   A release never closes them; each closes with its own evidence.
6. **History.** `H0`…`H8` are the project's stages, from the Swing monolith to
   pre-release hardening. Cohorts hold unmilestoned issues by close date.
   Closed issues inside a cut window (from the first cut to the last tag of
   the minor) belong to that shipped train, unless they sit on a curated stage.
7. **One milestone per issue**, including closed issues.
8. **Priority is a label, not a milestone.** The old v1.0/v1.1/Backlog priority
   buckets merged into `H8`; their `priority:*` labels keep the split.

## Description template

```text
Goal: <one sentence>. Window: <versions and dates>. Key deliverables: <≤ 6 issues or epics>.
Exit: <measurable criterion>. Next: <following milestone>.
```

Backlog, Catalog and the cohorts use a one-line purpose instead.

## Current map

| Milestone | Window | Theme |
|-----------|--------|-------|
| `H0 · Swing monolith (2014–2018)` | 2014–2018 | academic Swing + MySQL desktop app; source of the RF/RNF/CU catalog |
| `H1 · Revival & foundations (2026-03–2026-04)` | 2026-03 → 04 | docs, standards, base CI |
| `H2 · REST API migration (2026-05)` | 2026-05 | REST API, JWT, Flyway |
| `H3 · Platform & observability (2026-06)` | 2026-06 | k3d, Prometheus, Grafana |
| `H4 · Web frontend slice (2026-07)` | 2026-07 | first Next.js slice |
| `H5 · Web frontend wave (2026-07–2026-08)` | 2026-07 → 08 | use-case screens |
| `H6 · Production slice (2026-08–2026-09)` | 2026-08 → 09 | E2E, OWASP, DR |
| `H7 · Swing deprecation start (2026-09)` | 2026-09 | Swing removal |
| `H8 · Pre-release hardening (2026-09–2026-10)` | 2026-09 → 10-12 | merged priority buckets |
| `v0.1 · First release cuts (2026-10)` | notaire-v0.1.1, notaire-v0.1.2 | first semver cuts |
| `v0.2 · Release identity & product forms (2026-10)` | v0.2.0 … v0.2.3 | release identity, missing forms, workflow editor |
| `v0.3 · Hexagonal core & security (2026-11)` | → 2026-11-30 | #1693, jpa retirement #571–#580, security |
| `v0.4 · Product completeness (2026-12)` | → 2026-12-31 | product gaps, visual NFRs |
| `v0.5 · Quality & traceability (2027-01)` | 2027-01 | tests, docs, traceability |
| `v0.6 · Platform & operability (2027-02)` | 2027-02 | CI and observability gates |
| `v1.0 · GA: legacy-free (2027-03)` | 2027-03 | Phase 6, Owner GA decision |
| `Backlog · Unscheduled` | – | unscheduled work |
| `Catalog · Requirements & use cases` | – | requirement anchors |
| `H · 2026 Q2/Q3/Q4 cohort: …` | quarters | unmilestoned closed issues by close date |
