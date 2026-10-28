> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`scaffold-openspec-change.py` writes `### New Capabilities` then immediately a list
(MD022/MD032). The `traceability.md` template uses a fence without a language (MD040).
The `spec.md` template puts the Scenario list against the heading. Seeded changes inherit
the same defects when agents clear HTML comments without restoring blank lines.

## Goals / Non-Goals

**Goals:** a fresh scaffold (and seed templates) lint clean with repo markdownlint-cli2;
a workspace test locks that in.

**Non-Goals:** changing lint rules; mass-fixing archived OpenSpec history; changing
`openspec archive` itself.

## Decisions

1. **Fix generators and templates** rather than a post-scaffold autofix script — fewer
   moving parts; agents who seed then edit get blank lines from the template.
2. **Archive autofix** — `openspec archive` strips blank lines around Purpose/Requirements
   when creating main specs. Documented follow-up:
   `markdownlint-cli2 --fix --no-globs` on touched `docs/openspec/specs/<cap>/spec.md`.
3. **Fence language** — use `text` on the Chain diagram fence in `traceability.md`.

## Riesgos / Trade-offs

- [Template edits surprise agents mid-fill] → keep instructional HTML comments; only add
  blank lines and a fence language.
- [Test skips without frontend node_modules] → skip with a clear message like other
  markdownlint integration tests; CI/pipeline always has deps.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fresh scaffold has zero lint findings | unit/integration | `test_scaffolded_change_passes_markdownlint` |
| Seed templates lint clean | unit | `test_schema_templates_pass_markdownlint` |

## Regression Strategy

Existing `test_scaffold_openspec_change.py` cases must stay green.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release; tooling-only.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
