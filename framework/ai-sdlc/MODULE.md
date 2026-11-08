# ai-sdlc-framework

**Purpose:** Stack-agnostic AI SDLC kit: the `aisdlc` CLI (stage gates, ratchets, evidence, cost,
skills lint, context budget, commit/TDD checks, change records) and the policy, card and role-agent
templates that `aisdlc init` scaffolds. Extracted from Notaire's process (#1958).

**Verify:** `bash framework/ai-sdlc/verify.sh` (module checks only; the full gate is `bash workspace/sdlc/preflight.sh`).

## Contract (what other modules may rely on)

- `framework/ai-sdlc/bin/aisdlc` and its exit codes: 0 passed, 1 a gate or check failed, 2 usage or configuration error
- The profile schema documented in `README.md` (Notaire's profile: `.aisdlc/aisdlc.toml`)

## Seams (what this module reads from outside)

- Only the adopter's profile, git history and the files the profile names

## Must not

- Name an adopter's path, stack or tool in `aisdlc/`, `bin/` or `templates/` (guarded by `tests/test_independence.py`)
- Depend on anything outside the Python 3.11+ standard library
- Be imported by product code

Manifest entry: [`workspace/modules.yaml`](../../workspace/modules.yaml). Rationale: ADR-026.
