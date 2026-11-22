# E2E Playwright Tests

End-to-end test suite for Notaire using Playwright.

## Running the tests

```bash
cd testing/e2e
npx playwright test
```

## Starting the stack for a full run

A full Playwright run executes all tests as the single `admin` user across 2
workers. The default API rate limit (2 000 requests per 60 s) is insufficient
and will cause silent 429 rejections that break random tests.

Start the stack with a higher rate limit:

```bash
NOTAIRE_API_RATE_LIMIT_MAX=20000 bash workspace/stack/start.sh --no-admin
```

When running through `preflight.sh`, this is done automatically — the script
exports `NOTAIRE_API_RATE_LIMIT_MAX=20000` before starting the stack.
