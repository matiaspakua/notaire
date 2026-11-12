# Changelog — aisdlc kit

All notable changes to the **AI SDLC kit** (`framework/ai-sdlc`) are documented here.
Kit semver is independent of the Notaire product release-please train.
See [`docs/UPGRADE.md`](docs/UPGRADE.md) for the deprecation window.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- Kit tag release workflow (`aisdlc-v*`): wheel, sdist, CycloneDX SBOM and provenance attestation on the GitHub Release (#2256).
- Conformance matrix for stock profiles `go-service`, `java-maven`, `node-web`, `python-service` against an installed wheel (#2256).
- Second-adopter sample (`samples/second-adopter`) with measured quickstart budget ≤ 15 minutes (#2256).
- Profile `[project] schema_version` (default 1); `init` / `install` write it (#2256).
- `aisdlc foreman claim`: claim-protocol refuse when intake fails under `[intake] required` (#2257).
- `aisdlc intake board`: clean-board report over `[tracker] list` / `--file`; raise-only evidence before flipping enforcement (#2257).
- Profile `[intake] required` (default false / advisory) and `[tracker] list` (#2257).

### Notes

- Notaire adopter stays advisory (`required = false`); do not raise without a clean board report.
- PyPI publishing remains an Owner decision; this train does not upload to PyPI.

## [1.0.0] — 2026-11-11

### Added

- `pyproject.toml` wheel/sdist packaging; console script `aisdlc`; kit data under `aisdlc/_kit` (#2256 slice 1 / #2266).
- Optional `[project] kit_version`; `aisdlc doctor` reports skew (#2256 slice 1).
