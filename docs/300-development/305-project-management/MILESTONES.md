# Milestones

Every GitHub issue has exactly one milestone. A milestone is either a closed
history cohort or an open delivery train. It is not a SemVer release; product
cuts are defined in [RELEASES.md](RELEASES.md).

The strings below are canonical. Generated counts belong in
`docs/github/milestone-snapshot.json`, not in this policy.

## Open delivery trains

| Train | Window | Purpose |
|-------|--------|---------|
| `2026-11 · Release blockers` | 2026-10-13 → 2026-11-30 | Critical work, security fixes, and RNF-11–16 |
| `2026-12 · Frontend product` | 2026-10-13 → 2026-12-31 | Next.js product scope, functional requirements, use cases, and RNF-05–10 |
| `2027-01 · Quality & traceability` | 2027-01-01 → 2027-01-31 | Tests, documentation, traceability, RNF-01–04, RNF-21–22 |
| `2027-02 · Platform & operability` | 2027-02-01 → 2027-02-28 | CI, observability, and non-critical operability |
| `2027-03 · Legacy retirement` | 2027-03-01 → 2027-03-31 | `jpa` retirement and deprecation work |
| `2027-06 · Backlog` | 2027-04-01 → 2027-06-30 | Low-priority work, owner decisions, RNF-17–20 and RNF-23–24 |

Windows are planning markers for the GitHub roadmap, not effort estimates.
Priority orders work inside a train; it does not choose a second schedule.

## Closed history

Closed issues that had curated milestones retain those groupings under explicit
history names:

- `History · Phase 0 Foundations`
- `History · Phase 1 Backend API`
- `History · Phase 2 Platform & observability`
- `History · Phase 3 Frontend slice`
- `History · Phase 4 Frontend wave`
- `History · Phase 4 Production slice`
- `History · Phase 6 Deprecation start`
- `History · v1.0 priority bucket`
- `History · v1.1 priority bucket`
- `History · Backlog priority bucket`

Previously unmilestoned closed issues are grouped by close date:

| Cohort | Window | Rule |
|--------|--------|------|
| `History · 2026 Q2 — Foundations & API` | 2026-03-01 → 2026-06-30 | Closed before 2026-07-01 |
| `History · 2026 Q3 — Product build` | 2026-07-01 → 2026-09-30 | Closed during 2026 Q3 |
| `History · 2026 Q4 — Cloud AI SDLC` | 2026-10-01 → 2026-10-12 | Closed on or after 2026-10-01 at migration time |

History milestones are closed and must contain zero open issues. A new closed
issue keeps its delivery milestone, preserving the train in which it shipped.

## Assignment precedence

`workspace/sdlc/milestone_policy.py` is executable policy:

1. Security and critical work → Release blockers.
2. Visual/product requirements and use cases → Frontend product.
3. `jpa` retirement and deprecation → Legacy retirement.
4. DevOps implementation → Platform & operability.
5. Tests, docs, traceability, and process NFRs → Quality & traceability.
6. Low-priority, owner-only, or otherwise unclassified work → Backlog.
7. Closed issues without a milestone → the close-date history cohort.

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
