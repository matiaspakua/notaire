> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

CU76 factory deferred work still lists Isolator OS + Grant expire after #2197
(Foreman routing). Owner `[committer] runtime_*` is set with `required=false`
(do not flip). ADR-004 stub keeps Isolator `no-evaluable` / `sandbox=false`.
This slice starts Isolator OS honestly and ships Grant expire fail-closed.

## Goals / Non-Goals

**Goals:**
- Grant `expires_at` / `ttl_seconds` parse + fail-closed expire evaluation
- IsolatorPort probe/run with `bwrap` when present; honest degrade when missing
- CLI `aisdlc isolator status|probe|run` with JSON honesty fields
- Docs/ADR honesty: Isolator OS **started**; expire shipped; refuse fake sandbox

**Non-Goals:**
- Flipping `[committer] required=true`
- Portal / Context Lake / Wilson vanity / ForgePort write / ApprovalPort mint
- Claiming Isolator complete from expire schema or grant check alone
- Setting `sandbox=true` without a successful OS containment probe

## Decisions

1. **Expire fields** — optional on `[[grants]]`. `expires_at` is absolute ISO-8601 UTC.
   `ttl_seconds` is a relative deadline; for evaluation without a stored mint time,
   treat profile-load / evaluation `now` as the clock and require an explicit
   `expires_at` OR evaluate TTL against a caller-supplied `issued_at` / default
   "now at profile load" only when tests inject a clock. Prefer: if both set,
   earliest deadline wins. For kit simplicity: `expires_at` absolute; `ttl_seconds`
   means "expired if evaluation now is >= profile_loaded_at + ttl" where
   `profile_loaded_at` is injectable via clock for tests, else `datetime.now(UTC)`.
   Actually cleaner honesty: `ttl_seconds` alone requires pairing with `expires_at`
   OR we store nothing and interpret `ttl_seconds` as "must be accompanied by
   expires_at derived by operator". Best minimal approach used by many kits:
   - `expires_at`: absolute deadline
   - `ttl_seconds`: if set without expires_at, treat as expires_at = now + ttl at
     parse time (bake absolute into Grant at load) OR evaluate with injectable clock
     against an `issued_at` field.
   
   Minimal honest choice: support both fields; `is_expired(grant, now)` returns true
   if `expires_at` parsed < now, OR if `ttl_seconds` set and a grant-level
   `issued_at` (optional, ISO-8601) + ttl is past. If only `ttl_seconds` without
   `issued_at`/`expires_at`, fail closed at validate ("ttl_seconds requires
   expires_at or issued_at"). Simpler for adopters: allow `expires_at` OR
   `ttl_seconds` where `ttl_seconds` alone means "relative to evaluation now from
   an injected clock baseline" — for production use `expires_at`.

   **Final decision (KISS):** 
   - `expires_at`: absolute ISO-8601 UTC; expired when `now >= expires_at`
   - `ttl_seconds`: positive int; expired when `now >= issued_at + ttl` where
     `issued_at` defaults to profile parse time captured on the Grant object
     (`_loaded_at`) for evaluation; tests inject `now`
   - Both set → expired if either condition holds (fail closed)

2. **Backend** — detect `bwrap` on PATH. Probe runs a trivial command under
   `bwrap --ro-bind / / --dev /dev --proc /proc --tmpfs /tmp --die-with-parent -- true`
   (or similar). Success → `backend=bwrap`, `sandbox=true` for probe/status when
   probe ok. Missing → `backend=none`, `sandbox=false`, `status=no-evaluable`.

3. **Run** — when sandbox available and active (non-expired) grants have write_set,
   bind repo root RO and remount write_set paths RW under bwrap; execute argv.
   Without backend or with expired-only grants → refuse (exit non-zero).

4. **enforces_committer** — remains `false` in this slice (no sole-committer
   interception of `git commit`); honesty field stays false.

5. **Doctor** — Isolator INFO reflects real status; never claims sandbox without
   probe success. Grant doctor note mentions expire fail-closed + still not a
   sandbox claim from expire alone.

6. **Notaire sample** — comment showing optional `expires_at` on plan-sample; do
   not set a live past expire that breaks CI; do not claim live Isolator OS.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Readers treat expire as Isolator done | ADR + doctor + JSON notes |
| CI without bwrap looks like failure | Honest `no-evaluable` is success path for status/probe reporting |
| Over-broad bwrap binds | Constrain to Grant write_set RW; rest RO |

## Testing Strategy

| Scenario | Test |
|----------|------|
| grant expire fields parse and show | GrantExpireTest |
| expired grant fails closed | GrantExpireTest |
| ttl_seconds past fails closed | GrantExpireTest |
| isolator status without bwrap is honest | IsolatorOsTest |
| isolator probe reports backend and sandbox | IsolatorOsTest |
| isolator run refuses when sandbox unavailable | IsolatorOsTest |
| isolator run refuses expired grant | IsolatorOsTest |
| doctor never claims sandbox without probe success | IsolatorOsTest |

When `bwrap` is present in the environment, tests MAY additionally assert
`sandbox=true` after a successful probe; when absent, tests MUST assert honest
degrade. Do not skip the honesty cases.

## Regression Strategy

Existing `test_isolator.py`, `test_grants.py`, `test_grant_check.py` remain green;
update isolator stub expectations only where status payload gains honest `backend`
field while keeping `sandbox=false` without bwrap.

## Playwright Strategy

n/a — no UI surface (kit CLI only)

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

Optional Grant expire fields; omitted = previous behavior. Isolator CLI is additive.

## Open Questions

None.
