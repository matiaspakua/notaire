> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Catalogs (#2139) store digested JSON under `[catalogs] dir` with overwrite
updates. OpenAPI (#2136) lists catalogs read-only. Stages (#2182) and foreman
(#2196) are not workflows. ApprovalPort (#2205 / ADR-005) provides mint/verify
with honest `approvals_live`. Phase A must ship the domain contract only
(kit-first; no SPA) while #2088 owns product Playwright.

## Goals / Non-Goals

**Goals:**
- Immutable revisions + current pointer for factory entities
- Extended kinds: `workflows`, `providers`, `integrations`, `rules`
- Workflow governance FSM with fail-closed guards
- OpenAPI 3.1 + CLI JSON SoR; `contract check` green
- ApprovalPort-honest `approve` (or documented Owner override)

**Non-Goals:**
- SPA / Playwright product UI / Notaire frontend routes
- Context Lake, Port.io clone, Wilson vanity, fake Isolator
- Second config database; HTTP server mandatory
- Gate runner rewrite (rules catalog is CRUD projection only)

## Decisions

1. **Persistence layout** — `{catalogs}/{kind}/{id}/current.json` +
   `{catalogs}/{kind}/{id}/versions/{n}.json`. Digest covers revision payload
   excluding mutable pointer-only fields. Legacy flat `{id}.json` remains
   readable; first versioned write migrates into the directory layout.
2. **Kinds** — extend `catalogs.KINDS` rather than a second store so stage/list
   resolution stays one surface.
3. **Workflow ≠ stage** — workflow body may *reference* stage/catalog ids;
   stages remain foreman knobs.
4. **Transitions** — `define`, `request_validation`, `approve`, plus documented
   demote/reopen pairs only. Illegal → non-zero CLI + JSON error.
5. **Approve honesty** — when `approvals_live=true`, require ApprovalPort verify
   of a subject covering the workflow revision; when false, require explicit
   `--owner-override` + reason (or fail closed). Never a cosmetic badge.
6. **Secrets** — providers/integrations store `env:` refs only; reuse
   `factory.redact` on show.
7. **CLI** — `catalog` gains show/history/new-version semantics; `workflow`
   subcommand for create/list/show/new-version/transition/approved filter.

## Riesgos / Trade-offs

- [Flat→directory migration] → Read both layouts; migrate on write; tests cover
  legacy read.
- [Over-modeling providers/integrations] → Thin schemas in A; UI depth in B/C.
- [Approve without keys blocks demos] → Documented Owner override path only;
  never auto-approve.
- [Playwright contention with #2088] → Kit-only tip; no `frontend/` / E2E product.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| new version is immutable | unit | `tests/test_entity_versioning.py` |
| illegal transition fails | unit | `tests/test_workflow_governance.py` |
| legal draft→defined→validated→approved | unit | `tests/test_workflow_governance.py` |
| approve without live or override fails | unit | `tests/test_workflow_governance.py` |
| approved-only filter | unit | `tests/test_workflow_governance.py` |
| extended kinds list/add | unit | `tests/test_entity_versioning.py` |
| secret refs redacted on show | unit | `tests/test_entity_versioning.py` |
| OpenAPI schemas/paths present | unit | `tests/test_contract.py` (extend) |
| kit independence | unit | `tests/test_independence.py` (existing) |

- New unit tests: `framework/ai-sdlc/tests/test_entity_versioning.py`,
  `framework/ai-sdlc/tests/test_workflow_governance.py`
- New integration tests: n/a (kit CLI via Repo helper is the integration surface)
- Coverage impact: kit unittest suite; no JaCoCo/backend change

## Regression Strategy

- Existing tests affected: `test_catalogs.py` (update path expectations for
  versioned layout / update→new revision); `test_contract.py` for new required
  paths/schemas; stage catalog resolution still uses `list_entries`
- Full suite command: `bash framework/ai-sdlc/verify.sh` then
  `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: n/a — no product API change
- Legacy paths at risk: none in product packages

## Playwright Strategy

n/a — no UI surface (Phase A kit domain/OpenAPI/CLI only; product E2E deferred
to #2240+ and must not contend with #2088).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with next kit/adopter release; Notaire
  profile may omit workflows until Phase E
- Configuration or `.env` keys to add: optional Owner override env documented in
  README; never commit secrets
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc contract check` + workflow CLI
  round-trip on a scratch profile

## Rollback Strategy

- Revert safe: yes — additive kit APIs; flat catalog files still readable
- Database rollback: none needed
- Data written under the new behavior after revert: version directories remain
  inert files
- Blast radius if rollback is delayed: low (CLI opt-in)

## Migration Plan

1. Ship versioned layout + legacy flat read.
2. Adopter profiles keep empty workflows until Phase E demo.
3. Doctor/validate must not require workflows to exist.

## Open Questions

None — Owner plan + store OpenSpec drafts resolve scope for Phase A.
