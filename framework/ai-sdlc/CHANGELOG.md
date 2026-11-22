# Changelog — aisdlc kit

All notable changes to the **AI SDLC kit** (`framework/ai-sdlc`) are documented here.
Kit semver is independent of the Notaire product release-please train.
See [`docs/UPGRADE.md`](docs/UPGRADE.md) for the deprecation window.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- `harness report` refuses adapter ids not returned by `harness list` (exit 2, `unknown_adapter`), but always accepts the kit's reference adapter `local-worker` without registration (#2385).

### Added

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
