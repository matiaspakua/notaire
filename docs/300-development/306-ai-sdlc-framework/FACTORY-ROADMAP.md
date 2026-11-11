# AI SDLC factory roadmap

In-repo mirror of the durable multi-session factory master plan (#2140).
Store authority for the Project: `ai-sdlc-factory-master-plan.md`. Epic:
[#2134](https://github.com/matiaspakua/notaire/issues/2134) · Use Case **CU76**.

| Field | Value |
|-------|-------|
| Status | Living plan — **P0–P3 spine done** (epic [#2134](https://github.com/matiaspakua/notaire/issues/2134)); next work is deferred / follow-on only |
| Epic | [#2134](https://github.com/matiaspakua/notaire/issues/2134) (spine complete; close via ASLI-31 / #2161) |
| Docs mirror | [#2140](https://github.com/matiaspakua/notaire/issues/2140) |
| Kit | [`framework/ai-sdlc/`](../../../framework/ai-sdlc/) · profile [`.aisdlc/aisdlc.toml`](../../../.aisdlc/aisdlc.toml) |
| Landed slice notes | [`FACTORY-FOUNDATION.md`](FACTORY-FOUNDATION.md) |
| Framework report | [`ai_sdlc_framework.md`](ai_sdlc_framework.md) |
| Standards ADRs | [ADR-001](../../../framework/ai-sdlc/docs/adr/ADR-001-canonical-root.md) · [ADR-002](../../../framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md) · [ADR-003](../../../framework/ai-sdlc/docs/adr/ADR-003-standards.md) · [ADR-004](../../../framework/ai-sdlc/docs/adr/ADR-004-isolator-runtime.md) · [ADR-005](../../../framework/ai-sdlc/docs/adr/ADR-005-approval-port.md) |

Do not fork a second master plan. Update this file when the store master plan
moves; thin store pointers stay short.

---

## 1. Vision & non-goals

### Vision (one sentence)

Ship a **thin, profile-driven AI SDLC control-plane kit** that governs agent work
through ports, gates, evidence, and catalogs — **independent of Notaire product
code** — and **prove every slice by running it against Notaire** (issues → gates
→ PR green).

### What the factory is

| Layer | Owns | Does not own |
|-------|------|--------------|
| **Kit core** (`framework/ai-sdlc`) | Gates, ratchets, evidence, config, plugins, harness inventory, catalogs CLI | Product domain, forge hosting, CI runners |
| **Adopter profile** (`.aisdlc/`) | Stages, gate commands, agents, skills paths, allowlists, prices | Kit implementation |
| **Foreman / fleet** | Dispatch, merge rule under policy, stage routing | Silent policy mutation |
| **Runtime (later)** | Isolator, grant, **sole Git committer** | Agent identity as person/admin |

### Seven axioms (from V1 — non-negotiable)

1. A fact needs a verifiable source (evidence or interpretation — never confuse them).
2. **Agent never commits or approves.** Runtime/coordinator owns `git commit`; agents write the worktree only.
3. A gate is a command + exit code + expectation; skip ≠ pass (`pass` / `fail` / `no-evaluable`).
4. Authority comes from token + channel, never from request body fields.
5. A Grant names worktree, write set, required checks, and budget.
6. Quality ratchets are unidirectional (debt down, floors up).
7. Autonomy is earned (Wilson lower bound) and lost on the first incident.

### Non-goals (explicit)

- **Not a Port.io clone** — no Context Lake, Ocean catalog, interface builder, org SSO portal, or Port AI over unverified data.
- **Not Warp Factories as a product** — steal “factories as code + foreman + any harness,” not hosted compute lock-in.
- **Not a mega-monolith** — no AIOps remediator, infra provisioner, dual-LLM “auditor,” or 43-feature big bang.
- **Notaire is not the product** — Notaire is the **sandbox validator** and first adopter; kit code names no Notaire product paths.
- **No sandbox claim from a manifest flag** — permissions listed ≠ OS isolation (ADR-002).
- **No OpenAPI 3.2** — public contract target is **OpenAPI 3.1.0** (ADR-003).
- **No GenAI OTel conformance claim** until a pinned SDK + tests say so (#2125).

---

## 2. Architecture

```text
                    ┌─────────────────────────────┐
                    │  CLI (aisdlc)  ·  thin UI   │
                    │  OpenAPI 3.1 description    │
                    └─────────────┬───────────────┘
                                  │
                    ┌─────────────▼───────────────┐
                    │     Plugin / port core      │
                    │  config · gates · evidence  │
                    │  catalogs · flags · otel    │
                    └─┬─────┬─────┬─────┬─────┬───┘
          MethodPort  │     │     │     │     │  FeatureFlagPort (OpenFeature)
          HarnessPort │     │     │     │     │  TelemetryPort (OTel, default no-op)
          ForgePort   │     │     │     │     EvidenceStorePort
          RunnerPort  │     │     ApprovalPort (mint/verify #2205)
                      ▼
              Adopter profile (json / yaml / toml / md)
              + forge (GitHub) + CI runner (Actions/local)
```

### Plugin core

- Canonical root: `framework/ai-sdlc` (ADR-001).
- Ports per ADR-002: Method, Harness, Tracker, Gate, Evidence, Config; extend with Telemetry, FeatureFlag, Forge, Runner, Approval as separate one-feature PRs.
- Plugin manifests are **data** — list/validate/plan never execute untrusted code.
- Method plugins today: OpenSpec (working argv), Spec Kit (adapter present, opt-in allow), AWS AI-DLC (boundary designed, not a second orchestrator) — #2121–#2124 done.

### OpenAPI surface

- Separate factory control description (**3.1.0**), never the product OpenAPI tree.
- CLI JSON payloads and any future HTTP share one schema; breaking changes via `oasdiff` + explicit accept files (#2136).

### CLI + UI

- CLI is the system of record (`aisdlc` stdlib Python).
- UI is Layer-4 / deferred: a **minimal read-only** metrics consumer (#2142), not an interface builder.

### Config-as-code (config-driven stages / agents / skills / MCP / models)

| Kind | Format | Lives |
|------|--------|-------|
| Profile | TOML | `.aisdlc/aisdlc.toml` |
| Inventory / migration | TOML | `.aisdlc/migration-inventory.toml` |
| Agents / foreman prompts | Markdown | `.aisdlc/agents/`, fleet YAML |
| Skills | `SKILL.md` trees | profile `[skills] dir` |
| MCP / memory catalogs | YAML/JSON + md | under profile (CRUD #2139) |
| Flags | JSON (OpenFeature provider) | profile-managed (#2137) |
| Evidence | JSONL hash chain | `.aisdlc/evidence/` |

### Install / update / uninstall

One-command lifecycle (#2135): install kit + scaffold; update idempotent; uninstall removes kit-managed paths only (`--purge` explicit for adopter evidence/policy). Must not touch `frontend/`, `backend-api/`, or product OpenAPI.

---

## 3. Stage customization & Foreman control

### Stages (adopter-defined)

Profile `[[gates]]` already map stages (`intake` … `release`). Per stage, the factory
declares knobs via optional `[[stages]]` (#2182 shipped):

| Knob | Meaning |
|------|---------|
| **agents** | Role cards / worker ids from agent catalog |
| **rules / skills** | Skill ids + always-loaded budget (catalog or skills-dir overlay) |
| **MCP** | Allowlisted servers/tools (declare only; kit does not run them) |
| **model** | Preferred model id / tier for that stage |
| **effort** | `tokens` / `time_seconds` / `questions` / `review_lines` |

Unknown catalog refs fail `config validate` / `doctor`. `stage list` / `config show
--redacted` surface knobs without executing anything. **Foreman routing surface**
(#2196): `aisdlc foreman plan|route [--stage] [--json]` resolves the same knobs into
a dispatch plan (`executes=false`, `sandbox=false`); Cloud Foreman / kit foreman
templates must consult it before dispatch. Foreman (cloud or local) **routes** work
across stages, may skip/revise, and merges **only** under the written merge rule
(Notaire: preflight `--full` + heavy-CI). Spec approval is Foreman instruction, not a
forge permission. Factory never merges Constitution/harness without Owner.

**Local stage alias `signal` (#2159 / #2211):** Notaire keeps an intentional
adopter-local `signal` stage for cheap local ratchet/skills ledger recording
(preflight → `aisdlc gate signal`). It is **not** renamed to a canonical id.
`aisdlc doctor` accepts `signal` as a documented local alias (no "not canonical"
WARN); other custom kebab-case stages still warn. Documented in kit
`LOCAL_STAGE_ALIASES`.

**Foreman unmapped honesty (#2211):** Only stages with `[[stages]]` knobs appear
in `mapped_count`. Gate stages without knobs are listed as `unmapped_stages` and
are **expected** until knobs are filled — the kit does **not** invent fake stage
knobs (KISS). Doctor Foreman INFO states the same.

**Notaire stage knobs beyond plan (#2215):** After #2214 honesty, Notaire
declares real `[[stages]]` for `plan`, `red`, `pre-push`, and `pre-pr`
(`mapped_count` ≥ 4) using fleet agent catalog path refs and skills-dir ids —
empty MCP (no vanity servers). There is **no** Notaire `[[gates]] stage =
"green"`: kit canonical `green` (G3 verified) is expressed as `pre-push` +
`pre-pr`, so no green knob is invented solely to raise `mapped_count`. Remaining
unmapped gate stages (`merge`, `release`, `signal`) stay listed honestly until
real knobs exist.

### Industry challenge (keep / skip)

| Theme | Decision |
|-------|----------|
| Inventory → certify → enforce → optimize | **Adapt** as catalogs + gates + Wilson — repo-local |
| Factories as code | **Include** — profile + catalogs + ADRs |
| Any model / harness | **Include** — HarnessPort (#2122 done; #2188 Claude Code / Codex / ACP stubs, sandbox=false, opt-in allow) |
| Self-improve → PR | **Adapt** — gated PRs only (#2141) |
| Context Lake / portal | **Skip** |
| Hosted factory compute | **Skip** (RunnerPort consumes CI/local) |
| Kill switch | **Include concept** later with Isolator — not a SaaS workflow |

---

## 4. Observability

| Signal | Source | Issue |
|--------|--------|-------|
| **Lead-time** issue open → PR checks green | Evidence + forge timestamps; OTel metric when port live | #2138 |
| **Stage / gate metrics** | Evidence ledger → `aisdlc report` | existing + #2138 |
| **Cost / usage** | `aisdlc cost` + prices; OpenTelemetry when enabled | #2125 |
| **Flags evaluation** | OpenFeature hooks → optional OTel | #2137 |
| **Telemetry UI** | Read-only consumer of report/OTel | #2142 (after #2125 + #2138) |

Rules: default **no-op** SDK; exporter off unless configured; **never** export prompts, diffs, or secrets; no high-cardinality path attributes; no draft GenAI-convention claim (#2125). OpenFeature flags **cannot** disable required gates or raise PR ceilings (ADR-003).

---

## 5. Catalogs CRUD & self-improvement

### Catalogs (#2139)

CRUD via CLI + files for: **skills**, **MCP** allowlist entries, **agents**, **memory** packs. Lint/digest on write; list/validate never execute. Stage profiles reference catalog ids. Profile `[context].memory` pack ids resolve against the memory catalog and participate in `aisdlc budget` (#2186); unknown packs fail closed; list/budget never execute packs.

### Self-improvement (#2141)

Loop: evidence → proposal artifact → PR → gates → Owner/Foreman merge. Aligns with Constitution P12 and #2023. **Never** silent writes to protected paths or local flag files that weaken policy.

---

## 6. Harness neutrality

| Adapter | Status | Notes |
|---------|--------|-------|
| Cursor Cloud fleet | Landed (#2122 / PR #2131) | Reads `fleet-manifest.yaml` in place |
| Local oMLX / foreman | Landed (#2122) | Reads `.aisdlc/project.yml` |
| Claude Code / Codex / ACP | Planned ports | Conformity harness later; no sandbox claim |
| Spec Kit method | Landed adapter (#2123 / #2132) | Out of allow until Owner opt-in |
| AWS AI-DLC | Boundary designed (#2124 / #2133) | Method, **not** second orchestrator |

Cloud ≠ local. Commercial “Harness” product ≠ this port. Permissions in YAML ≠ isolation.

---

## 7. Phased backlog (landed + spine done)

### Landed (do not reopen)

| Slice | Issue | PR |
|-------|-------|-----|
| Kit extract + case study | #1958 | — |
| Cross-repo protocol | #2052 | — |
| Factory foundation | #2121 | — |
| Harness port | #2122 | #2131 merged |
| Spec Kit plugin | #2123 | #2132 merged |
| AWS AI-DLC boundary | #2124 | #2133 merged |
| TelemetryPort | [#2125](https://github.com/matiaspakua/notaire/issues/2125) | [#2143](https://github.com/matiaspakua/notaire/pull/2143) |
| Install / update / uninstall | [#2135](https://github.com/matiaspakua/notaire/issues/2135) | [#2144](https://github.com/matiaspakua/notaire/pull/2144) |
| Lead-time + stage metrics (fixture) | [#2138](https://github.com/matiaspakua/notaire/issues/2138) | [#2146](https://github.com/matiaspakua/notaire/pull/2146) |
| ForgePort read-only live lead-time | [#2184](https://github.com/matiaspakua/notaire/issues/2184) | [#2185](https://github.com/matiaspakua/notaire/pull/2185) |
| ForgePort write / factory gate attest | [#2208](https://github.com/matiaspakua/notaire/issues/2208) | [#2210](https://github.com/matiaspakua/notaire/pull/2210) |
| OpenFeature port | [#2137](https://github.com/matiaspakua/notaire/issues/2137) | [#2147](https://github.com/matiaspakua/notaire/pull/2147) |
| OpenAPI 3.1 control surface | [#2136](https://github.com/matiaspakua/notaire/issues/2136) | [#2148](https://github.com/matiaspakua/notaire/pull/2148) |
| Catalogs CRUD | [#2139](https://github.com/matiaspakua/notaire/issues/2139) | [#2149](https://github.com/matiaspakua/notaire/pull/2149) |
| Per-stage profile knobs | [#2182](https://github.com/matiaspakua/notaire/issues/2182) | [#2183](https://github.com/matiaspakua/notaire/pull/2183) |
| Roadmap mirror | [#2140](https://github.com/matiaspakua/notaire/issues/2140) | [#2145](https://github.com/matiaspakua/notaire/pull/2145) |
| Self-improve gated PRs | [#2141](https://github.com/matiaspakua/notaire/issues/2141) | [#2152](https://github.com/matiaspakua/notaire/pull/2152) |
| Minimal telemetry UI | [#2142](https://github.com/matiaspakua/notaire/issues/2142) | [#2151](https://github.com/matiaspakua/notaire/pull/2151) |
| Install abs-path refuse | [#2153](https://github.com/matiaspakua/notaire/issues/2153) | [#2154](https://github.com/matiaspakua/notaire/pull/2154) |
| Catalog skills-dir discover | [#2155](https://github.com/matiaspakua/notaire/issues/2155) | [#2156](https://github.com/matiaspakua/notaire/pull/2156) |
| Improve empty propose | [#2157](https://github.com/matiaspakua/notaire/issues/2157) | [#2158](https://github.com/matiaspakua/notaire/pull/2158) |
| Preflight → evidence `signal` | [#2159](https://github.com/matiaspakua/notaire/issues/2159) | [#2160](https://github.com/matiaspakua/notaire/pull/2160) |
| Doctor `signal` alias + Foreman unmapped + lead-time Issue hint | [#2211](https://github.com/matiaspakua/notaire/issues/2211) | [#2214](https://github.com/matiaspakua/notaire/pull/2214) |
| Notaire Foreman stage knobs beyond plan (`mapped_count` ≥ 4) | [#2215](https://github.com/matiaspakua/notaire/issues/2215) | [#2219](https://github.com/matiaspakua/notaire/pull/2219) |

### Active spine — **done** (epic [#2134](https://github.com/matiaspakua/notaire/issues/2134))

P0–P3 and the docs mirror (#2125–#2142) shipped. Notaire validation follow-ons
(#2153 / #2155 / #2157) and local preflight evidence feed (#2159) closed the
loop so `report` / HTML / improve / autonomy have ledger signal. **Do not reopen
this ordered spine.**

### Deferred (after ports exist)

- Isolator **sole-committer enforcement** started (#2200 / ADR-004):
  `aisdlc isolator wrap` + hooks custody; `enforces_committer=true` only after a
  proven interception probe; works without `bwrap` (`sandbox` stays independent).
  Thin **policy** dry-run still exists (`aisdlc committer`, #2167/#2169/#2175).
  Owner `runtime_*` configured → evaluable, still advisory (`required = false`).
  **Isolator OS started** (#2198 / ADR-004): `aisdlc isolator status|probe|run` with
  `bwrap` when available; honest `sandbox=false` when missing; YAML ≠ isolation.
  **Grant profile + write-set + expire** shipped (#2192/#2194/#2198): expire fail-closed;
  expire alone ≠ Isolator complete.
  **Foreman stage-knob routing** shipped (#2196): `aisdlc foreman plan|route` —
  Foreman ops habit **partially kit-backed** (dispatch plan only; no agent/MCP
  execution). **Notaire knobs beyond plan** (#2215): `plan`/`red`/`pre-push`/
  `pre-pr` mapped; `merge`/`release`/`signal` still unmapped honestly; no
  invented `green` gate.
  Remaining Isolator honesty limits: hooks can be bypassed with
  `git -c core.hooksPath=…` unless combined with stronger containment; not unbreakable.
- ForgePort **write** / `factory/gate` attest shipped (#2208): default dry-run;
  live scoped `comment`/`label`/`check-run` only after ApprovalPort verify +
  `--write`. Read-only lead-time remains (#2184). Unscoped merge/close still refused.
  Remaining: broader forge mutation surfaces if Owner scopes them; never fake attest.
- Wilson autonomy **promotion** (L0–L4). Multi-stage **honesty** already ships
  (#2171): L1 stays no-evaluable until ≥2 allowlisted stages have `min_n` —
  not Isolator, not auto-promotion. **Refused in #2190** until Owner-chosen
  multi-stage operational volume (Notaire `improve autonomy` still `no-evaluable`).
- ApprovalPort **runtime** mint/verify shipped (#2205 / ADR-005) via
  `ssh-keygen -Y`; `approvals_live=true` only after proven round-trip; local/dev
  keys ≠ HSM. Remaining: production HSM / DSSE alternate encoder if Owner chooses.
  ForgePort write (#2208) is gated by Approval verify.

### Parallel stay path

- [#2117](https://github.com/matiaspakua/notaire/issues/2117) — generated testing inventory hotspot (do not move into kit core).

---

## 8. Delivery rules (every slice)

1. **GitHub Issue + CU76** before code; OpenSpec Gate 1 from `docs/`.
2. **TDD** — failing kit tests first, then implement.
3. **KISS / DRY / SRP** — one port or one catalog surface per PR; bounded **boy scout** only on touched kit files.
4. **One-feature PRs** — no mega-PR from the pre-V1 43-feature list.
5. **Validate on Notaire open issues** — run profile commands against real gates/inventory; cite issue numbers in evidence.
6. **Preflight** — `bash workspace/sdlc/preflight.sh --full` before PR (CONSTITUTION); `Closes #…`.
7. **Self-improve** friction into follow-on Issues / gates / skills (P12) — as gated PRs (#2141).

Serialize `CHANGELOG.md` `[Unreleased]` with the TelemetryPort PR (#2125): docs-only
mirrors omit Unreleased when that slice owns the fence.

---

## 9. Success metrics & anti-patterns

### Success (binary + operational)

| Metric | Target |
|--------|--------|
| Kit independence | Temp project with no Notaire paths passes `config validate` |
| Telemetry safety | Prompts/diffs/secrets absent from signals (#2125) |
| Lead-time | Measured issue→PR-green on Notaire sample (#2138) |
| Flag safety | Required gate cannot be disabled by flag (#2137) |
| Install lifecycle | Install → doctor → uninstall leaves product trees untouched (#2135) |
| Catalog integrity | CRUD + lint; no execute-on-list (#2139) |
| Self-improve | Only via PR; protected paths stay Owner (#2141) |
| Process | Clean-pass rate + cost/change from evidence; Wilson when autonomy ships |

### Anti-patterns (refuse)

1. Mega-monolith / Port clone / Context Lake inside the kit.
2. Second Constitution copy under `framework/ai-sdlc`.
3. Claiming sandbox from inventory/manifest permissions.
4. Importing product OpenAPI / `frontend` / `backend-api` into the kit.
5. Dual-LLM review as custody (advisory only; deterministic admission wins).
6. OpenFeature (or any flag) weakening security/merge invariants.
7. Agent commits, self-approvals, or silent policy mutation.
8. OAS 3.2 as the production contract.
9. One PR that “finishes the factory.”
10. Implementing TelemetryPort code inside this docs mirror (#2125 stays the code Issue).

---

## 10. How later sessions continue

1. Read **this** file + store master plan / thin plan next-5 list.
2. **§7 Active spine is done.** Pick only from Deferred (Wilson L0–L4 /
   broader forge mutations / HSM) or a new CU76 Issue that cites friction from
   evidence (`aisdlc improve` / preflight `signal`) — one thin PR, no portal.
3. Branch from updated `main`; TDD; validate on Notaire; open PR `Closes #…`.
4. Update thin plan next-5 and inventory notes; do not fork a second master plan.
5. Keep this mirror aligned when the store master plan changes (#2140).
6. Control-surface UI, host and reuse work follows the design record
   [#2251](https://github.com/matiaspakua/notaire/issues/2251):
   [`FACTORY-PRODUCT-ANALYSIS.md`](FACTORY-PRODUCT-ANALYSIS.md) (gaps, reuse plan) and
   [`FACTORY-UI-SPEC.md`](FACTORY-UI-SPEC.md) (architecture, contract, slices). Claim a slice
   there before branching.

---

## References

- Kit README: [`framework/ai-sdlc/README.md`](../../../framework/ai-sdlc/README.md)
- Cloud fleet: [`../304-ai-sdlc-cloud/`](../304-ai-sdlc-cloud/)
- Cross-repo assessment: [`CROSS-ASSESSMENT-2026-11.md`](CROSS-ASSESSMENT-2026-11.md)
- Factory as a product — gap analysis: [`FACTORY-PRODUCT-ANALYSIS.md`](FACTORY-PRODUCT-ANALYSIS.md)
- Factory UI and host — architecture spec: [`FACTORY-UI-SPEC.md`](FACTORY-UI-SPEC.md)
- Constitution P12 / gates: [`CONSTITUTION.md`](../../../CONSTITUTION.md)
- Port AgentOps / Warp Factories: contrast only — adapt inventory→gates ideas; skip portal/hosted compute
