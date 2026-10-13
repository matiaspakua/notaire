# Docs: fix docs/tests failing on main (#1582)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1582 |
| Use Case | N/A – documentation hygiene |
| Branch | `fix/1582_docs_tests_main` |
| Gate 1 status | draft |

## Objetivo

After #1572, `python3 -m unittest discover -s docs/tests` failed on main: CHANGELOG `[Unreleased]` had two `### Changed` sections and RELEASE.md used a relative placeholder link the link checker rejects. Every PR's quick.sh failed on it.

## What Changes

- CHANGELOG `[Unreleased]`: one `### Changed` section; the #1571 entry moved under it (no text lost).
- `docs/300-development/RELEASE.md`: release heading placeholder uses an absolute compare URL.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| CHANGELOG has one section per type in [Unreleased] | N/A | Existing (docs test) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `docs-quality`: Documentation checks that run on every PR.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `docs` | yes | RELEASE.md |
| `root` | yes | CHANGELOG.md |

### Surface area

- Endpoints / entities / Flyway / configuration: none

### Architecture review

Documentation only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | section merge |
| `docs/300-development/RELEASE.md` | placeholder link |
