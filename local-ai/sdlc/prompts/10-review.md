# Apply the foreman's review of PR for #{{ISSUE}}

The foreman reviewed the pull request for `{{BRANCH}}` (Gate 4) and listed the fixes
below. Do exactly those, nothing else. Rules:

- Edit files with the edit tool; for the PR body, edit `{{IO}}/pr-body.md`, then run
  `gh pr edit {{BRANCH}} --body-file {{IO}}/pr-body.md`.
- Commit each file fix atomically, exactly as the notes say. Never push — the foreman pushes.
- Run every CHECK in the notes and compare with its EXPECTED before finishing.

## Foreman review

{{GATE_OUTPUT}}
