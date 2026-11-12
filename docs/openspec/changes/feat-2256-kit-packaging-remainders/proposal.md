# Kit packaging remainders — release, conformance matrix, second adopter

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2256 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2256_kit-packaging-remainders-79dc` |
| Gate 1 status | passed |

## Objetivo

Slice 1 (#2266) shipped the wheel, sdist basics and `kit_version` skew. Adopters still
cannot consume tagged kit releases with SBOM/provenance, the four stock profiles are
never exercised against an installed wheel in CI, and second-adopter proof beyond the
Phase E temp demo remains unmeasured. This change closes the remaining #2256
acceptance criteria (P1 release assets, P2 conformance matrix, P3 second adopter,
profile `schema_version`, kit CHANGELOG/upgrade policy). PyPI publishing stays an
Owner decision (documented non-goal).

## What Changes

- Kit-tag release workflow (`aisdlc-v*`): build wheel + sdist, attach CycloneDX SBOM
  and GitHub artifact provenance attestation to the GitHub Release; never publish to PyPI.
- Conformance matrix: for each profile (`go-service`, `java-maven`, `node-web`,
  `python-service`) a fixture installs the built wheel and runs
  `install → doctor → change new → gate plan → gate red → report`, asserting exit codes
  (offline stubs for stack tools; stock profile identity preserved).
- Second-adopter sample under `framework/ai-sdlc/samples/second-adopter` (non-Notaire)
  plus a timed wheel-adoption test that records time-to-first-verified-gate ≤ 15 minutes.
- Optional `[project] schema_version` on the profile; `init`/`install` write it; doctor
  rejects an invalid value.
- Kit-local `CHANGELOG.md` + upgrade/deprecation notes (one minor version window).
  Notaire root `CHANGELOG.md` `[Unreleased]` omitted (fence #2271).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Kit tags build wheel+sdist with SBOM and provenance on the GitHub Release; PyPI is Owner-gated | #2256 AC; #2251 §9/§12 | New |
| Each stock profile is proven against an installed wheel via the conformance matrix exit codes | #2256 AC; FACTORY-PRODUCT-ANALYSIS P2 | New |
| A non-Notaire sample adopts via the wheel with measured quickstart ≤ 15 minutes | #2256 AC; P3 | New |
| Profile may record `schema_version`; invalid values fail closed | #2256 AC; P4 (profile half) | New |
| Kit CHANGELOG carries upgrade notes; deprecated behaviour survives one minor | #2256 AC | New |
| Independence test still passes; no adopter product paths in kit code/templates | #2256 AC; MODULE.md | Made explicit |

## Capabilities

### New Capabilities

- _(none — extends existing `kit-packaging`)_

### Modified Capabilities

- `kit-packaging`: release workflow, conformance matrix, second-adopter proof,
  profile `schema_version`, kit CHANGELOG/upgrade policy.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | kit release + conformance workflow |
| `workspace` / `framework/ai-sdlc` | yes | profile schema_version, tests, samples, docs, CHANGELOG |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none at runtime (stdlib only); CI uses setuptools/trivy/attest actions

### Write set

- `framework/ai-sdlc/**`
- `.github/workflows/aisdlc-kit.yml`
- `docs/openspec/changes/feat-2256-kit-packaging-remainders/**`
- `docs/openspec/specs/kit-packaging/**`
- `docs/300-development/306-ai-sdlc-framework/**`

### Architecture review

Follows existing kit packaging (ADR-026 / slice 1). No new architecture ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Release tags, matrix, second adopter, schema_version, PyPI non-goal |
| `framework/ai-sdlc/MODULE.md` | Contract: release tags, matrix, schema_version |
| `framework/ai-sdlc/CHANGELOG.md` | Kit-local changelog + upgrade notes (new) |
| `framework/ai-sdlc/docs/UPGRADE.md` | Deprecation window policy (new) |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-PRODUCT-ANALYSIS.md` | Mark P1–P3 remainders shipped |
| Root `CHANGELOG.md` | n/a — omit Unreleased (fence #2271) |

## Out of Scope

- Publishing to PyPI without Owner ask (documented TODO)
- Moving the kit to its own repository (`REPO-SPLIT-PLAN.md` / P5)
- Context Lake, Wilson vanity, `[committer] required=true`
- Contending with MFA docs #2271
- Notaire product Playwright tip
