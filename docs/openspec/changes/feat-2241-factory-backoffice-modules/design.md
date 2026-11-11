> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Phase B (#2240/#2267) shipped `backoffice.py` / `backoffice_html.py` with right-nav
shell and CRUD for workflows/agents/skills over Phase A catalogs. Phase C (#2241)
completes the Owner module set. A.2 `#2253` (`aisdlc serve`) and A.3 `#2254`
(If-Match writes) remain open — this tip must **not** steal those contracts.
Runs monitor uses evidence + `summarize`/`report`/cost; A.4 Run entities stay
out of scope, so cancel/retry stay honestly disabled.

## Goals / Non-Goals

**Goals:**
- Extend MODULES nav: Tools/MCP, Providers, Knowledge, Rules/Gates, Integrations, Runs
- Catalog modules: list/detail/create/edit + history via `catalogs` ports
- Runs: evidence-backed monitor + report/cost summary; honest empty cancel/retry
- Fix Phase B catalog create/edit adapter to use service-layer dict/`KitError` API
  (boy scout; latent quiet=/exit-code mismatch after A.1)
- Secrets redacted; kit-neutral brand; one right-nav composition
- Kit unittests only

**Non-Goals:**
- Implementing `aisdlc serve` OpenAPI host (#2253) or If-Match (#2254)
- A.4 Project/Run/RunRequest models (#2255)
- Phase D runtime UI; Wilson vanity; fake Isolator; Context Lake
- Second OpenAPI tree or parallel metrics store
- Notaire product Playwright

## Decisions

1. **Extend Phase B path** — same `backoffice` / `backoffice_html` modules; map
   UI modules → catalog kinds: `mcp`, `providers`, `memory`, `rules`,
   `integrations`. Display labels in nav (e.g. Knowledge for `memory`).
2. **Runs is not a catalog** — `snapshot` branches to `evidence.summarize` (+
   autonomy/cost) and lists recent ledger stage records via a small public
   `evidence.ledger_records` helper (thin wrapper over `_read`); no new store.
3. **Cancel/retry** — capability flags `cancel_supported=false`,
   `retry_supported=false` with reason citing missing Runner/Forge + A.4; UI
   shows disabled controls / empty actions region, never fake success.
4. **Mutations** — catalog create/edit through `catalogs.add` / `catalogs.update`
   (dict + `KitError`); runs module rejects POST mutations.
5. **OpenAPI** — descriptive note only that Phase C adapts catalogs + evidence;
   no path/schema additions competing with A.2/A.3.
6. **Host** — keep optional `backoffice --serve` loopback; do not implement
   `#2253` bearer/`/factory/*` host here.

## Riesgos / Trade-offs

- [A.4 not shipped] → Runs UI is evidence-derived, not Project/Run SoR; label
  honesty in empty/actions copy; A.4 can replace later without second metrics.
- [A.2/A.3 open] → Ship HTML adapters over CLI/ports; avoid rewriting write
  contract; merge main often.
- [Nav sprawl] → Flat right nav with fixed module order from FACTORY-UI-SPEC;
  no cards/Ocean.
- [Phase B create adapter broken vs A.1 dict API] → Fix in same tip (boy scout);
  covered by serve create tests.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| nav lists Phase C modules + kit brand | unit | `tests/test_backoffice_phase_c.py` |
| list/detail/create/edit for mcp/providers/memory/rules/integrations | unit | `tests/test_backoffice_phase_c.py` + serve |
| version history on Phase C kinds | unit | `tests/test_backoffice_phase_c.py` |
| secrets redacted on providers | unit | `tests/test_backoffice_phase_c.py` |
| runs monitor from evidence + cost; no parallel store | unit | `tests/test_backoffice_phase_c.py` |
| cancel/retry empty/disabled with reason | unit | `tests/test_backoffice_phase_c.py` |
| runs POST rejected | unit | `tests/test_backoffice_serve.py` / phase_c |
| Phase B shell still green | unit | existing `test_backoffice_*.py` |

## Regression Strategy

- Existing tests affected: Phase B HTML may assert only three nav labels —
  update expectations for expanded MODULES; serve create must keep passing
  after adapter fix
- Full suite: `bash framework/ai-sdlc/verify.sh` then
  `bash workspace/sdlc/preflight.sh --full`
- Bruno / product API: n/a

## Playwright Strategy

- n/a — kit HTML/contract tests only; no Notaire product UI in this tip

## Deployment Strategy

- Flyway: no
- Kit-only; smoke: `aisdlc backoffice --html /tmp/bo.html --module runs`
  and create mcp via serve

## Rollback Strategy

- Revert safe: yes — additive kit modules/CLI; no schema migration
- Database rollback: none needed
- Data written under the new behavior after revert: catalog revisions remain
  valid Phase A artifacts; evidence ledger unchanged by runs UI
- Blast radius if rollback is delayed: local HTML/serve only

## Migration Plan

1. Gate 1 OpenSpec + branch (done in planning).
2. Red tests for Phase C modules, runs honesty, redaction, serve mutations.
3. Implement MODULES extension + runs evidence adapter + catalog KitError fix.
4. Green kit verify + preflight --full.
5. PR `Closes #2241` only.

## Open Questions

- None blocking: A.2/A.3 deferred by Owner instruction; runs honesty without A.4
  is explicit acceptance.
