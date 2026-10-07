# Cloud Environment Requirements Checklist

Checklist for the Cursor Cloud **environment.json** / environment build that
hosts the AI SDLC foreman fleet. Populate secrets from `.env.example` keys
(never commit `.env`).

> This fleet does **not** require `local-ai/`, oMLX, or Codex local profiles.
>
> **Nested Docker:** bridge CNI between containers often fails in Cloud Agent VMs.
> Use **host-network compose** (`docker-compose.cloud.yml` on `main`, or equivalent)
> and point the backend JDBC URL at `127.0.0.1`. Boot `start` must start `dockerd`
> (fuse-overlayfs + iptables-legacy) before `docker compose up`.
>
> **Until the Environment card is Saved** with `install=bash .cursor/install.sh`,
> Cloud Agents must run `bash .cursor/install.sh` themselves — that script is the
> source of `openspec` (`@fission-ai/openspec`) and `bc` on PATH. Gate 1 scenario
> counting in `validate-sdlc-plan.sh` no longer hard-depends on `bc` (awk sum).

---

## 0. Cursor Environment card (must be Saved)

Draft environment builds alone are **not** enough for auto boot. On the Cursor
Cloud Environment dashboard card:

1. Set `install` to `bash .cursor/install.sh`
2. Set `start` to `bash .cursor/start.sh`
3. **Save** the card (promotable / bootable config on the default branch)

Until the card is Saved with those self-contained scripts (present on `main` via
PR #1112), new agents may boot without Maven/Docker/OpenSpec/`bc` and Gate 1 / stack
bring-up will fail. Triggering a draft build from a feature branch does not replace
Saving the card.

---

## 1. Base toolchain (install / snapshot)

| Component | Required version | Why | Verify |
|-----------|------------------|-----|--------|
| JDK | **21** | Spring Boot 4.1 backend | `java -version` |
| Maven | **3.9+** | `mvn test`, `verify`, Spotless/Checkstyle | `mvn -version` |
| Node.js | **22.x LTS** (or project engines) | Next.js 16 frontend | `node -v` |
| npm | bundled with Node | `npm ci`, Vitest, Playwright | `npm -v` |
| Docker Engine + Compose | **24+** | `workspace/stack/start.sh`, integration/smoke | `docker version` && `docker compose version` |
| Git | 2.40+ | branches, hooks | `git --version` |
| GitHub CLI `gh` | recent | issues, PR checks, merge | `gh auth status` |
| OpenSpec CLI | current project-supported | Gate 1 `openspec validate` | `openspec --version` |
| Bruno CLI | **≥4.2.0** (`@usebruno/cli@4.2.0` via `.cursor/install.sh`) | OpenCollection / `preflight.sh --full` API tests; older 2.x only understands `bruno.json` | `bru --version` |
| Python 3 | 3.11+ | occasional scripts / unittest helpers | `python3 --version` |
| `curl` / `jq` | any | health checks, JSON parsing | `curl --version`; `jq --version` |
| `bc` | any | Installed by `.cursor/install.sh` (optional for Gate 1; validator uses awk) | `bc --version` |

### Frontend / E2E extras

| Component | Notes | Verify |
|-----------|-------|--------|
| `frontend/node_modules` | `cd frontend && npm ci` in install or first boot | `test -d frontend/node_modules` |
| Playwright browsers | `cd testing/e2e && npx playwright install --with-deps` (or CI-equivalent) | `npx playwright --version` |
| Bruno / API collection | Collection under `backend-api/api-test/`; CLI `bru` ≥4.2.0 on PATH from install | `bru --version`; collection present |
| markdownlint-cli2 | Via frontend deps for docs lint | `frontend/node_modules/.bin/markdownlint-cli2 --version` |

### Optional but useful

| Component | Notes |
|-----------|-------|
| Trivy | Mirrors advisory CI scan in `preflight.sh` |
| PostgreSQL client (`psql`) | Debug DB when stack is up |

---

## 2. Secrets & env files

Copy `.env.example` → `.env` in the environment (git-ignored). Minimum keys the
fleet needs for autonomous Gate 3 / smoke:

| Key (from `.env.example`) | Used by |
|---------------------------|---------|
| `POSTGRES_*` | Docker DB |
| `APP_ADMIN_USER` / `APP_ADMIN_PASSWORD` | Login / E2E |
| `JWT_SECRET` | Backend boot (≥32 bytes, not placeholder) |
| `ACTUATOR_USER` / `ACTUATOR_PASSWORD` | Health/metrics auth |
| `POSTGRES_EXPORTER_*` | Infra stack if started |
| Grafana / Sonar keys | Only if `start-all` / Sonar runs in env |

Also required **outside** `.env`:

| Secret | Purpose |
|--------|---------|
| GitHub token / `gh` auth | Issue/PR/merge (Cloud agent identity) |
| Any Cursor Cloud secrets for private deps | If applicable |

---

## 3. Boot / start scripts

Prefer the repo scripts wired on the **Saved** Environment card:

```bash
# environment.json (Saved card)
install: bash .cursor/install.sh
start:   bash .cursor/start.sh
```

`.cursor/install.sh` is idempotent (JDK/Maven/Node/Docker/`bc`/OpenSpec/Bruno CLI,
`.env`, frontend deps, Maven reactor). `.cursor/start.sh` starts `dockerd` then brings up
the stack with host-network compose:

```bash
export COMPOSE_FILE=docker-compose.yml:docker-compose.cloud.yml
bash workspace/stack/start.sh     # DB + backend; frontend may be separate
# for full autonomy including observability: bash workspace/stack/start-all.sh
```

Health before E2E / Bruno:

```bash
curl -sf http://localhost:8080/actuator/health
```

---

## 4. PATH / CLI expectations for agents

Agents assume these commands work without interactive prompts:

- `mvn`, `java`, `node`, `npm`, `npx`, `docker`, `docker compose`, `gh`, `openspec`, `bru`, `bc`
- `bash scripts/preflight.sh`, `bash scripts/validate-sdlc-plan.sh`
- `bash scripts/seed-openspec-change.sh` (Gate 1 scaffold — prefer before filling artifacts)
- `bash workspace/stack/start.sh` / `stop.sh` / `run_pipeline.sh`

Install OpenSpec CLI the same way CI/devs do for this repo (document the exact
install line in the environment build when finalized). If `openspec` is missing,
Gate 1 cannot pass. Bruno CLI ≥4.2.0 (`bru`) is pinned by `.cursor/install.sh` so
OpenCollection API tests work without relying on unpinned `npx @usebruno/cli`.

---

## 5. Resource notes

| Resource | Guidance |
|----------|----------|
| Disk | Room for `~/.m2`, `frontend/node_modules`, Docker images, Playwright browsers |
| Memory | Maven + Next + Postgres + Playwright: prefer ≥8 GB container RAM |
| Nested Docker | Required for `workspace/stack/start.sh` and CI-parity smoke |
| Network | Access to Maven Central, npm, GitHub, Playwright browser download hosts |

---

## 6. Explicit non-requirements (cloud path)

- [ ] ~~oMLX / Metal / macOS~~
- [ ] ~~Codex CLI with `omlx` profile~~
- [ ] ~~`local-ai/sdlc/foreman.sh`~~
- [ ] ~~Sibling worktree `../notaire-localai`~~

---

## 7. Process learnings (Cloud fleet — from PRs #1111 / #1112 / #1116)

Hard rules discovered while landing the fleet. Also restated for the foreman in
[`FLEET-ARCHITECTURE.md`](FLEET-ARCHITECTURE.md).

| Learning | Do | Do not |
|----------|----|--------|
| **Issue close keyword** | Put `Closes #<issue>` in commit messages (and PR body). GitHub only auto-closes on merge with closing keywords. | Rely on `Issue: #N` or a body mention alone — the issue stays **OPEN** after merge. |
| **PR Validation wiki** | Leave wiki/CI reports to workflows that do **not** commit onto the PR head. Fixed on `main` in `pr-validation.yml` (#1111 / #1117). | Reintroduce committing `docs/wiki/cicd-reports/pr-validation-*.md` onto PR heads with `[skip ci]` — that moves HEAD to a SHA with an empty check suite and stalls merge-when-green. |
| **Nested Docker** | Use `docker-compose.cloud.yml` (host network). | Assume bridge networking between containers works in Cloud VMs. |
| **Run install until Saved** | Run `bash .cursor/install.sh` when `openspec`/`bc`/Maven/Docker are missing; Save the Environment card so new boots wire it automatically. | Assume draft builds already put OpenSpec/`bc` on PATH without running install. |
| **Saved Environment card** | Save `install=bash .cursor/install.sh` and `start=bash .cursor/start.sh` on the Environment card. | Treat draft builds from feature branches as a substitute for a Saved card. |
| **OpenSpec Gate 1 seed** | Prefer `bash scripts/seed-openspec-change.sh <name> --issue N --use-case "CU…" --branch … --create` before filling artifacts (#1108 / #1116). | Hand-write empty proposal/design/tasks from scratch (templates get rejected by `validate-sdlc-plan.sh` when `<!-- -->` bodies remain). |
| **Light CI ≠ mergeable** | Wait until Unit, Integration, Coverage Gate, Bruno, and Playwright are terminal **success** (or workflow-skipped). Run `bash scripts/check-heavy-ci.sh <pr>` before merge. See [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md). | Treat PR Validation + Frontend + SDLC (~12 checks) as “all green” while `CI - Build, Test & Security` / Playwright are still **pending**. Docs-only tips still run Playwright unless the workflow is skipped. |
| **Stale tip ≠ product bug** | If Integration/Playwright fail with Budget/person / `undefined, undefined` and the branch is behind `main`, **rebase onto `main` first** (#1132 nested `BudgetResponse.person`). | Invent product fixes for failures already fixed on `main`. |
| **CodeQL advanced vs default** | Keep code scanning on `.github/workflows/codeql.yml`. Use `wait-for-processing: false` where appropriate; disable default setup via `bash scripts/enable-gh-secure.sh --apply` (admin). See [DevSecOps](../../200-architecture/208-devsecops/README.md#codeql-advanced-vs-default-setup). | Enable GitHub Code Scanning default setup alongside the advanced workflow — SARIF upload is rejected and Analyze fails. |
| **Serialize heavy CI** | Prefer one heavy-CI PR at a time; docs/rebase tips wait; do not open new product PRs until the in-flight suite finishes. Always `bash scripts/check-heavy-ci.sh <pr>` (subscription “all N success” can be light-only). See [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md#runner-contention--serialize-heavy-ci). | Push many tips that each enqueue Playwright while another PR’s heavy suite is still queued. |

---

## 8. Handoff to environment.json author

Use this checklist as the source list for install steps and secrets. The Saved
card must already point at `.cursor/install.sh` / `.cursor/start.sh` on the
default branch; translate any remaining version pins into the dashboard only when
they differ from those scripts.
