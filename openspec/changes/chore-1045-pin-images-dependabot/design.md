> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1045 (audit-2026-09, CU78): floating container tags and incomplete
Dependabot ecosystems. See proposal.md — Objetivo.

Verified on `origin/main` tip `ce97e114` (2026-10-03, after `git fetch origin main`):

| Location | Finding |
|----------|---------|
| `docker-compose.yml` | `postgres:16`; `dpage/pgadmin4:latest` |
| `docker-compose.prod.yml` | `postgres:16`; `nginx:1.27-alpine` (minor-ish; tighten if AC hygiene wants `1.27.x`) |
| `infra/docker-compose.yml` | `b4bz/homer:latest`, `sonarqube:community`, `postgres:15`, `prom/prometheus:latest`, `prometheuscommunity/postgres-exporter:latest`, `grafana/grafana:latest`, `grafana/loki:latest`, `grafana/promtail:latest` |
| `backend-api/Dockerfile` | `FROM maven:3.9-eclipse-temurin-21-alpine`; `FROM eclipse-temurin:21-jre-alpine` |
| `backend-api/Dockerfile.slim` | `FROM eclipse-temurin:21-jre-alpine` |
| `frontend/Dockerfile` | `FROM node:22-alpine` (builder + runner) |
| CI workflows | `postgres:16-alpine` in `playwright-e2e.yml` / `performance-test.yml` |
| `.github/dependabot.yml` | **maven** + **npm** (`/frontend`) + **github-actions** — **no docker** |
| npm presence | Added with #1136 (`[#1135] ci(security): add CodeQL and the gh-secure baseline`); issue body “maven + github-actions only” is **stale** |
| Docs | DevSecOps README already lists Maven/npm/Actions; omits docker |
| Overlap #1046 | Alert remediation (smol-toml + Swing delete) — **different scope**; queue **before** #1045 |

Serialize implement: after **#1043** (queue `#1046 → #1042 → #1041 → #1040 → #1043 → #1045`)
or as coordinator schedules. No product UI; heavy CI Playwright must still pass.

## Goals / Non-Goals

**Goals:**

- Eliminate `:latest` and other floating selectors for images in #1045 AC.
- Pin Dockerfile `FROM` lines and compose/CI postgres (and infra) images to
  minor tags or digests within ADR-017 families.
- Ensure Dependabot has npm (`/frontend`) and docker (`/backend-api`,
  `/frontend`) ecosystems without duplicating npm.
- Prove pins + Dependabot shape with a failing-then-green hygiene script.

**Non-Goals:**

- Fixing Dependabot *alerts* (#1046).
- Publishing frontend GHCR / semver (#1043).
- Changing Java/Node major versions or leaving Alpine.
- Automating compose `image:` updates via Renovate (Dependabot docker covers
  Dockerfiles; compose pins are maintained in-repo + docs).

## Decisions

1. **Prefer minor tags over digests for human-readable pins; digests optional for highest-risk bases**
   - Why: AC allows either; minor tags stay reviewable and Dependabot-friendly
     for Dockerfiles. Digests are acceptable where registries lack stable
     minor tags.
   - Alternative rejected: digest-only everywhere — harder reviews / opaque diffs.
   - Implement MUST resolve **current** minors at implement time (do not trust
     Gate 1 examples that may age).

2. **Idempotent npm ecosystem — keep existing entry from #1136**
   - Why: `origin/main` already has npm `/frontend`. AC “added” is satisfied by
     presence; duplicating breaks Dependabot.
   - Alternative rejected: remove/re-add npm — noise and risk of config drift.

3. **Docker Dependabot entries per Dockerfile directory (`/backend-api`, `/frontend`)**
   - Why: Dependabot’s docker ecosystem discovers `Dockerfile` in the named
     directory; those two own product images. Compose-only infra images are
     pinned in YAML; Dependabot will not magically rewrite every compose
     `image:` line — document manual/infra bump path in README.
   - Alternative rejected: single docker entry at `/` — no root Dockerfile;
     would miss nested Dockerfiles.
   - Note: `Dockerfile.slim` shares the same directory; pin its `FROM` in the
     same PR; Dependabot primarily tracks the primary `Dockerfile`.

4. **Include CI postgres service images in the pin sweep**
   - Why: Same floating postgres family as compose; leaving CI on
     `postgres:16-alpine` while compose is `16.x` recreates drift.
   - Alternative rejected: compose-only pins — weaker AC for “postgres”.

5. **TDD via static hygiene script, not product JUnit**
   - Pattern: `scripts/test_image_pins_and_dependabot.py` (or equivalent)
     asserts forbidden tags and required Dependabot ecosystems; fail on
     pre-change tree, pass after pins.

6. **Implement after #1043 (and the #1046→…→#1040 queue)**
   - Why: #1043 extends CD around `frontend/Dockerfile`; pinning bases after
     that publish path lands avoids mid-flight Dockerfile races. #1046 may
     touch `frontend/package.json` / lockfile and delete Swing — land first.
   - Coordinator may schedule #1045 later or in parallel only if no file
     contention on Dockerfiles / dependabot.yml.

## Riesgos / Trade-offs

- **[Risk] Pinned minor becomes EOL / CVEs accumulate** → Mitigation: Dependabot
  docker PRs for Dockerfile bases; document compose/infra bump checklist.
- **[Risk] Sonar/Grafana/Prometheus minor bump breaks local infra** → Mitigation:
  smoke `bash scripts/start-all.sh` (or infra subset) after pin; prefer known-good
  minors used in recent successful local runs when choosing pins.
- **[Risk] Issue body claims npm missing but main already has it** → Mitigation:
  design Decision 2; AC scenarios assert presence, not “must rewrite”.
- **[Risk] Concurrent Dependabot flood after enabling docker** → Mitigation:
  reuse weekly schedule + `open-pull-requests-limit` similar to existing
  ecosystems; group if needed.
- **[Risk] Race with #1043 on `frontend/Dockerfile`** → Mitigation: serialize
  after #1043.
- **[Trade-off] Compose images not auto-updated by Dependabot docker** → Accept;
  pin + docs; optional follow-up for Renovate/compose support outside AC.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| pgadmin not `:latest` | unit/script | `scripts/test_image_pins_and_dependabot.py` |
| infra no `:latest` | unit/script | same |
| sonarqube not bare `community` | unit/script | same |
| postgres minor/digest in compose + infra | unit/script | same |
| backend Dockerfile bases pinned | unit/script | same |
| frontend Dockerfile bases pinned | unit/script | same |
| CI postgres pinned | unit/script | same |
| npm ecosystem `/frontend` present once | unit/script | same |
| maven + github-actions retained | unit/script | same |
| docker `/backend-api` + `/frontend` | unit/script | same |
| no duplicate docker directories | unit/script | same |
| Stack still boots (smoke) | command | `bash scripts/start.sh` / infra as practical |

- New unit tests: stdlib/Python hygiene script (forbidden tag regex + YAML parse).
- New integration tests: none required for backend Java.
- Coverage impact (JaCoCo): none expected (no product Java).

## Regression Strategy

- Existing tests affected: none expected in `backend-api` product code.
- Full suite command: `mvn test -pl backend-api` (sanity); frontend `npm ci`
  if lockfile untouched (expect no change from #1045 alone).
- Docker build smoke: `docker build` backend/frontend contexts if preflight
  requires; ensure pinned tags pull successfully.
- HTTP/Bruno: n/a for API delta.
- Legacy paths at risk: none; image tag strings only.

## Playwright Strategy

- n/a — no UI product surface (compose/Dockerfile/Dependabot/docs only).
- Repository heavy CI still runs Playwright on the PR; do **not** skip that job.
- Serialize behind the fleet queue through #1043; if unexpected E2E edits
  appear, serialize with other Playwright-heavy PRs per fleet playbook.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge to `main` → operators pull new tags →
  Dependabot docker may open base-image PRs on next schedule
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): compose/infra images match pins; Dependabot
  config shows npm + docker; optional `docker compose pull` succeeds for pinned
  tags

## Rollback Strategy

- Revert safe: yes — `git revert` restores prior tags and dependabot.yml; no
  schema/data impact
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: low (local/dev reproducibility / update PRs)

## Migration Plan

1. Wait for queue through **#1043** (`#1046 → #1042 → #1041 → #1040 → #1043`)
   unless coordinator schedules otherwise.
2. Copy this draft into `openspec/changes/chore-1045-pin-images-dependabot/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh chore-1045-pin-images-dependabot`.
4. Branch `cursor/chore-1045-pin-images-dependabot-69d3` → failing hygiene tests
   → pin images + Dependabot docker (verify npm) → green → PR `Closes #1045`.
5. After merge: confirm Dependabot docker PRs can open; docs reflect ecosystems.

## Open Questions

- Exact minor numbers for each image — **defer to implement time** (resolve from
  registries); does not change specs or task structure.
- Whether to digest-pin GHCR-published app images in prod compose — out of
  scope (prod compose currently builds locally / nginx reverse proxy path).
