# Accessibility — axe WCAG 2.1 AA (Playwright)

Issue [#1251](https://github.com/matiaspakua/notaire/issues/1251) / Wave 7 [#1955](https://github.com/matiaspakua/notaire/issues/1955).

## What runs

- Package: `@axe-core/playwright` in `testing/e2e`
- Spec: `testing/e2e/tests/TS-0155-axe-wcag-aa.spec.ts`
- Tags: `wcag2a`, `wcag2aa`
- Routes: login + featured dashboard paths listed in `tests/setup/axe.ts` (`AXE_ROUTES`)

## Allowlist

Pre-existing findings that must not block the suite live in
`testing/e2e/tests/setup/axe-allowlist.json`.

Each entry:

```json
{ "ruleId": "color-contrast", "target": ".some-selector", "issue": "#NNNN", "note": "why" }
```

Fingerprint = `ruleId|target` (first axe node CSS selector). New serious/critical
findings without an entry fail TS-0155. Prefer fixing over allowlisting.

## Local run

```bash
bash workspace/stack/start.sh
cd testing/e2e && npx playwright test TS-0155-axe-wcag-aa
```

Also covered by `bash workspace/sdlc/run_pipeline.sh` (preflight `--full`).
