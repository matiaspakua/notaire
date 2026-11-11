> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

A.1 (#2252) extracted a service layer that returns dicts / raises `KitError`.
OpenAPI already declares factory GET paths; `contract.py` still says the kit hosts
no server. Phase A.2 (#2253) adds the single stdlib host UIs will call. Do **not**
touch #2254 tip (`feat/2254_…`).

## Goals / Non-Goals

**Goals:**
- `aisdlc serve` implements declared factory GET paths via service layer
- Loopback bind + Bearer token (0600) + read-only enforcement (405 mutate)
- Honest `GET /factory/capabilities`
- Additive OpenAPI; `contract check` + oasdiff additive OK
- Static mount without shipping UI
- Kit unittests; secret redaction golden

**Non-Goals:**
- Mutating HTTP methods / If-Match (#2254)
- Project/Run (#2255); Phase D SPA
- Context Lake / Port clone / Wilson vanity / fake Isolator
- Second OpenAPI tree; Notaire product Playwright
- Changing committer `required=true`

## Decisions

1. **Modules** — `serve_token.py` (create/load/verify scopes; never log plaintext)
   and `serve.py` (routing, problem+json, bind guard, static mount). Keep each
   small (issue ≤400 counted lines; split if needed).
2. **stdlib host** — `ThreadingHTTPServer` + `BaseHTTPRequestHandler` (same family
   as backoffice serve). No Flask/FastAPI.
3. **Token file** — `.aisdlc/serve.token` under the adopter profile root; create
   on first serve with `secrets.token_urlsafe` + default scope `owner` (holder may
   present as read); mode `0o600`. Format: `token=<value>\nscope=<read|write|owner>\n`.
4. **Auth** — `Authorization: Bearer <token>`; missing/invalid → 401 problem;
   insufficient scope → 403 (reserved); mutating methods → **405** in this slice
   even with write/owner.
5. **Bind** — default host `127.0.0.1`. Non-loopback (`0.0.0.0`, LAN IP, `::`)
   refused unless `--allow-remote` **and** token file present. No CORS.
6. **Capabilities** — assemble from existing ports without claiming truth they
   do not prove:
   - `approvals_live` ← `approval.approval_status`
   - `runner` ← false unless a harness adapter is proven executable (today: always
     honest `false`; inventory stubs are not runners)
   - `forge_write` ← `false` (dry-run default; live write only after Approval +
     `--write` path — never claim true from YAML)
   - `isolator.sandbox` / `isolator.enforces_committer` ← `isolator.isolator_status`
7. **GET routing** — map OpenAPI paths to service functions (`factory.config_view`,
   `list_plugins`, `harness.list_adapters`, `evidence.report_summary` /
   report payload, `catalogs.*`, `workflows.*`). Ids validated by existing
   `_assert_id` / regex equivalent.
8. **Static mount** — `/ui/` (or `/static/`) served from kit `ui/` directory;
   create empty mount + README placeholder; no SPA.
9. **OpenAPI** — single tree; add `/factory/capabilities` + `FactoryCapabilities`
   (+ `Problem` schema / bearer security description as needed). Update
   `contract.REQUIRED_PATHS` if structural check needs it.
10. **Committer identity** — Matias Miguez / matiaspakua@gmail.com; leave
    profile `required=true` alone.

## Riesgos / Trade-offs

- [A.3 tip in flight] → Implement GET only; never steal write/If-Match semantics.
- [Harness “runner” honesty] → Prefer false over inventory-as-live; document in
  capabilities `detail`.
- [Token leakage] → Never print/log token; redaction golden on responses/logs.
- [Line budget] → Split token vs routing; no SPA chrome in this PR.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| serve GET paths on ephemeral port | unit/contract | `tests/test_serve.py` |
| missing/invalid token → 401 | unit | `tests/test_serve.py` |
| mutate → 405 | unit | `tests/test_serve.py` |
| non-loopback refused without allow-remote+token | unit | `tests/test_serve.py` |
| capabilities honest false fields | unit | `tests/test_serve.py` |
| traversal ids → 400 problem | unit | `tests/test_serve.py` |
| secret redaction golden | unit | `tests/test_serve.py` |
| OpenAPI additive + contract check | unit | `tests/test_contract.py` |
| static mount exists, empty UI | unit | `tests/test_serve.py` |

## Regression Strategy

- Existing backoffice `--serve` tests stay green (separate adapter).
- Service-layer / CLI goldens unchanged.
- Full suite: `bash framework/ai-sdlc/verify.sh` then
  `bash workspace/sdlc/preflight.sh --full`.

## Playwright Strategy

n/a — no product UI surface; kit static mount ships no SPA. Kit UI E2E stays
out of `testing/e2e` (F11).

## Deployment Strategy

Ships with the next release via kit tree; no Notaire deploy change.

## Rollback Strategy

Revert the PR.

## Migration Plan

None for adopters. First `aisdlc serve` creates `.aisdlc/serve.token` locally.

## Open Questions

None — FACTORY-UI-SPEC §3 is SoR.
