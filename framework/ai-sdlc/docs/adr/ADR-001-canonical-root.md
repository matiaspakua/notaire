# ADR-001: Canonical factory root

**Status:** Accepted for the first foundation slice

## Context

Delivery process already lives in more than one tree: a zero-dependency Python kit, a shell preflight, a local-model harness, and cloud agent prompts. Copying the policy into each of those trees makes them disagree.

## Decision

`framework/ai-sdlc` is the canonical root. It already has the CLI, the profile loader, the gates, and tests that forbid adopter paths in the kit.

Discoverability paths stay where tools look for them (`workspace/sdlc`, `local-ai/sdlc`, `.claude/agents`, `.claude/skills`). They are compatibility shims or harness adapters. This slice does not move them and does not generate a second copy of the policy.

## Consequences

A later extraction copies `framework/ai-sdlc` and asks each adopter for its own profile. Product specs, product OpenAPI, and application source stay in the adopter repository.
