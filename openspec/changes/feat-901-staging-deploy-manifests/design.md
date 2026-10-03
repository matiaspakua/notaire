> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #901 (DEVOPS, risk, roadmap Phase 6). Use Case CU77. Re-checked on
updated `origin/main` (2026-10-03):

| Finding | Detail |
|---------|--------|
| `docker-compose.prod.yml` | **Present** (#1044 CLOSED) — postgres + backend + frontend + reverse-proxy; no pgAdmin; host `:80` only |
| `k8s/` / `helm/` / Kustomize | **Absent** before this change |
| `.github/workflows/cd.yml` | Publish-only (backend + frontend → GHCR, SBOM, Trivy, cosign) — no kubectl/SSH deploy |
| `DEPLOYMENT-PLAN.md` §1 | Still claims no staging/prod target (stale vs prod compose) |
| CU77 | Exists; GitHub ID table omits #901 |
| Related | #1043 CLOSED (frontend GHCR); open: #254 TLS, #256 backups, #306 SLO, #288 runbooks |

Queue ahead cleared (#1067 merged). Frontend GHCR image tags are available
(#1043 CLOSED); staging overlay uses GHCR SHA tags for backend and frontend.

## Goals / Non-Goals

**Goals:**

- Ship minimal Kustomize base + staging overlay mirroring #1044’s four services.
- Static validation of service set, isolation, secrets placeholders, env/Flyway.
- Honest docs: compose vs manifests vs “no automated cluster deploy yet”.
- Optional CI validate job (build/render), not a fake prod deploy.

**Non-Goals:**

- Helm ecosystem / operators / mesh / multi-cluster / GitOps platform.
- TLS (#254), backups (#256), SLOs (#306), runbooks (#288).
- Folding `infra/` into app manifests.
- Replacing `docker-compose.prod.yml`.
- Claiming live staging exists if no host is available.

## Decisions

1. **Kustomize over Helm**
   - Why: KIS; overlays fit “same services, different image tags/config”.
   - Alternative rejected: full Helm chart with values sprawl for four services.

2. **Directory `deploy/kustomize/` (preferred) or `k8s/`**
   - Why: `deploy/` already holds `deploy/nginx/nginx.conf` from #1044.
   - Keep manifests next to existing deploy artifacts unless a repo convention
     prefers top-level `k8s/`.

3. **Parity with #1044 topology, not a new platform**
   - Four logical services only; proxy/ingress as sole external entry.
   - Postgres ClusterIP / internal-only.

4. **Frontend image strategy**
   - Prefer GHCR frontend when #1043 is available; until then document
     build-from-source or compose for frontend while backend pulls GHCR.
   - Do not invent a second frontend packaging story.

5. **CD scope: validate, don’t fake-deploy**
   - Add optional workflow job: `kustomize build` / kubeconform / script.
   - Do not add a “Deploy to production” job without a real target environment.

6. **TDD via static validator first**
   - Pattern: `scripts/test_prod_compose.py` → new `scripts/test_staging_manifests.py`
     (or equivalent) that fails before manifests exist, then goes green.

## Riesgos / Trade-offs

- [No real staging cluster in CI] → Validate by render/static checks; document
  manual apply; do not claim automated env deploy.
- [Frontend image gap until #1043] → Overlay documents fallback; serialize
  implement after #1043 when practical.
- [Scope creep into Helm/platform] → Spec forbids extra platform services;
  reject PRs that add operators/mesh.
- [Doc drift in DEPLOYMENT-PLAN / SAD] → Explicit Gate 3 doc tasks.
- [Secret leakage in example overlays] → Use placeholders / `Secret` refs only;
  review in PR.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Base renders four services | static script / CI | `scripts/test_staging_manifests.py` (or equiv.) |
| Staging overlay same service set | static | same |
| Postgres not publicly published | static | same |
| No committed credentials | static / grep | same + PR review |
| ENVIRONMENT production / staging-equivalent | static | same |
| Flyway baseline off | static | same |
| Validator fails when incomplete | static (red-then-green) | same |
| Validator passes on shipped tree | static | same |
| Docs document apply path | docs checklist | PR review |

- New unit tests (`src/test/java/.../unit/`): n/a unless Java touched
- New integration tests: n/a
- Coverage impact (JaCoCo): none expected

## Regression Strategy

- Existing tests affected: `scripts/test_prod_compose.py` must remain green;
  do not weaken #1044 compose invariants.
- Full suite command: `mvn verify -pl backend-api` only if Java touched;
  otherwise script validators + applicable CI jobs.
- HTTP/Bruno API suite: n/a unless accidentally broken by unrelated edits.
- Legacy paths at risk: none (`jpa` / Swing not involved).

## Playwright Strategy

n/a — no UI surface. Product pages are unchanged. Required Playwright CI jobs
may still run on the PR; no new E2E specs.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: land manifests + validator + docs; optional CI
  validate job after scripts exist. Operators apply staging overlay manually
  (or via future CD once a real host exists).
- Configuration or `.env` keys: document Secret keys (postgres, JWT, actuator,
  app admin) — add pointers in `.env.example` / deployment README only.
- Feature flag: no
- Smoke test after deploy (Gate 5): if a staging host exists, health endpoint
  via ingress; otherwise document “render + validator green” as Gate 5 evidence
  for this infra change and note live apply is operator-owned.

## Rollback Strategy

- Revert the PR (safe): removes manifests/validator/docs only; does not affect
  running compose-based environments.
- Leave `docker-compose.prod.yml` as the fallback production artifact (#1044).
- No schema migration to roll back.
