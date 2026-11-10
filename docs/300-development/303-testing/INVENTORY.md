# Testing inventory

> **Stub (committed).** The generated suite-count table is **not** committed on
> feature pull requests — that monolith was a parallel-PR merge hotspot (#2117).
>
> Disk discovery is the source of truth. Preflight runs
> `python3 workspace/sdlc/validate-testing-inventory.py`, which checks Playwright
> `TS-nnnn` naming and that backend / frontend / Bruno trees are readable, then
> prints live counts.

## On-demand / docs-build report

```bash
python3 workspace/sdlc/generate-testing-inventory.py
# writes docs/300-development/303-testing/INVENTORY.generated.md (gitignored)
python3 workspace/sdlc/validate-testing-inventory.py
```

The github-page `prebuild` script regenerates the same report for local docs
builds. Live Pages metrics also discover counts from disk
(`github-page/lib/tech-facts.ts` → `testCounts()`).

Do **not** commit `INVENTORY.generated.md` on feature PRs. After merging
`main`, regenerate locally if you need the table — never keep either side's
count line from a conflict.
