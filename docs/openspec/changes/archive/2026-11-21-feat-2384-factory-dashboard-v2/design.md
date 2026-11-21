> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

FACTORY-UI-SPEC.md (#2251) is the contract; SESSION-FEATURES.md lists what the branch built.

## Goals / Non-Goals

**Goals:** honest, multi-tenant dashboard over the kit; living coach; independent RepoLens.
**Non-Goals:** replacing the kit's CLI; a hosted service.

## Decisions

1. **Contract client.** The dashboard is an optional companion (Node) outside the stdlib-only kit core; it talks to `aisdlc serve` only, so the kit stays the system of record and the static UI option stays open.
2. **Users are kit tokens.** Several named tokens with scopes in the kit; the dashboard keeps each user's token in a server-side session, so authorization and the actor are the kit's.
3. **Workspaces are endpoints.** One entry per `aisdlc serve` (and optional RepoLens), so projects, repositories and machines combine without shared storage.
4. **Coach in the kit.** Stdlib-only analysis of local harness logs, aggregated, no prompt text.
5. **RepoLens moves to framework/repolens.** Wiring paths change; its code already has no kit import.

## Riesgos / Trade-offs

- [Moving RepoLens breaks wiring] -> regenerate with `repolens setup`, wiring tests.
- [Coach reads private logs] -> local only, counts and categories, no text, opt-out paths.
- [Dashboard token theft] -> httpOnly SameSite cookie, server-side token, loopback default.

## Testing Strategy

kit pytest (tokens, coach, serve), dashboard vitest (server via fastify inject), repolens pytest.

## Regression Strategy

`bash framework/ai-sdlc/verify.sh`, `bash framework/repolens/verify.sh`, dashboard `pnpm test` and typecheck.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
