## ADDED Requirements

### Requirement: The uncommitted-changes gate names the staging command

The message SHALL include `git add -A -- <files> && git commit --amend --no-edit`
for the files it lists.

#### Scenario: Unstaged edits after an amend

- **WHEN** the branch has uncommitted changes to `a.md`
- **THEN** the message contains `git add -A -- a.md && git commit --amend --no-edit`
