# Cloud Environment Requirements Checklist

Checklist for the Cursor Cloud **environment.json** / environment build that
hosts the AI SDLC foreman fleet. Populate secrets from `.env.example` keys
(never commit `.env`).

> This fleet does **not** require `local-ai/`, oMLX, or Codex local profiles.

---

## 1. Base toolchain (install / snapshot)

| Component | Required version | Why | Verify |
|-----------|------------------|-----|--------|
| JDK | **21** | Spring Boot 4.1 backend | `java -version` |
| Maven | **3.9+** | `mvn test`, `verify`, Spotless/Checkstyle | `mvn -version` |
| Node.js | **22.x LTS** (or project engines) | Next.js 16 frontend | `node -v` |
| npm | bundled with Node | `npm ci`, Vitest, Playwright | `npm -v` |
| Docker Engine + Compose | **24+** | `scripts/start.sh`, integration/smoke | `docker version` && `docker compose version` |
| Git | 2.40+ | branches, hooks | `git --version` |
| GitHub CLI `gh` | recent | issues, PR checks, merge | `gh auth status` |
| OpenSpec CLI | current project-supported | Gate 1 `openspec validate` | `openspec --version` |
| Python 3 | 3.11+ | occasional scripts / unittest helpers | `python3 --version` |
| `curl` / `jq` | any | health checks, JSON parsing | `curl --version`; `jq --version` |

### Frontend / E2E extras

| Component | Notes | Verify |
|-----------|-------|--------|
| `frontend/node_modules` | `cd frontend && npm ci` in install or first boot | `test -d frontend/node_modules` |
| Playwright browsers | `cd frontend && npx playwright install --with-deps` (or CI-equivalent) | `npx playwright --version` |
| Bruno / API collection | Used by `preflight.sh --full` / `playwright-e2e.yml` | Collection under `backend-api/api-test/` present |
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

Wire into environment `install` / `start` as appropriate:

```bash
# install (idempotent)
cp -n .env.example .env   # or inject secrets
# ensure JDK21, Maven, Node, Docker, gh, openspec on PATH
cd frontend && npm ci && npx playwright install --with-deps

# optional warm caches
mvn -q -B -pl backend-api -am dependency:go-offline || true

# start (detached services for Gate 3)
bash scripts/start.sh     # DB + backend; frontend may be separate
# for full autonomy including observability: bash scripts/start-all.sh
```

Health before E2E / Bruno:

```bash
curl -sf http://localhost:8080/actuator/health
```

---

## 4. PATH / CLI expectations for agents

Agents assume these commands work without interactive prompts:

- `mvn`, `java`, `node`, `npm`, `npx`, `docker`, `docker compose`, `gh`, `openspec`
- `bash scripts/preflight.sh`, `bash scripts/validate-sdlc-plan.sh`
- `bash scripts/start.sh` / `stop.sh` / `run_pipeline.sh`

Install OpenSpec CLI the same way CI/devs do for this repo (document the exact
install line in the environment build when finalized). If `openspec` is missing,
Gate 1 cannot pass.

---

## 5. Resource notes

| Resource | Guidance |
|----------|----------|
| Disk | Room for `~/.m2`, `frontend/node_modules`, Docker images, Playwright browsers |
| Memory | Maven + Next + Postgres + Playwright: prefer ≥8 GB container RAM |
| Nested Docker | Required for `scripts/start.sh` and CI-parity smoke |
| Network | Access to Maven Central, npm, GitHub, Playwright browser download hosts |

---

## 6. Explicit non-requirements (cloud path)

- [ ] ~~oMLX / Metal / macOS~~
- [ ] ~~Codex CLI with `omlx` profile~~
- [ ] ~~`local-ai/sdlc/foreman.sh`~~
- [ ] ~~Sibling worktree `../notaire-localai`~~

---

## 7. Handoff to environment.json author

Use this checklist as the source list for install steps and secrets. Sibling
task “Map env requirements” should translate rows into concrete
`environment.json` `install`/`start` commands once versions are confirmed on
the Cloud snapshot.
