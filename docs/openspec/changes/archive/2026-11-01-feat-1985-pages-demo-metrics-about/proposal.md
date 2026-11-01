# Pages — live read-only demo, delivery outcomes and an About section

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1985 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/1985_pages_demo_metrics_about` |
| Gate 1 status | passed |

## Objetivo

The portfolio review (#1977) found three gaps in the GitHub Pages site: a visitor cannot try the product, the site
counts activity but not outcomes, and it does not say who built it. On 2026-11-01 the Owner asked for all three, at
zero cost and without invented facts or numbers.

## What Changes

- **Live read-only demo at `/demo/`.** `github-page/demo/build-demo.mjs` statically exports the real Next.js frontend
  under `/notaire/demo/`. A small fetch shim answers `/api/**` from `demo/fixtures.json`, which is recorded from the
  seeded local stack by `demo/record-fixtures.mjs`. Writes never reach a network. Every page shows a banner that says
  "Demo – sample data, read-only".
- **Delivery outcomes.** `workspace/ci/generate-delivery-metrics.py` reads GitHub and writes
  `github-page/lib/delivery-metrics.json`: PR lead time (median, p90), weekly throughput, a change-failure proxy, time to
  restore `main`, first-push CI pass rate, issues closed and releases, with its window and method. Metrics that the
  GitHub data cannot support are stored as unavailable, with the reason. The Pages deploy re-runs the script before
  every build.
- **About.** `/about/` and a landing section. Content comes from `github-page/lib/about.json`, where every fact names
  its source. Missing facts are marked placeholders that the site never renders.
- A hosting issue for a demo with a real backend: options and costs, nothing provisioned.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Numbers on the site come from the repository or GitHub, with their date | CONSTITUTION §7, §8 | Extended to outcome metrics |
| The demo is read-only and sends nothing to a server | Owner, 2026-11-01 | New |
| Facts about a person need a named source; nothing is invented | Owner, 2026-11-01 | New |
| No paid hosting | Owner, 2026-11-01 | New |

## Capabilities

### New Capabilities

- `github-pages-demo-outcomes-about`

### Modified Capabilities

- none

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | the demo build copies and patches it in a temporary directory; no source change |
| `infra` / observability | no | – |
| CI/CD (`.github`) | yes | `pages-ci.yml` and `deploy-github-page.yml` install frontend deps and build the demo; the deploy refreshes the metrics |
| `docs` | yes | OpenSpec change; CHANGELOG |
| `workspace` | yes | `workspace/ci/generate-delivery-metrics.py`, `workspace/tests/test_pages_demo_outcomes_about.py`; `github-page/` (`demo/`, `/about/`, results section, landing sections, site registry) |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none new (the demo build reuses `frontend/` dependencies)

### Architecture review

Static-export Pages app only. No ADR.

## Documentation Impact

| Document | Change |
|----------|--------|
| `github-page/docs/demo.md` | new: how the demo works and why this approach |
| `github-page/README.md` | demo, metrics and About |
| `CHANGELOG.md` | `[Unreleased]` entry |

## Out of scope

- A demo with a real backend (separate issue with options and costs).
- Spanish locale in the demo: it is exported in English only.
