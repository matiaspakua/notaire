<!-- Governed by CONSTITUTION.md. CU76, issue #1624. Owner decision 2026-10-13: option (b), Foreman merges under a rule. -->

## ADDED Requirements

### Requirement: One Foreman merge rule across governance docs and harness

The Constitution SHALL define a Foreman merge rule. The Foreman may merge a Worker PR only when every required `protect-main` check is green, `workspace/sdlc/check-heavy-ci.sh <pr>` exits 0, the PR is mergeable, and Gate 4 is clear. The Owner alone SHALL merge Constitution amendments, `sdlc-exception` PRs, and PRs that change the Foreman's harness or merge path. Every agent-facing governance document SHALL state this rule, and none SHALL keep an Owner-only merge rule. The local Foreman harness SHALL run the heavy-CI gate before it merges.

#### Scenario: Harness runs the heavy-CI gate before merging

- **WHEN** `merge_and_close` in `local-ai/sdlc/foreman.sh` is read
- **THEN** it calls `check-heavy-ci.sh` before `gh pr merge`

#### Scenario: Governance docs name the rule

- **WHEN** `CONSTITUTION.md`, `CONSTITUTION-AGENT-CARD.md`, `.claude/rules/ai-agent-workflow.md`, `.claude/agents/cloud-foreman.md` and `CI-MERGE-GATE.md` are read
- **THEN** each names the Foreman merge rule and `check-heavy-ci.sh`, and none says agents never merge or that only the Owner merges PRs

#### Scenario: Owner-only merges stay explicit

- **WHEN** the Constitution's Foreman merge rule is read
- **THEN** it reserves Constitution amendments and `sdlc-exception` PRs for the Owner
