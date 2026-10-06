# local-ai

**Purpose:** Local AI SDLC engine: foreman, worker, harness and prompts.

**Verify:** `bash local-ai/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- `local-ai/sdlc/foreman.sh` and `bin/` entry points

## Seams (what this module reads from outside)

- The Constitution, OpenSpec CLI, `.claude/skills` and `.claude/agents`

## Must not

- Be imported by product code

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
