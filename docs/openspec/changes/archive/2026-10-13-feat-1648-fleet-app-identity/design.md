> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — CU78 least privilege.

## Context

GitHub Apps authenticate with an RS256 JWT, then exchange it for a one-hour installation token. `gh` and the SDLC scripts honour `GH_TOKEN`.

## Goals / Non-Goals

**Goals:** board/label writes without humans; least privilege; no new dependency.
**Non-Goals:** creating the App (Owner); webhook-driven automation.

## Decisions

1. **Bash + openssl.** No PyJWT dependency; openssl is on every runner and Mac.
2. **The token goes out as an `export` line** for `eval`, so the caller's shell gets it and no file is written.
3. **Loud fallback**, never a silent identity switch.

## Riesgos / Trade-offs

- [The private key leaks] → secrets only in the runtime store. Tokens last 1 h. The App has no admin and no secrets permission.
- [The App id is wrong, or the App isn't installed] → `curl -f` fails and the script exits non-zero.

## Testing Strategy

`workspace/tests/test_fleet_token.py`: the JWT verifies against a generated key; the fallback is loud. Both red with the script absent.

## Regression Strategy

`workspace/tests`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Inert until the Owner configures the App.

## Rollback Strategy

Revert. Agents fall back to ambient auth.

## Migration Plan

None.

## Open Questions

None.
