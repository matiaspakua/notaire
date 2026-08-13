---
title: Notaire — Proposed Changes Triage (from explore_report.md)
date: 2026-08-11
mode: opsx:propose (triage list, not scaffolded OpenSpec changes)
---

# Notaire — Proposed Changes Triage

> Derived from `openspec/explore_report.md` (2026-08-11 snapshot). This is a
> **prioritized candidate list**, not full OpenSpec artifacts. Nothing here has been
> scaffolded via `openspec new change` yet. Pick an item (by ID) and I'll run the full
> `/opsx:propose` flow for it — Issue verification, proposal.md, spec deltas,
> design.md, tasks.md.
>
> Every candidate below still needs Gate 0 (GitHub Issue linked to a Use Case) before
> any proposal can be written per `CONSTITUTION.md` §3. Existing issue numbers are
> noted where they already exist; several critical items have **no tracked "do the
> migration" issue**, only symptom issues — that gap is called out explicitly.

## How to read this

| Column | Meaning |
|---|---|
| ID | Candidate change ID for reference in conversation (`P0-1`, `P1-3`, ...) |
| Candidate name | Suggested kebab-case change name if scaffolded |
| Source issue(s) | Existing GitHub issues this would close/relate to |
| Why now | One-line rationale, tied back to explore_report.md evidence |
| Size signal | Rough shape — not an estimate, just scope flavor |

---

## P0 — Critical (blocks trust in the pipeline or the data model)

### P0-1 · `enable-blocking-quality-gates`
- **Source issues:** #566 (critical), #680, #710, #711
- **Why now:** Every other gate in the project — tests, coverage, Checkstyle,
  SpotBugs, Trivy — currently cannot fail a build. This is the root cause behind
  the "green CI, 4 Critical/45 High CVEs" contradiction in §2 of the explore report.
  Until this is decided, every other quality investment is advisory only.
- **Size signal:** Two-phase by necessity — (1) decide burn-down-first vs.
  diff-only-gate strategy (policy decision, not code), (2) implement the chosen gate.
  Phase 1 alone is worth a short design spike before a proposal is written.
- **Open question carried from explore report:** burn down 3,066 Checkstyle
  warnings + 766 SpotBugs findings first, or gate new diffs only and grandfather
  existing code? Materially changes scope — flagged in explore_report.md §6.1.

### P0-2 · `retire-static-jpacontroller-provider`
- **Source issues:** #574 (critical)
- **Why now:** REST controllers instantiate legacy `*JpaController` classes directly
  via a static provider, bypassing Spring DI and `@Transactional` — a direct
  contradiction of the Constitution's stated `repository`-over-`jpa` direction
  (CLAUDE.md, explore_report.md §4.2).
- **Size signal:** Touches controller layer across an unknown subset of the 31
  REST controllers backed by the 27 legacy `jpa` classes — needs a scoping pass to
  find how many controllers are actually affected before sizing further.
- **Gap noted:** No broader "finish `jpa` → `repository` migration" tracking issue
  exists — only this symptom issue. Worth deciding whether to scope this narrowly
  (just the DI/transaction bypass) or open a parent tracking issue for the full
  migration (see explore_report.md §6.2).

### P0-3 · `remove-raw-jdbc-report-singleton`
- **Source issues:** #575 (critical)
- **Why now:** Report generation uses a raw-JDBC `Conexion` singleton that bypasses
  HikariCP pooling and the Flyway-managed schema entirely — connections outside the
  framework's lifecycle management, in a critical business flow (JasperReports
  output).
- **Size signal:** Localized to `ReporteController` and its JDBC access path;
  smaller blast radius than P0-2 but touches a business-critical output.

### P0-4 · `implement-rbac-enforcement`
- **Source issues:** #559 (critical)
- **Why now:** No role/permission enforcement exists anywhere in the backend today.
  Combined with #676 (JWT has no refresh/revocation/expiry), this is the project's
  most significant authz gap.
- **Size signal:** Cross-cutting — likely touches most/all of the 31 controllers if
  enforced consistently. Good candidate for a design.md that defines the
  enforcement mechanism (e.g. method-level `@PreAuthorize` + a permissions model)
  before any controller-level tasks are written.

### P0-5 · `reconcile-cu-api-matrix-and-issue-status`
- **Source issues:** #598 (critical), #599 (high)
- **Why now:** The traceability artifact the whole SDLC leans on (Constitution P4)
  is itself unreliable — `CU-API-MATRIX.csv` has zero rows for CU69–78 (the entire
  Workflow feature), and 64 open issues self-report "Terminado/100%" contradicted by
  their own test-matrix row. This undermines trust in every other status claim in
  the backlog, including this triage.
- **Size signal:** Primarily a documentation/audit change, not code — closer to the
  2026-05-29 functional-baseline reconciliation effort already done once. Could
  reasonably be scoped as a recurring process (see explore_report.md §6.3) rather
  than a one-time fix.

---

## P1 — High (security, performance, and process debt)

### P1-1 · `harden-jwt-lifecycle`
- **Source issues:** #676 (high)
- **Why now:** No refresh mechanism, no revocation, no expiry enforcement on JWTs —
  pairs directly with P0-4 (RBAC) as the other half of the authn/authz gap.
- **Size signal:** Contained to the security/auth config layer; well-scoped.

### P1-2 · `complete-bean-validation-rollout`
- **Source issues:** #655 (high, already `in-progress`)
- **Why now:** Already underway per its label — flagging here mainly so it isn't
  dropped from a prioritized view. Not a new proposal; check current branch/PR
  state before scaffolding anything.
- **Size signal:** Incremental, controller-by-controller.

### P1-3 · `paginate-unbounded-list-endpoints`
- **Source issues:** #596 (high)
- **Why now:** 26 of 31 REST controllers return unbounded `findAll()`. Compounds
  with P1-4 and P1-5 into the performance risk described in explore_report.md §4.3.
- **Size signal:** Mechanical but wide — 26 controllers is a lot of touch points;
  good candidate for a shared pattern (e.g. a base paginated-list convention) defined
  once in design.md and then applied repeatedly.

### P1-4 · `fix-eager-fetch-n-plus-one-risk`
- **Source issues:** #595 (high)
- **Why now:** 27 `FetchType.EAGER` associations across 16 entities — Cartesian
  product / N+1 risk that compounds with P1-3's unbounded queries.
- **Size signal:** Entity-mapping change; needs care around existing test coverage
  for lazy-loading behavior (`LazyInitializationException` risk if done carelessly
  outside a transaction boundary).

### P1-5 · `add-catalog-read-cache`
- **Source issues:** #597 (medium, but grouped here as it completes the
  performance trio with P1-3/P1-4)
- **Why now:** No caching layer on read-heavy catalog endpoints (tramites,
  conceptos, estados, etc.) — the natural complement once pagination and eager-fetch
  are addressed.
- **Size signal:** Spring Cache abstraction on a known, bounded set of catalog
  endpoints — moderate, well-isolated.

### P1-6 · `enable-observability-tls`
- **Source issues:** #684 (medium)
- **Why now:** Metrics traffic between backend, Prometheus, Grafana, Loki is
  unencrypted. Infra-only change, no application code impact.
- **Size signal:** `infra/` docker-compose + cert management; contained to
  `devops-engineer` territory.

### P1-7 · `split-ci-workflow-into-reusable-jobs`
- **Source issues:** #679 (medium)
- **Why now:** The CI workflow is a 735-line monolith. Not urgent on its own, but
  worth doing *before* P0-1's gate work lands more logic into the same file —
  sequencing matters here.
- **Size signal:** Refactor of `.github/workflows/`, no behavior change intended.

### P1-8 · `purge-src-old-and-binaries-from-repo`
- **Source issues:** #682 (medium)
- **Why now:** `src.old/` (8.1M) and committed `.class`/`.jasper` binaries bloat
  every clone (`.git` is already 109M). Cheap win, zero behavior risk, but touching
  git history (if history-purge is in scope, not just deletion) needs explicit
  user sign-off before anything destructive runs.
- **Size signal:** Small if just deleting going forward; materially different
  (and higher-risk) if rewriting history — flag this distinction explicitly when
  scoping.

---

## Not included (already fully tracked / pure ops-doc backlog)

The five ops-runbook issues from the explore report's critical list (#264 DB
migration guide, #265 env config guide, #266 incident response, #267 OWASP
checklist, #270 disaster recovery, #271 DB maintenance procedures) are
documentation-only deliverables with no code dependency and no ambiguity about what
"done" looks like — they don't need a design.md/tasks.md OpenSpec treatment the way
a code change does. Say the word if you want them scaffolded anyway (e.g. to get
them under the same traceability discipline), otherwise they're better handled as
direct documentation PRs referencing their existing issues.

---

## Suggested sequencing (if useful)

```
P0-1 (gates)  ──┬──▶ everything else benefits from a real gate before it lands
                │
P0-5 (traceability) ──▶ trust the backlog before prioritizing further
                │
P0-2, P0-3 (architecture drift) ──▶ independent of each other, can run in parallel
                │
P0-4 (RBAC) + P1-1 (JWT) ──▶ natural pair, same subsystem
                │
P1-3 → P1-4 → P1-5 (performance trio) ──▶ do in this order, each de-risks the next
                │
P1-7 (split CI) ──▶ do before or alongside P0-1, not after
                │
P1-6, P1-8 ──▶ low-risk, can slot in anytime
```

---

*Next step: tell me which ID(s) to take through the full `/opsx:propose` flow
(GitHub Issue check → proposal.md → spec deltas → design.md → tasks.md). I'll do
them one change at a time, per the Constitution's one-issue-per-change rule.*
