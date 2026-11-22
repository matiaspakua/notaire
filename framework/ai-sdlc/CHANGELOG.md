# Changelog — aisdlc kit

All notable changes to the **AI SDLC kit** (`framework/ai-sdlc`) are documented here.
Kit semver is independent of the Notaire product release-please train.
See [`docs/UPGRADE.md`](docs/UPGRADE.md) for the deprecation window.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- `harness report` refuses adapter ids not returned by `harness list` (exit 2, `unknown_adapter`), but always accepts the kit's reference adapter `local-worker` without registration (#2385).

### Added

- `aisdlc store sync|stats` and `aisdlc telemetry record|report`: persistent SQLite store (`.aisdlc/factory.db`, git-ignored) mirroring the evidence ledger with idempotent sync, redacted telemetry rows, and grouped reports (#2394).
- `GET /factory/telemetry?group=…` (read scope) on `aisdlc serve`: aggregated time, cost, and failure counts from the store (#2394).
- `aisdlc status [--json]`: live factory status derived from the evidence ledger and the store — current intent/run/step, previous steps with duration and cost, next steps; never invents a running state (#2397).
- `GET /factory/status` (read scope) on `aisdlc serve`: same live status payload as the CLI (#2397).
- `GET /factory/events` (read scope) on `aisdlc serve`: Server-Sent Events stream of ledger rows with Last-Event-ID replay and keep-alive (#2397).
- Contract `factory-control` 1.4.0 (additive — new `FactoryStatus` schema and two paths).
- Contract `factory-control` 1.3.0 (additive — new `TelemetryReport` schema and path).
- Intent model (submit/list/show), spec parser, reference intents, MCP stdio server, and API endpoints for intent lifecycle (#2395).
- `aisdlc line [--json]` and `GET /factory/line`: the production line model (ordered stages with gates, agents, skills, tools/MCP, model, effort, inputs/outputs, guardrails); optional `inputs`/`outputs` on `[[stages]]` with kit defaults; contract `factory-control` 1.3.0 (#2396).
- `GET /factory/config/files` (tree) and `GET /factory/config/files/{path}` (read with redaction and confinement) for config-as-code (#2398).
- `DELETE /factory/catalogs/{kind}/{id}` with If-Match, CSRF, write scope, and evidence (#2398).
- CLI `config files`, `config show <path>`, `catalog remove --if-match` (#2398).
- Contract `factory-control` 1.3.0 (additive) (#2398).
- `aisdlc catalog import --harness H --from DIR [--kind K]` reads harness layouts (`claude-code`, `cursor`, `codex`, `junie`, `kiro`, `agy`) as UTF-8 bytes, preserves CRLF, stores content in the `files` revision field, rejects literal secrets (exits 1), idempotent when unchanged (#2399).
- `aisdlc catalog materialize --harness H --dest DIR [--kind K] [--check]` projects catalog entries into a harness layout, writes `.aisdlc-materialized.json` manifest with SHA-256 digests; `--check` verifies disk without writing and exits 1 on drift; stale files never deleted (#2399).
- Catalog revision `files` field with literal-secret guard — entries containing secret-shaped values are rejected before storage (#2399).
- `agy` and `kiro` harness adapter stubs registered in the Notaire profile (`framework/ai-sdlc/plugins/agy/`, `framework/ai-sdlc/plugins/kiro/`) (#2399).
- Junie harness adapter stub registered in the Notaire profile (`framework/ai-sdlc/plugins/junie/`) (#2385).
- `aisdlc serve-token add|list|revoke`: named serve tokens, one per person, each with its scope; the identity is the actor of every write (#2384).
- `GET /factory/session` names the caller; `POST /factory/catalogs/{kind}` (declared in the contract but not served before) creates entries; `PUT` accepts `fields`. A credential field takes only `env:NAME`; a literal secret is refused with `literal_secret` (#2384).
- `aisdlc coach` and owner-only `GET /factory/coach`: local harness session counts and the audit anti-patterns, never prompt text (#2384). Junie CLI added as a supported harness (#2387).
- Factory dashboard v2 (`factory-dashboard/`, optional, Node): a client of the serve contract for many workspaces and users, with RepoLens search and the coach (#2384, ADR-006).
- Contract `factory-control` 1.2.0 (additive).

- Foreman template: a forge write that returns 403 is an Orchestrator action for the owner. The loop continues. The kit version stays 1.0.0; this is template text, not a package behavior change.
- Kit tag release workflow (`aisdlc-v*`): wheel, sdist, CycloneDX SBOM and provenance attestation on the GitHub Release (#2256).
- Conformance matrix for stock profiles `go-service`, `java-maven`, `node-web`, `python-service` against an installed wheel (#2256).
- Second-adopter sample (`samples/second-adopter`) with measured quickstart budget ≤ 15 minutes (#2256).
- Profile `[project] schema_version` (default 1); `init` / `install` write it (#2256).
- `aisdlc foreman claim`: claim-protocol refuse when intake fails under `[intake] required` (#2257).
- `aisdlc intake board`: clean-board report over `[tracker] list` / `--file`; raise-only evidence before flipping enforcement (#2257).
- Profile `[intake] required` (default false / advisory) and `[tracker] list` (#2257).

### Fixed

- Serve token files are git-ignored through `.aisdlc/.gitignore`; before, `git add -A` in an adopter repository could commit a live token (#2384).

### Changed

- RepoLens moved out of the kit to `framework/repolens`; the kit's boundary scan covers its core and dashboard, and RepoLens checks its own side (#2384).

### Notes

- Notaire adopter stays advisory (`required = false`); do not raise without a clean board report.
- PyPI publishing remains an Owner decision; this train does not upload to PyPI.

## [1.0.0] — 2026-11-11

### Added

- `pyproject.toml` wheel/sdist packaging; console script `aisdlc`; kit data under `aisdlc/_kit` (#2256 slice 1 / #2266).
- Optional `[project] kit_version`; `aisdlc doctor` reports skew (#2256 slice 1).
