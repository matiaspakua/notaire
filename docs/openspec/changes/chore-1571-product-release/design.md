# Design — product release

## Context

release-please cuts one component named `notaire`. The manifest is `0.1.2`,
the frontend package matches, and Maven is `1.0-SNAPSHOT`. Tags are
`notaire-v0.1.2`. CD attaches SBOMs only when the ref starts with
`refs/tags/v`, so v0.1.2 has no assets. The project-management schema
(#1561, #1562) schedules trains; it does not define a product cut.

## Goals / Non-Goals

**Goals**

- One committed record of backend, frontend, and database schema.
- Tags `vX.Y.Z` going forward, with CD still accepting `notaire-v*`.
- A defined CycloneDX product bill and a database bill, attached on the tag.
- The running app and GitHub Pages show that version.
- Docs that stop calling a milestone a release.

**Non-Goals**

- Renaming GitHub milestones.
- Cutting `v1.0.0` or closing the v1.0 train.
- Making the Flyway id equal the semver.
- Adding a Released column to the board.
- Editing the release-please branch in this change.

## Decisions

1. **Derive the train, do not store it** in `product-release.json`.
   release-please bumps version fields and would leave a stored train stale
   on `main`. `train_for()` is the map. An unmapped minor fails the check.
2. **Record the schema id beside the semver.** Highest `V*` filename. New
   migrations must refresh the JSON in the same change.
3. **Filter `app.version` with `@` delimiters** so Spring `${ENV:default}`
   placeholders are not rewritten. OpenAPI and actuator read `app.version`.
4. **Generate CycloneDX at tag time**, not as a second committed copy, so a
   release-please bump cannot desync the bill. The committed contract is
   `release/product-release.json`.
5. **Pages history is built from the changelog** in `prebuild`. The narrative
   timeline is left intact.
6. **Legacy tags stay in a constant** (`0.1.1`, `0.1.2` → `notaire-v*`).
   Newer versions use `v*`.

## Riesgos

| Risk | Mitigation |
|------|------------|
| release-please still emits `notaire-v*` if the flag is ignored | CD accepts both prefixes; the config test locks `include-component-in-tag: false` |
| Maven drifts back to `1.0-SNAPSHOT` and extra-files silently skip | `--check` compares pom, parent, npm, manifest |
| A future `1.2.0` has no train | `train_for()` raises; the release check fails until RELEASES.md names the train |
| Filtering corrupts `${}` in `application.properties` | Delimiter is `@` only; test asserts the token and `useDefaultDelimiters` false |
| Image SBOM files missing makes the attach step fail | Pre-existing. Product and database bills are always generated |

## Testing Strategy

- Python: train map, three-component record, changelog parser ignores
  `1.0.0-SNAPSHOT`, CycloneDX shape, CD tag filter, doc guards.
- JUnit: actuator section and OpenAPI version.
- Vitest: sidebar renders `package.json` version.
- Red was observed: Maven `1.0-SNAPSHOT` ≠ manifest `0.1.2`, missing
  `include-component-in-tag`, missing product record, `app.version` still a
  Spring placeholder (`python3 -m unittest workspace.tests.test_product_release`).

## Regression Strategy

- Existing semver and frontend GHCR tests stay. CD still publishes both images
  and still attaches the image bills.
- OpenAPI tests assert the injected version instead of a hardcoded `1.0.0`.
- No Flyway file is edited.

## Playwright Strategy

No new product route or form. The version line is covered by
`app-sidebar.test.tsx`. Playwright is n/a for this change.

## Deployment Strategy

Merge to `main`. The next release-please run should bump `0.1.2` → the next
semver in the pom, the parent, the package, and `product-release.json`, and
tag `vX.Y.Z`. CD on that tag attaches the five release assets. Pages rebuild
after CI on `main` and regenerate the history in `prebuild`.

Do not merge the already-open release-please pull request until this is on
`main`, or that cut repeats the `notaire-v*` tag and the Maven skip.

## Rollback Strategy

Revert the merge. Tags already pushed are not moved. Legacy `notaire-v*`
filters remain, so 0.1.2 behavior is unchanged. `app.version` returns to the
placeholder only if the properties line is reverted with the filtering plugin.
