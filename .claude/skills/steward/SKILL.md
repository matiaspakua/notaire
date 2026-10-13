---
name: steward
description: Drive a Notaire pull request to green and merge-ready — required checks, heavy-CI rule, behind-main procedure, commit and archive rules, who merges, environment-only failures. Use on every PR event or check-in for a PR you opened.
---

# Steward — drive a Notaire PR to green

Restates rules owned elsewhere; when they change, change them there and keep this list short.
Owners: `CONSTITUTION.md` §5, `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md`,
`.claude/rules/ai-agent-workflow.md`, `.claude/rules/hooks.md`.

## 1. Read the whole PR first

Merge state, checks on the **current head**, open review threads. Light-only green is not
green: `bash workspace/sdlc/check-heavy-ci.sh <pr>` must exit 0 (needs `gh`; without it read the
check runs and require `Unit Tests`, `Integration Tests`, `Coverage Gate (mvn verify)`,
`API Tests (Bruno)` and `UI E2E Tests (Playwright)` to be success or workflow-skipped).

## 2. Required checks on `main`

`CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, `PR Validation` and `Process Checks`
(ruleset `protect-main`, strict: the branch must contain current `main`).

## 3. Behind or conflicting with `main`

merge `origin/main` into the branch, never rebase or force-push a pushed branch. Regenerate
generated files with their tool (`python3 workspace/sdlc/generate-testing-inventory.py`), rerun
the affected suites, push, then confirm it landed: `git ls-remote --heads origin <branch>`.

## 4. CI red

Reproduce locally with `bash workspace/sdlc/preflight.sh` (mirrors CI) before pushing a fix.
Fix the cause; never skip, `@Disabled` or quarantine a test, never push an empty commit.
Typical Notaire causes: stale `docs/300-development/303-testing/INVENTORY.md`, Spotless format
(`mvn spotless:apply -pl backend-api`), an unticked or unarchived OpenSpec change.

## 5. Commits and the OpenSpec change

Atomic Conventional Commits: plan, failing test, implementation, docs, archive. Only the commit
that completes an issue carries `Closes #n`; the others carry `Refs #n`. Archive the change in the
implementing PR (`cd docs && openspec archive <change> --yes`) after ticking groups 3–8
(`python3 workspace/sdlc/scaffold-openspec-change.py complete <change> --test <Test>`); the
hygiene gate `workspace/sdlc/check-openspec-archive-hygiene.py` fails otherwise. Several issues
may share one PR when each has its own change and commits. Never commit bot reports or `[skip ci]`.

## 6. Who merges

Workers never merge. The Foreman merges a Worker PR when every required check is green and
`check-heavy-ci.sh` exits 0; the Owner merges Constitution, `sdlc-exception` and harness PRs.
When only people are owed (approval, merge), say so once and wait.

## 7. Failures that are the environment, not the PR

A Claude Code cloud sandbox has no Docker daemon: pg-integration, Playwright, Bruno, DAST and the
Docker build are CI-only. `test_watchdog` fails there because PID 1 does not reap zombies. Spotless
needs a UTF-8 locale. `.claude/hooks/session-start-toolchain.sh` installs JDK 26, `openspec`,
`kustomize`, Trivy, `promtool` and `amtool` at session start; say so in the PR instead of hiding it.
