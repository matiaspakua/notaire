# Factory adopter demo path (#2243)

Kit-neutral walkthrough: seed one **approved** workflow, then open the factory
UIs. The kit never executes agents or MCP. Product admin surfaces (if any) stay
separate — this path does not replace them.

## Prerequisites

- Python 3.11+
- A profile with `[catalogs] dir` (and optional `[[stages]]` for Foreman-aligned pins)
- Kit on `PYTHONPATH` or installed from the wheel (`aisdlc` entry point)

## Seed

```bash
# Generic demo (scaffolds declare-only agent/skill stubs as needed)
aisdlc --profile path/to/aisdlc.toml demo seed --json

# Prefer agents/skills already declared on [[stages]] (no vanity skill stubs
# when [skills] dir is set — only folders that already contain SKILL.md)
aisdlc --profile path/to/aisdlc.toml demo seed --from-stages --json
```

Idempotent when the workflow is already `approved`. Default workflow id:
`factory-feature` (override with `--workflow ID`).

## Honesty checks (CU76)

```bash
aisdlc --profile path/to/aisdlc.toml doctor
aisdlc --profile path/to/aisdlc.toml catalog validate
aisdlc --profile path/to/aisdlc.toml report
aisdlc --profile path/to/aisdlc.toml evidence verify
aisdlc --profile path/to/aisdlc.toml workflow list --approved --json
```

## UI entry

| Surface | Command | What to open |
|---------|---------|--------------|
| OpenAPI host | `aisdlc serve --port 8765` | `/factory/workflows`, `/factory/capabilities`, `/ui/` when mounted |
| Backoffice | `aisdlc backoffice --serve --port 8765` | Workflows / Agents / Skills modules |
| Runtime | `aisdlc runtime --serve --port 8766` | Projects → bind repos → **approved** workflow → Start run → Monitor |

Static snapshots (no server):

```bash
aisdlc backoffice --html /tmp/backoffice.html --module workflows
aisdlc runtime --html /tmp/runtime.html --module start
```

## Second-adopter fixture

Kit tests create a temporary adopter root, run `demo seed`, and assert
independence (`aisdlc/`, `bin/`, `templates/` stay free of adopter product path
markers). A public sample repo + wheel time-to-first-gate remains packaging
work (#2256).

## Out of scope

- Replacing a product admin console with factory backoffice
- Context Lake / Port.io clones / Wilson vanity stages / fake Isolator
- Flipping `[committer] required=true` without Owner ask
