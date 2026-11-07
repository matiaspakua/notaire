<!-- Governed by CONSTITUTION.md. CU76, issue #2052. -->

## ADDED Requirements

### Requirement: Agents do not stop with unpublished Issue-branch commits

The Claude Code `Stop` hook MUST block the stop once, naming `publish-progress.sh <issue>`, when the current branch follows `<type>/<issue>_<desc>` and holds commits that are on no remote, and MUST let the stop through on the retry (`stop_hook_active`), on `main`, on other branches, after a push, and on any error.

#### Scenario: local-only commits on an Issue branch

- **WHEN** the agent stops on `fix/1620_x` with a commit on no remote
- **THEN** the hook exits 2 and names `publish-progress.sh 1620`

#### Scenario: the retry passes

- **WHEN** the same stop is retried with `stop_hook_active` true
- **THEN** the hook exits 0

#### Scenario: pushed work passes

- **WHEN** the branch's commits are on a remote
- **THEN** the hook exits 0
