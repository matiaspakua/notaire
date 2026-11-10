# Cloud preflight: python3-venv + Playwright chromium/webkit

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2217 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/2217_cloud-env-preflight-deps` |
| Gate 1 status | passed |

## Objetivo

Cursor Cloud preflight (`bash workspace/sdlc/preflight.sh --full`) fails mid-run
when `python3-venv` / ensurepip is missing (`security/run_gates.py` cannot create
`security/.tools/venv`) and when Playwright browsers for `github-page` (chromium /
headless shell) and `testing/e2e` mobile (WebKit) were never installed by
`.cursor/install.sh`. Fix the cloud install bootstrap so agents do not apt-install
or download browsers mid-preflight.

## What Changes

- `.cursor/install.sh` installs `python3-venv` (ensurepip) with the existing apt block.
- `.cursor/install.sh` installs npm deps and Playwright browsers for `testing/e2e`
  (chromium + webkit) and `github-page` (chromium), in addition to frontend chromium.
- Document the deps in `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md`
  and comments in `.cursor/install.sh`.
- Static unittest guards the install script contract.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Cloud install SHALL provide python3-venv so local security gates create a venv without apt mid-run | CU76 / #2217 | New |
| Cloud install SHALL install Playwright chromium + webkit for the packages used by preflight (`github-page`, `testing/e2e`) | CU76 / #2217 | New |
| Environment checklist SHALL document python3-venv and the Playwright browser set | CU76 / ENVIRONMENT-CHECKLIST | Made explicit |

## Capabilities

### New Capabilities

- `cloud-env-preflight-deps`: Cursor Cloud install must ship python3-venv and the
  Playwright browser set required by preflight `--full`.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `.cursor/install.sh` | yes | python3-venv + Playwright installs |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | checklist rows |
| `workspace/tests/` | yes | static contract test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: apt `python3-venv`; Playwright browser binaries for e2e + pages

### Write set

- `.cursor/install.sh`
- `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md`
- `docs/openspec/changes/improve-2217-cloud-env-preflight-deps/**`
- `workspace/tests/test_cloud_env_preflight_deps.py`
- `CHANGELOG.md`

### Architecture review

No application-architecture change. No ADR. Cloud install / process tooling only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | python3-venv + Playwright chromium/webkit for e2e and github-page |
| `CHANGELOG.md` | n/a — not user-visible product change; optional process note |

## Out of Scope

- Product test fixes unrelated to missing browsers/venv.
- Wilson / Portal / Context Lake.
- Setting `[committer] required=true`.
- Reopening or implementing #2218 / #2221.
