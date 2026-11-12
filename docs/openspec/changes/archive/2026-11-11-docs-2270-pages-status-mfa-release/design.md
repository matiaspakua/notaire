## Context

#1640 shipped TOTP MFA. The portfolio still said there was no second factor. The committed release snapshot still said 0.3.0 while the manifest is 0.4.1. Local preflight called `next build` and skipped `prebuild`.

## Goals / Non-Goals

**Goals**

- Tell the MFA story honestly: shipped, OPTIONAL by default, ENFORCE for ADMIN and NOTARY.
- Keep the release history on the current product version.
- Make local preflight refresh `releases.json` the same way CI does.

**Non-Goals**

- Changing MFA policy or the product UI.
- Regenerating delivery-metrics.json in this change.
- Redrawing factory diagrams.

## Decisions

1. Status stays Partial: default OPTIONAL means MFA is not forced for every role.
2. SR-17 is listed under Spoofing beside AT-1.
3. `npm run build` in preflight is the same entry CI uses.

## Riesgos

A reader who expected "no MFA" will see a Partial control. That is the correct reading of OPTIONAL.

## Testing Strategy

`github-page/tests/pages-status.test.ts` fails if "no second factor" returns or if `releases.json` drifts from `.release-please-manifest.json`.

## Regression Strategy

Pages unit tests and `preflight.sh --full`.

## Playwright Strategy

Existing pages axe coverage. No new journey.

## Deployment Strategy

Pages publishes on the push to main.

## Rollback Strategy

Revert the pull request.
