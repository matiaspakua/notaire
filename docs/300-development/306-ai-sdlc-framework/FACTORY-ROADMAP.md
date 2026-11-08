# AI SDLC factory roadmap

In-repo mirror of the durable multi-session factory master plan (#2140).
Store authority for the Project: `ai-sdlc-factory-master-plan.md`. Epic:
[#2134](https://github.com/matiaspakua/notaire/issues/2134) · Use Case **CU76**.

| Field | Value |
|-------|-------|
| Status | Living plan — continue from here across sessions |
| Epic | [#2134](https://github.com/matiaspakua/notaire/issues/2134) |
| Docs mirror | [#2140](https://github.com/matiaspakua/notaire/issues/2140) |
| Kit | [`framework/ai-sdlc/`](../../../framework/ai-sdlc/) · profile [`.aisdlc/aisdlc.toml`](../../../.aisdlc/aisdlc.toml) |
| Landed slice notes | [`FACTORY-FOUNDATION.md`](FACTORY-FOUNDATION.md) |
| Framework report | [`ai_sdlc_framework.md`](ai_sdlc_framework.md) |
| Standards ADRs | [ADR-001](../../../framework/ai-sdlc/docs/adr/ADR-001-canonical-root.md) · [ADR-002](../../../framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md) · [ADR-003](../../../framework/ai-sdlc/docs/adr/ADR-003-standards.md) |

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
          RunnerPort  │     │     ApprovalPort (later)
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

Profile `[[gates]]` already map stages (`intake` … `release`). Per stage, the factory must allow declaring:

| Knob | Meaning |
|------|---------|
| **agents** | Role cards / worker ids from agent catalog |
| **rules / skills** | Skill ids + always-loaded budget |
| **MCP** | Allowlisted servers/tools (declare only; kit does not run them) |
| **model** | Preferred model id / tier for that stage |
| **effort** | Token/time budget, review size, question budget |

Foreman (cloud or local) **routes** work across stages, may skip/revise, and merges **only** under the written merge rule (Notaire: preflight `--full` + heavy-CI). Spec approval is Foreman instruction, not a forge permission. Factory never merges Constitution/harness without Owner.

### Industry challenge (keep / skip)

| Theme | Decision |
|-------|----------|
| Inventory → certify → enforce → optimize | **Adapt** as catalogs + gates + Wilson — repo-local |
| Factories as code | **Include** — profile + catalogs + ADRs |
| Any model / harness | **Include** — HarnessPort (#2122 done) |
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

CRUD via CLI + files for: **skills**, **MCP** allowlist entries, **agents**, **memory** packs. Lint/digest on write; list/validate never execute. Stage profiles reference catalog ids.

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

## 7. Phased backlog (landed + active spine)

### Landed (do not reopen)

| Slice | Issue | PR |
|-------|-------|-----|
| Kit extract + case study | #1958 | — |
| Cross-repo protocol | #2052 | — |
| Factory foundation | #2121 | — |
| Harness port | #2122 | #2131 merged |
| Spec Kit plugin | #2123 | #2132 merged |
| AWS AI-DLC boundary | #2124 | #2133 merged |

### Active spine (epic [#2134](https://github.com/matiaspakua/notaire/issues/2134))

| Phase | Order | Issue | Module | Validate on Notaire |
|-------|-------|-------|--------|---------------------|
| **P0** | 1 | **[#2125](https://github.com/matiaspakua/notaire/issues/2125)** | TelemetryPort (reuse — open) | kit unittest; OTEL off default; redaction |
| **P0** | 2 | [#2135](https://github.com/matiaspakua/notaire/issues/2135) | Install / update / uninstall | profile validate after lifecycle |
| **P1** | 3 | [#2138](https://github.com/matiaspakua/notaire/issues/2138) | Lead-time + stage metrics | real issue→PR fixture or sample |
| **P1** | 4 | [#2137](https://github.com/matiaspakua/notaire/issues/2137) | OpenFeature port | flag cannot kill required gate |
| **P1** | 5 | [#2136](https://github.com/matiaspakua/notaire/issues/2136) | OpenAPI 3.1 control surface | oasdiff in CI; no product OpenAPI import |
| **P2** | 6 | [#2139](https://github.com/matiaspakua/notaire/issues/2139) | Catalogs CRUD | skills/agents discoverable; no Constitution fork |
| **P2** | 7 | [#2141](https://github.com/matiaspakua/notaire/issues/2141) | Self-improve gated PRs | proposal only; protected paths Owner-merge |
| **P3** | 8 | [#2142](https://github.com/matiaspakua/notaire/issues/2142) | Minimal telemetry UI | fixtures from evidence; after #2125+#2138 |
| **Docs** | ∥ | [#2140](https://github.com/matiaspakua/notaire/issues/2140) | In-repo roadmap mirror | this file + foundation/index cross-links |

### Deferred (after ports exist)

- Isolator + agent-never-commits runtime (V1 M0/M1 / D1–D7 class) — **not** in P0–P2.
- ForgePort attestations / `factory/gate` on GitHub checks.
- Wilson autonomy promotion (L0–L4).
- Full ApprovalPort (SSH/DSSE).

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
2. Pick the lowest open issue in §7 Active spine (currently **#2125**).
3. Branch from updated `main`; TDD; validate on Notaire; open PR `Closes #…`.
4. Update thin plan next-5 and inventory notes; do not fork a second master plan.
5. Keep this mirror aligned when the store master plan changes (#2140).

---

## References

- Kit README: [`framework/ai-sdlc/README.md`](../../../framework/ai-sdlc/README.md)
- Cloud fleet: [`../304-ai-sdlc-cloud/`](../304-ai-sdlc-cloud/)
- Cross-repo assessment: [`CROSS-ASSESSMENT-2026-11.md`](CROSS-ASSESSMENT-2026-11.md)
- Constitution P12 / gates: [`CONSTITUTION.md`](../../../CONSTITUTION.md)
- Port AgentOps / Warp Factories: contrast only — adapt inventory→gates ideas; skip portal/hosted compute
