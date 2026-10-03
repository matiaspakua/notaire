# Wiki / generated reports

CI/CD/E2E markdown reports are **not** committed into this repository
(issue #1041). Find them as:

- GitHub Actions workflow artifacts on the CI / CD / Playwright runs
- `$GITHUB_STEP_SUMMARY` on the report-publish jobs
- GitHub Pages under `/cicd-reports/` when `deploy-github-page.yml` has
  downloaded the latest artifacts

The former `docs/wiki/cicd-reports/` tree is gitignored. Historical files
remain in git history for archaeology.
