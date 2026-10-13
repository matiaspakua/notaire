<!-- Governed by CONSTITUTION.md. CU76, issue #1647. -->

## ADDED Requirements

### Requirement: Agent harness config is portable and within the context budget

`opencode.json` SHALL contain no absolute user or home paths and SHALL load only the always-loaded set (`AGENTS.md`, `CONSTITUTION-AGENT-CARD.md`); skills load on demand.

#### Scenario: opencode config is portable

- **WHEN** `opencode.json` is read
- **THEN** it holds no `/Users/` or `/home/` path and its instructions are exactly `AGENTS.md` and `CONSTITUTION-AGENT-CARD.md`

### Requirement: Session start surfaces work that was never pushed

The SessionStart hook SHALL list local branches without an upstream whose commits are on no remote branch, and SHALL still exit 0.

#### Scenario: Never-pushed branch is listed

- **WHEN** a local branch has commits and no upstream
- **THEN** the hook output names the branch and says its commits were never pushed

### Requirement: Pushes are verified on the remote

The agent workflow rule SHALL tell agents to confirm a push with `git ls-remote --heads origin <branch>`.

#### Scenario: Workflow rule names the verification

- **WHEN** `.claude/rules/ai-agent-workflow.md` is read
- **THEN** it contains `git ls-remote --heads origin`
