# Fix a failing quality gate for #{{ISSUE}}

The foreman ran `{{GATE_CMD}}` on branch `{{BRANCH}}` and it failed.
Output (tail) is below. Find the root cause and fix it — in the code, the
test, the doc or the config that is actually wrong. Rules:

- Formatting/lint: run `bash scripts/preflight.sh --fix` first, then fix the rest by hand.
- Do not disable checks, add suppressions, `@Disabled`, `.skip`, or lower thresholds.
- If the failure is clearly unrelated to this branch (it also fails on `main`),
  write the evidence to `{{IO}}/BLOCKED.md` and stop.
- Re-run `{{GATE_CMD}}` yourself before finishing.
- Commit the fix atomically: `<type>(<scope>): <fix>` with body `Refs #{{ISSUE}}`.

## Gate output

```text
{{GATE_OUTPUT}}
```
