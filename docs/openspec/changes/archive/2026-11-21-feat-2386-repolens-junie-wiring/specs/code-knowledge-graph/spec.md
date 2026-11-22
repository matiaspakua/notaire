## ADDED Requirements

### Requirement: `repolens setup` wires the Junie CLI alongside every other harness

`repolens setup` SHALL register the RepoLens MCP server with every supported harness: Claude Code, Cursor, Codex, OpenCode, and Junie. The Junie configuration file `.junie/mcp/mcp.json` SHALL use the same launcher command and arguments as the other harnesses.

#### Scenario: setup writes Junie MCP config

- **WHEN** `repolens setup` runs on a repository
- **THEN** `.junie/mcp/mcp.json` is created with `{"mcpServers": {"repolens": {"command": ..., "args": [...]}}}`
  using the same launcher as `.cursor/mcp.json`

#### Scenario: dry-run reports Junie drift

- **WHEN** `repolens setup --dry-run` runs and `.junie/mcp/mcp.json` is missing or stale
- **THEN** the output lists `junie` with the files it would update, without writing them

#### Scenario: committed wiring matches the generator

- **WHEN** `repolens setup --dry-run` runs on the committed wiring
- **THEN** no drift is reported for the `junie` target
