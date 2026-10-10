# agent-context-budget

Always-loaded agent context stays within an explicit token budget.

## ADDED Requirements

### Requirement: Measurable always-loaded budget

The repository MUST provide a script that measures the always-loaded agent file set
and can fail when the estimate exceeds 8,000 tokens (bytes/4 heuristic unless a
tighter tokenizer is documented).

#### Scenario: Budget check passes after trim

- **WHEN** `python3 workspace/ci/agent-context-budget.py --max-tokens 8000` runs after #1259
- **THEN** it exits 0

### Requirement: Constitution digest for agents

A short agent card MUST exist so agents need not always-load the full Constitution,
while still pointing to `CONSTITUTION.md` as authority.

#### Scenario: Card present and small

- **WHEN** `CONSTITUTION-AGENT-CARD.md` is inspected
- **THEN** it exists, is ≤3072 bytes, and links to the full Constitution

### Requirement: On-demand rule loading

`AGENTS.md` MUST NOT bulk-import large rule/skill bodies for every session; it MUST
provide an on-demand path→skill table for area-specific guidance.

#### Scenario: No bulk alwaysApply frontend skill

- **WHEN** `.claude/skills/frontend-design/SKILL.md` frontmatter is inspected
- **THEN** it is not marked `alwaysApply: true`
