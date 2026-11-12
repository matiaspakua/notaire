# Kit packaging remainders — design

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Slice 1 shipped `pyproject.toml`, wheel/sdist offline build, and `kit_version`
skew (#2266). Remaining #2256 ACs: tagged releases with SBOM/provenance, the
four-profile conformance matrix, second-adopter proof via the wheel, profile
`schema_version`, and kit CHANGELOG/upgrade policy. Phase E (#2276) already
proved a kit-neutral temp fixture; this change extends that with wheel install
and timing.

## Goals / Non-Goals

**Goals:**
- `aisdlc-v*` GitHub Release: wheel + sdist + CycloneDX SBOM + provenance attestation
- Conformance matrix for `go-service`, `java-maven`, `node-web`, `python-service`
- Second-adopter sample adopting via wheel; ≤ 15 min to first verified gate
- Profile `[project] schema_version`; kit CHANGELOG + one-minor deprecation window

**Non-Goals:**
- PyPI publish (Owner decision; documented)
- Kit repository split (P5)
- Context Lake / Wilson / `required=true`
- Root CHANGELOG Unreleased (fence #2271)

## Decisions

1. **Kit tags `aisdlc-vX.Y.Z`** — independent of Notaire release-please `v*`. One
   workflow `.github/workflows/aisdlc-kit.yml` owns release-on-tag and PR/push
   conformance for `framework/ai-sdlc/**`.
2. **SBOM** — Trivy filesystem CycloneDX on `framework/ai-sdlc` (and/or the built
   dist); attach `sbom-aisdlc.cdx.json` to the Release. Provenance via
   `actions/attest-build-provenance` (or `actions/attest`) on wheel+sdist with
   `id-token: write` + `attestations: write`.
3. **Conformance fixtures** — start from stock `profiles/*.toml`; overlay offline
   adaptations (policy path, tracker `verify = "true"`, PATH stubs for stack
   tools that `expect = fail` on red). Assert exit 0 for install/doctor/change
   new/plan/red/report. Go stock profile lacks red — fixture adds an offline red
   stub so the matrix is uniform while keeping `[project] name`.
4. **Second adopter** — in-tree public sample `samples/second-adopter/` (shop)
   plus `tests/test_second_adopter.py` that builds the wheel, installs into a
   venv, runs install→doctor→change→gate plan and records elapsed seconds ≤ 900.
5. **`schema_version`** — optional integer on `[project]`; absent means 1 for
   compat; `init`/`install` write `1`; non-integer / `<1` fails profile load.
6. **CHANGELOG** — kit-local only; `docs/UPGRADE.md` states one-minor deprecation.
7. **PyPI** — README + UPGRADE note: non-goal until Owner asks.

## Riesgos / Trade-offs

- [Offline stubs ≠ full stack tools] → Fixtures document stubs; stock profiles
  remain the language examples; matrix proves kit+profile wiring via the wheel.
- [Attestation only on tags] → PR job runs matrix without attest; release job
  attests. Documented.
- [15-minute bound flaky in slow CI] → Bound is wall-clock in the test; skip only
  when venv unavailable (same as packaging).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| release workflow builds and attaches assets | unit (workflow present + script dry) | `tests/test_release_workflow.py` |
| matrix profile X install→…→report exit codes | unit/integration | `tests/test_conformance_matrix.py` |
| second adopter wheel quickstart ≤ 15 min | unit/integration | `tests/test_second_adopter.py` |
| schema_version init + invalid rejected | unit | `tests/test_profile_schema_version.py` |
| independence still passes | unit | `tests/test_independence.py` |
| kit CHANGELOG + upgrade policy present | unit | `tests/test_release_workflow.py` |

## Regression Strategy

- Existing packaging / kit_version / independence / adopter-demo tests stay green
- `bash framework/ai-sdlc/verify.sh` then `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

- n/a — kit-only packaging / CI / samples; no Notaire product UI tip

## Open Questions

- None for Gate 1. PyPI remains Owner-gated.

## Deployment Strategy

- Merge via PR; first kit release when Owner pushes `aisdlc-v*` tag

## Rollback Strategy

- Revert the PR; delete the kit tag/release if needed

## Migration Plan

- Profiles without `schema_version` keep working (default 1); `init` writes it going forward
