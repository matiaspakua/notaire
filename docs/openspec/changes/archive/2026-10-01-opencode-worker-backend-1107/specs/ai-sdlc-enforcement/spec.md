## ADDED Requirements

### Requirement: The worker agent is selectable

The harness SHALL run the worker with the agent named by `AGENT` or the adapter's
`backend.agent`, keeping the git hooks path and the crawl-guard shims for either.

#### Scenario: OpenCode command

- **WHEN** the agent is `opencode`
- **THEN** the command is `opencode run --pure --auto --format json --dir <wt> -m <model> <prompt>`
  and the environment disables the project config and points `OPENCODE_CONFIG_DIR` at `local-ai/opencode`

#### Scenario: Codex command unchanged

- **WHEN** the agent is `codex`
- **THEN** the command is `codex exec --profile <profile> ... -o <last> <prompt>` as before

#### Scenario: Final message extracted

- **WHEN** an OpenCode JSON event stream ends with a `text` part `DONE`
- **THEN** the last message is `DONE`

#### Scenario: Guards kept

- **WHEN** the worker environment is built for either agent
- **THEN** it sets the git `core.hooksPath` override and puts `bin/shims` first on `PATH`
