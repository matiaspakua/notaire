## Context

The factory diagrams already match main. The product story page still drew "~80% of AI work" and hid cards until an intersection observer ran. The numbers page still said the legacy JPA package was being migrated after #576 retired it.

## Goals / Non-Goals

**Goals**

- Remove the unmeasured bar.
- State the JPA retirement as a fact.
- Keep the story cards on screen, including when reduced motion is set.
- Translate the release-history lead.

**Non-Goals**

- Regenerating delivery metrics.
- Redrawing the anatomy figures.
- A new diagram library.

## Decisions

1. Roles stay as sentences. A bar needs a measured numerator.
2. The fade-in is removed. Content that starts at opacity 0 is invisible without JavaScript and ignores reduced motion.
3. The AI-era chips name `preflight.sh --full` and a version tag. They do not say heavy CI is the merge.

## Riesgos

A reader who liked the percentage loses a number that was never sourced. The replacement is the role of each tool.

## Testing Strategy

`github-page/tests/story-honesty.test.tsx` fails if the 80% bar returns, if the JPA sentence says the package is still being migrated, or if the release lead has no Spanish template.

## Regression Strategy

Pages unit tests, typecheck, and the existing Playwright site spec run inside `preflight.sh --full`.

## Playwright Strategy

No new journey. The pages axe run in preflight covers the product story and the numbers page in both themes.

## Deployment Strategy

GitHub Pages publishes on the push to main. This pull request does not publish product images.

## Rollback Strategy

Revert the pull request. The story page returns to the previous copy.
