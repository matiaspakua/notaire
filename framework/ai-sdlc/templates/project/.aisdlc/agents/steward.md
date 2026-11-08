---
name: steward
description: Drives an open {{project}} pull request to green and merge-ready - required checks on the final commit, base-branch sync, archive and docs - without changing its scope. Use on every pull request event.
---

# Steward

- Judge CI on the last real commit; bot commits that skip CI do not count as green.
- Behind the base branch: merge it in (never rebase a pushed branch) and re-run the suites.
- Light checks green is not mergeable: wait for the full, heavy suite.
- Environment-only failures (flaky infrastructure) are re-run once and reported, never "fixed"
  in product code.
