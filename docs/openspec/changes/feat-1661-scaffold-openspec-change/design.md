> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The notaire-sdlc templates carry instructional HTML comments. The validator rejects sections that are still only comments, and tasks and the coverage table need manual edits that agents repeat for each change.

## Goals / Non-Goals

**Goals:** Gate 1 in one command; mechanical completion bookkeeping.
**Non-Goals:** deciding content; replacing the openspec CLI.

## Decisions

1. **JSON input.** Agents already hold structured data; JSON avoids a second template language.
2. **Write whole files from the schema templates' headings**, not regex-patching comments, so output cannot keep placeholders.
3. **`complete` only ticks boxes the workflow requires before the PR**; groups 9.2 and 10-12 stay open for the real PR and deploy.

## Riesgos / Trade-offs

- [Template headings change] → the scaffold test runs the real validator and fails first.
- [Agents tick groups without doing the work] → `complete` is invoked after the suites ran; the CI task gate still checks every PR.

## Testing Strategy

`workspace/tests/test_scaffold_openspec_change.py` runs the tool in a temporary repo copy with the real validator and archive-hygiene task gate. Written first and observed failing.

## Regression Strategy

`python3 -m unittest discover -s workspace/tests`; `validate-sdlc-plan.sh`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
