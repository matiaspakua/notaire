# Design — frontend lockfile pins a patched katex

## Context

`katex` is a transitive dev dependency of the docs linter only:
`markdownlint-cli2@0.24.0` → `markdownlint@0.42.0` → `micromark-extension-math@3.1.0` (`katex@^0.16.0`).
Dependabot's security update cannot satisfy the advisory (`>= 0.11.0 < 0.18.2`) without downgrading
`markdownlint-cli2` to 0.16.0, so it errors on every run. markdownlint uses the math extension to
*parse* `$`/`$$` syntax; it never calls katex to render.

## Goals / Non-Goals

**Goals:** resolve a patched katex; keep `markdownlint-cli2` working. **Non-Goals:** changing lint rules or docs.

## Decisions

- Preferred fix (upgrade `markdownlint-cli2`) is unavailable: 0.24.0, `markdownlint` 0.42.0 and
  `micromark-extension-math` 3.1.0 are the latest releases. Fallback: npm `overrides`
  `"katex": "^0.18.2"`, the same pattern as `smol-toml` (#1046).
- Caret on 0.x keeps the pin within 0.18.x, the smallest jump past the advisory floor.

## Riesgos / Trade-offs

- npm overrides ignore the declared `^0.16.0` range. Risk is low: the parse path does not touch katex,
  and the scenario test runs `markdownlint-cli2` on math Markdown.
- Remove the override once upstream accepts katex 0.18.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| package.json declares a katex override | unit | `test_package_json_declares_patched_katex_override` |
| lockfile resolves katex to a patched version | unit | `test_lockfile_resolves_patched_katex` |
| markdownlint-cli2 still lints the repository Markdown | integration | `test_markdownlint_cli2_runs_on_math_markdown` (skips without `frontend/node_modules`) |

## Regression Strategy

`security/tests/` (incl. `test_dependabot_hygiene.py`), frontend `npm ci`, build, lint, typecheck and Vitest in CI.

## Playwright Strategy

n/a — dev-only dependency; CI runs the Playwright suite on the PR.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- Dev dependency only; the runtime image is unchanged.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed

## Migration Plan

1. Merge. 2. Dependabot's katex alert closes on the next scan of `main`.

## Open Questions

None.
