# Factory as a reusable product — gap analysis

Design record for [#2251](https://github.com/matiaspakua/notaire/issues/2251) ·
Use Case **CU76** · epic [#2238](https://github.com/matiaspakua/notaire/issues/2238) ·
lineage [#2134](https://github.com/matiaspakua/notaire/issues/2134),
[#1958](https://github.com/matiaspakua/notaire/issues/1958),
[#2136](https://github.com/matiaspakua/notaire/issues/2136),
[#2139](https://github.com/matiaspakua/notaire/issues/2139),
[#2142](https://github.com/matiaspakua/notaire/issues/2142).

| Field | Value |
|-------|-------|
| Status | Analysis accepted as the shared contract; Owner decisions open (see [`FACTORY-UI-SPEC.md`](FACTORY-UI-SPEC.md)) |
| Companion | [`FACTORY-UI-SPEC.md`](FACTORY-UI-SPEC.md) — architecture, contract, UI, tests, slices, coordination |
| Parent plan | [`FACTORY-ROADMAP.md`](FACTORY-ROADMAP.md) |
| Verified against | `main` `d90e106` (the issue was written against `67aa1cc4`) |

Every finding cites a file or issue. Facts were re-checked against `main` before this
document was written; the corrections are listed in section 1.1.

## 1. Verdict

The kit is strong on policy, gates, evidence, ratchets, ports and honesty rules (axioms 1-7 in
[`FACTORY-ROADMAP.md`](FACTORY-ROADMAP.md)). Two things block the UI phases as written, and four
more block reuse outside Notaire:

1. **The UIs have no host.** `framework/ai-sdlc/aisdlc/contract.py` states the kit hosts no
   server, and the CLI has no `serve` command. Phases B-D say "adapters over CLI/OpenAPI", but
   nothing implements the HTTP paths that `openapi/factory-control.openapi.yaml` declares.
2. **Runs and projects do not exist as entities.** Phase D asks to "create project", "start run"
   and "monitor progress". The contract has no Project, Run or RunRequest schema, and
   `aisdlc foreman plan|route` is routing-only by design (`aisdlc/adopt.py`, #2196). Nothing
   defines what "start" does or who executes.
3. **The workflow entity is a reference bag.** `workflows.create` stores
   `body = {agents, skills, mcp, providers}` as id lists: no ordered steps, no per-step gates or
   budget, and references are not pinned to versions.
4. **Packaging is copy-a-directory.** `aisdlc/__init__.py` hardcodes `__version__ = "1.0.0"`;
   there is no `pyproject.toml`, wheel or release artifact. Adopters cannot pin or upgrade by
   version.
5. **Adopter count is 1.** The independence test proves no Notaire strings in kit code, not that
   a second project works. Profiles for go, node, python and java exist
   (`framework/ai-sdlc/profiles/`), but no CI job installs the kit into a fixture repo of each.
6. **The phase issues lacked Definition of Ready.** No labels, `#2240` carried a dangling
   `[#TBD-link-in-plan]`, Phase B bundled a shell plus three CRUD modules (well over the 400
   counted-line review budget), and no issue had a test plan. Repaired on 2026-11-11 (section 2,
   F12).

### 1.1 Corrections made while verifying

| Claim in #2251 | State on `main` `d90e106` | Treatment |
|----------------|---------------------------|-----------|
| F4: OpenAPI paths are "config, plugins, harness, report, catalogs, workflows" | The document also declares `/factory/catalogs/{kind}/{id}`, `/factory/catalogs/{kind}/{id}/versions`, `/factory/workflows/{id}` and `/factory/workflows/{id}/transitions` | Evidence corrected in F4; the conclusion holds: no Project, Run or RunRequest path or schema |
| F11: "no Notaire Playwright while #2088 is in flight" | #2088 is closed | Rationale weakened; the decision stands because kit UI tests belong to the kit and must not depend on a product suite |
| F12 / item 6: `#2240` carries a dangling `#TBD` | Fixed: `#2240` body has slices B1-B4 and a test plan, and all five phase issues carry labels | Recorded as repaired; the intake lint (section 10) stays valuable because the defect class can recur |
| Section 1 item 1: the HTTP paths are `/factory/workflows`, `/factory/catalogs/{kind}/{id}/versions`, ... | Both paths are declared | No change; the contract exists, the host does not |

All other cited facts hold: `contract.py` docstring ("The kit does not host a server"), no `serve`
subparser in `cli.py`, `workflows.py` body shape and `_resolve_refs` existence-only check,
`__version__ = "1.0.0"`, no `pyproject.toml` anywhere in the repository root or kit, no
`schema_version` anywhere under `framework/ai-sdlc`, and `catalogs.py` allocating `version + 1`
at lines 293 and 324.

## 2. Findings (evidence)

| # | Finding | Evidence | Consequence |
|---|---------|----------|-------------|
| F1 | No HTTP/UI host in the kit | `contract.py` docstring; no `serve` in the `cli.py` subparsers | Phase B cannot start without an invented host; two agents will invent two |
| F2 | Logic and IO are mixed | `workflows.create`, `list_workflows` and `show` print and return an exit code (`-> int`); `catalogs.list_entries`, `show` and `history` already return dicts | A UI/HTTP adapter would have to scrape stdout. The service layer must return data; the CLI formats |
| F3 | Workflow body is thin and unpinned | `workflows.py` `body = {...}` id lists; `_resolve_refs` checks existence only | An `approved` workflow silently changes meaning when a skill or agent gets a new version |
| F4 | No Project/Run/RunRequest | `openapi/factory-control.openapi.yaml` paths: config, plugins, harness, report, catalogs (`{kind}`, `{kind}/{id}`, `{kind}/{id}/versions`), workflows (`/`, `{id}`, `{id}/transitions`) | Phase D has no contract to implement |
| F5 | Concurrent writers | Catalogs are files; version allocation is `version + 1` (`catalogs.py:293`, `:324`). UI, CLI and agents can write the same entity | Lost update unless writes carry the expected version or digest (verify with a two-writer test) |
| F6 | Actor identity on writes | Axiom 4: authority comes from token and channel, never the body | The HTTP host must derive the actor from a token scope; no `actor` field in any request body |
| F7 | Packaging | No `pyproject.toml`; `__version__ = "1.0.0"`; `install` copies the directory | No semver pinning, no upgrade notes, no provenance |
| F8 | Adopter proof n=1 | `tests/test_independence.py` scans for adopter markers only | "Reusable" is unproven |
| F9 | Schema evolution | Entities have `version` but no `schema_version`; no `aisdlc migrate` | Workflow body v2 would break existing entries |
| F10 | Design-system coupling risk | Notaire UI rules (`.claude/rules/ui-ux-design.md`, `frontend/src/theme`) are product-bound | The kit UI must ship its own neutral tokens, not import Notaire's |
| F11 | Test contention | The epic forbade Notaire Playwright runs while #2088 was in flight (now closed) | Solved structurally: kit UI tests live in the kit with their own job, never in `testing/e2e` |
| F12 | Issue quality | #2240-#2243 bodies at the time of the analysis | Repaired 2026-11-11; see the intake lint in section 10 |

## 9. Making the factory a reusable product

Numbering follows the issue; sections 3-8 and 11 live in [`FACTORY-UI-SPEC.md`](FACTORY-UI-SPEC.md).

1. **Packaging (P1).** `pyproject.toml` (stdlib-only runtime, `aisdlc` entry point), semver
   independent of Notaire's release-please, `CHANGELOG`, wheel and sdist as GitHub Release assets
   with SBOM and provenance attestation; `pipx install aisdlc==X` and `aisdlc install` both work.
   The kit version is recorded in the adopter profile; `aisdlc doctor` reports skew.
2. **Conformance matrix (P2).** A CI job that, for each of `go-service`, `java-maven`, `node-web`
   and `python-service`, creates a fixture repo and runs
   `install -> doctor -> change new -> gate plan/red -> report`, asserting exit codes. This
   replaces "independent" claims with a measured result.
3. **Second adopter (P3).** One non-Notaire repo adopts via the wheel (a public sample is
   enough). Target: time to first verified gate ≤ 15 minutes from the quickstart, measured and
   recorded.
4. **Compatibility (P4).** Profile and entity `schema_version`, `aisdlc migrate`, a deprecation
   policy of one minor version, and upgrade notes in the `CHANGELOG`.
5. **Extraction readiness (P5).** `framework/ai-sdlc` builds, tests (`verify.sh`) and releases
   from its own workflow; moving it to its own repository (see
   [`REPO-SPLIT-PLAN.md`](../REPO-SPLIT-PLAN.md)) is then a subtree split with no code change.
6. **Policy packs (P6).** The Constitution template and card per domain (web, data, mobile) as
   optional overlays; adopters keep one policy file.
7. **Scope guard (P7).** No Context Lake, Port.io clone, hosted compute or interface builder
   (the epic refusals stay).

Tracking issues: P1, P2 and P3 are [#2256](https://github.com/matiaspakua/notaire/issues/2256);
P4 spans [#2254](https://github.com/matiaspakua/notaire/issues/2254) (`schema_version`,
`migrate`) and #2256 (profile skew); P5 and P6 have no issue yet and wait for P1.

## 10. Improvements for the Notaire AI SDLC itself

- **Issue intake lint.** A kit gate that rejects an issue lacking Use Case, acceptance
  checkboxes, out-of-scope, test plan, size estimate or a resolvable dependency link (it would
  have caught the dangling `#TBD` in #2240). Tracked in
  [#2257](https://github.com/matiaspakua/notaire/issues/2257).
- **Remote attestation.** `preflight --full` is local; GitHub does not re-run it
  ([#2001](https://github.com/matiaspakua/notaire/issues/2001)). Publish the result as a
  ForgePort `factory/gate` check-run
  ([#2208](https://github.com/matiaspakua/notaire/issues/2208)) bound to the head SHA so the
  merge rule verifies evidence instead of trusting the agent.
- **Outcome SLOs.** Lead time (issue open to PR green), clean-pass rate and cost per change
  exist in `aisdlc report`; set targets and alert on regression.
- **Autonomy volume plan.** Wilson promotion stays refused until the Owner chooses a multi-stage
  volume; define the volume and the incident rule so it can be evaluated.
- **Agent coordination.** The claim protocol and Agent queue
  (`docs/300-development/305-project-management/`) are the bus; add a machine-checked "slice
  claim" comment format so Cursor and Claude sessions never edit the same slice.
- **Cost budgets per run.** `budget` in workflow steps feeds `aisdlc cost`; a run exceeding its
  budget becomes `blocked`, not silently continuing.
