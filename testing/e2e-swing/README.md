# testing/e2e-swing — RETIRED (do not wire into CI)

**Status:** Hard-deprecated. Issue [#811](https://github.com/matiaspakua/notaire/issues/811),
CU76, ADR-012.

The Swing desktop client was removed. Active UI E2E lives in
[matiaspakua/notaire-testing](https://github.com/matiaspakua/notaire-testing)
(`e2e/`). This stub remains in the product so Dependabot/hygiene guards keep
forbidding Swing CI wiring after the testing extract (#2514).

## Forbidden

- Do **not** add `.github/workflows/e2e-swing.yml` or any workflow that runs
  `robot` against this directory.
- Do **not** build Swing modules (`mvn -pl frontend-swing` /
  `deprecated-frontend-swing`).
