> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1179, Use Case CU77, related #302. Assessment on updated `main`
(2026-10-03):

| Finding | Detail |
|---------|--------|
| `infra/` | Observability + SonarQube stack; scripts read the root `.env`, use `REPO_DIR`, attach to external network `notaire_notary-network` |
| `deploy/kustomize` | Staging manifests (#901); pure infra, pulls GHCR images, no build context |
| `deploy/nginx/nginx.conf` | Mounted by `docker-compose.prod.yml`; **byte-for-byte equivalent** (54/54 non-comment lines, 0 diff) to the inline copy in `reverse-proxy-configmap.yaml` |
| `deploy/` as a folder | Created today by #1044 and #901; no independent reason to exist outside `infra/` |
| `performance-test/k6` | Load test used by `performance-test.yml` and guarded by `scripts/test_performance_test_assets.py` |
| `docker-compose.cloud.yml` | Override of `docker-compose.yml` (`COMPOSE_FILE=a:b` in `.cursor/*`) |
| `docker-compose.prod.yml` | Builds from application source (`context: .`) |
| `infra/tests/e2e` | Stale duplicate Playwright suite, unreferenced |

## Goals / Non-Goals

**Goals:**

- One self-contained `infra/` folder, split-ready.
- Infra specifics documented in `infra/`; `docs/` links, never copies.
- No behaviour change; every existing validator keeps passing.

**Non-Goals:**

- Creating the new repository or splitting history.
- Moving the three root compose files, Dockerfiles or workflows.
- Changing observability configuration, dashboards, alerts or credentials.

## Decisions

1. **Root compose files stay at the root**
   - Why: `.cloud.yml` is an override of the dev file and `.prod.yml` builds
     application source; moving them would break pairing or make `infra/`
     escape its folder. They are the application composition.
   - Alternative rejected: move and use `../../` build contexts — violates
     self-containment.

2. **Layout `infra/{observability,deploy,performance,scripts,docs}`**
   - Why: groups by operational concern; each folder maps to one future
     CI/doc section.
   - Alternative rejected: keep `infra/` flat and add folders beside it — the
     compose file and four config dirs would stay mixed with new ones.

3. **`git mv` only, no content edits during moves**
   - Why: preserves history; path edits happen in a separate atomic commit.

4. **Env handling: `infra/.env.example` + explicit env file**
   - Scripts load `INFRA_ENV_FILE`, else `infra/.env`, else the root `.env`
     (documented, optional fallback while co-located). `infra/.env` is already
     git-ignored.

5. **One `nginx.conf`, kept in `infra/deploy/kustomize/base/`**
   - Why: today's two copies are identical and will drift. A `configMapGenerator`
     with `files: [nginx.conf]` builds the ConfigMap from the file; kustomize
     rewrites the Deployment's reference to the hashed name. Keeping the file
     inside the base satisfies kustomize's default load restrictor, so plain
     `kubectl apply -k` works.
   - Alternative rejected: separate `infra/deploy/nginx/` read via
     `--load-restrictor=LoadRestrictionsNone` — every operator would need the flag.
   - `deploy/` is dropped as a top-level folder; `deploy/nginx` disappears.

6. **Remaining app→infra coupling is documented, not hidden**
   - prod compose mounts `infra/deploy/kustomize/base/nginx.conf`; observability
     scrapes `notary-backend` over the external network. `infra/docs/OPERATION.md`
     lists both as the seam to replace (GHCR images) at split time.

7. **TDD via a static guard first**
   - `scripts/test_infra_standalone.py` encodes the layout and
     self-containment; existing guards are repointed to new paths first, so they
     fail until the move.

## Riesgos / Trade-offs

- [Missed consumer of a legacy path] → guard greps all tracked files for
  legacy paths; full preflight before push.
- [CI path filters/`paths-ignore` silently stop matching] → update and check
  `codeql.yml` and `performance-test.yml` explicitly.
- [Generated ConfigMap gets a hash suffix] → kustomize rewrites references; guard asserts the Deployment still mounts the generated ConfigMap.
- [Docs drift] → Gate 3 doc tasks name every file; guard checks links.
- [Large diff hides logic] → moves and edits in separate commits.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| All infra assets live under infra/ | static | `scripts/test_infra_standalone.py` |
| Legacy locations no longer exist | static | same |
| Stale E2E suite removed | static | same |
| infra/ does not reference paths outside itself | static | same |
| infra/ ships its own env example without secrets | static | same |
| nginx.conf has a single source | static + `kustomize build` | same + `scripts/test_staging_kustomize.py` |
| Documentation set exists and is linked | static | same |
| Consumers point to the new paths | static | same + repointed `scripts/test_*.py` |
| Stack still starts and manifests validate | static / compose config | existing validators + `docker compose -f infra/observability/docker-compose.yml config` |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java touched)
- New integration tests: n/a
- Coverage impact (JaCoCo): none

## Regression Strategy

- Existing tests affected: `test_staging_kustomize.py`, `test_prod_compose.py`,
  `test_infra_prometheus_hardening.py`, `test_performance_test_assets.py`,
  `test_image_pins_and_dependabot.py` — repointed, assertions not weakened.
- Full suite command: `bash scripts/preflight.sh`, then
  `bash scripts/run_pipeline.sh`; `mvn verify -pl backend-api` only if Java is touched.
- HTTP/Bruno API suite: unchanged; run via the pipeline.
- Legacy paths at risk: none (`jpa` / Swing not involved).

## Playwright Strategy

n/a — no UI surface. The deleted `infra/tests/e2e` suite was never wired to CI;
`frontend/tests/e2e` is untouched and still runs in CI.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; CI workflow path updates land in the
  same PR as the moves so no job points at a missing file.
- Configuration or `.env` keys: none new; infra variables documented in
  `infra/.env.example`.
- Feature flag: no
- Smoke test after deploy (Gate 5): bring up app + `bash infra/scripts/start-infra.sh`,
  `bash infra/scripts/check-infra.sh` all green; `kustomize build` of staging overlay.

## Rollback Strategy

- Revert the PR: restores all paths; no data, schema or runtime state changes.
- Running stacks are unaffected: containers keep running; only the compose
  file path used to manage them changes.
