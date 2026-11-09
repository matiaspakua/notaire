# Delivery metrics (#2138 / #2184)

## Lead time

**Event pair (fixed):**

| Pole | Event | Meaning |
|------|-------|---------|
| Start | `issue.created_at` | Forge issue open timestamp |
| End | `pull_request.checks_green_at` | Earliest linked PR whose **required checks are all green** |

Merge is **optional** and is not part of this metric. A PR without `checks_green_at` or without the issue in `linked_issues` is ignored. If no candidate exists, status is `no-evaluable` (not a pass).

### Offline fixture (#2138)

```bash
python3 tools/ai-sdlc/bin/aisdlc metrics lead-time \
  --fixture framework/ai-sdlc/tests/fixtures/lead-time-notaire-sample.json --json
```

The Notaire sample fixture models issue #2135 → PR #2144 green (6300s).

### Live read-only ForgePort (#2184)

```bash
python3 tools/ai-sdlc/bin/aisdlc metrics lead-time --issue 2182 --json
```

Uses `gh` **read-only** (`issue view`, `pr list`) to build the same sample shape, then the same calculator. Exactly one of `--issue` / `--fixture` is required.

**Bounds:** never creates/edits/closes issues or PRs, never creates/updates check runs, never writes `factory/gate` attestations. Empty check rollup on a **merged** PR uses `mergedAt` as `checks_green_at` (Notaire #2001: missing checks with no workflow run count as success). Pending or failing checks → omit green → `no-evaluable`.

When TelemetryPort is enabled, the same result is emitted as `aisdlc.lead_time` (secrets/prompts/diffs redacted).

## Stage durations

Derived from the evidence ledger: per stage run, sum gate `seconds`, then median across runs.

```bash
python3 tools/ai-sdlc/bin/aisdlc metrics stages --json
python3 tools/ai-sdlc/bin/aisdlc report --json   # includes "stages" + event pair pointer
```

## Non-goals

Org-wide ROI dashboards, Port.io Context Lake, exporting prompt/diff bodies.
