# testing/e2e-swing — RETIRED (do not wire into CI)

**Status:** Hard-deprecated. Issue [#811](https://github.com/matiaspakua/notaire/issues/811),
CU76, ADR-012.

The Swing desktop client (`frontend-swing` / `deprecated-frontend-swing`) was
removed from the repository. The GitHub Actions workflow `e2e-swing.yml` is
retired and must not return. Active UI E2E is Playwright under `frontend/tests/e2e/`.

## Forbidden

- Do **not** add `.github/workflows/e2e-swing.yml` or any workflow that runs
  `robot` against this directory.
- Do **not** build Swing modules (`mvn -pl frontend-swing` /
  `deprecated-frontend-swing`).
- Do **not** treat `run_tests.sh` / `setup_env.sh` as supported operator docs.

## Why these files remain

Historical Robot suites and `requirements.txt` stay on disk so ignore-rule
hygiene (#1050) keeps working and so git history retains context. They are
**not** a supported test path.

## Supported E2E

```bash
cd frontend && npm run test:e2e
```

See `docs/300-development/303-testing/README.md`.
