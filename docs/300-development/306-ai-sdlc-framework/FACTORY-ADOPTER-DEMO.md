# Factory adopter demo — Notaire sandbox (#2243 / CU76)

Notaire is the **sandbox adopter** for the AI SDLC kit, not the product brand inside
kit code. This page is the Notaire-specific entry for Phase E. Kit-neutral steps live
in [`framework/ai-sdlc/docs/ADOPTER-DEMO.md`](../../../framework/ai-sdlc/docs/ADOPTER-DEMO.md).

**Does not replace** Notaire product admin (`frontend/` admin). Factory backoffice /
runtime are kit adapters only.

## What is wired

- `.aisdlc/catalogs/skills/*` — catalog entries for Foreman stage skills that already
  exist under `.claude/skills/`
- `.aisdlc/catalogs/workflows/factory-feature` — one **approved** workflow with pinned
  `agents:` / `skills:` refs drawn from `[[stages]]` (MCP empty)
- Existing `.aisdlc/catalogs/agents/*` — Foreman fleet agent refs (unchanged)

## Commands

From the repository root:

```bash
# Re-seed if needed (idempotent when factory-feature is already approved)
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml demo seed --from-stages --json

python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml doctor
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml catalog validate
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml report
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml evidence verify
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml workflow list --approved --json
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml foreman plan --json
```

## UI entry

```bash
# Control plane (workflows / agents / skills / …)
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml backoffice --serve --port 8765

# Runtime (Projects → repos → approved workflow → Start → Monitor)
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml runtime --serve --port 8766

# OpenAPI host (tokenized; loopback)
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml serve --port 8770
```

Open the printed loopback URL. On runtime **Start run**, pick `factory-feature`
(approved only). Progress stays honest (`queued` until harness events).

## Refuse / serialize

- No product Playwright tip in this change (serialize vs MFA docs #2271 / #2088 rule)
- Leave `[committer] required=true` alone
- Public wheel second-adopter sample → packaging #2256 remainder
- Context Lake / Wilson vanity / fake Isolator → out of scope
