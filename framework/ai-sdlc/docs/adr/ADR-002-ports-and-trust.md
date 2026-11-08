# ADR-002: Ports, and what is not a sandbox

**Status:** Accepted as a contract. Only the method-plugin read path is implemented.

## Decision

The core talks to the outside through ports:

| Port | Responsibility | This slice |
|------|----------------|------------|
| Method | Planning workflow (validate, plan) | OpenSpec argv is printed. Example plugin loads. Spec Kit adapter prints `specify extension search` (out of allow on Notaire). AWS AI-DLC is not loaded. |
| Harness | Agent runtime (Cursor Cloud fleet, local oMLX foreman, …). Not a commercial Harness product. | `harness list` / `harness show` read profile `[harness.adapters]` sources in place. Always reports `sandbox=false`. |
| Tracker | Issues and pull requests | Not implemented. The profile may name a verify command; this slice does not run it from `plugin list`. |
| Gate | Quality commands | Existing `gate` command. Unchanged. |
| Evidence | Hash-chained ledger | Existing `evidence` command. Unchanged. |
| Config | Profile, ceiling, redaction | `config validate` and `config show --redacted`. |

A plugin manifest is data. `plugin list`, `plugin capabilities`, and `config validate` read TOML. They do not import plugin code and they do not spawn the command.

`plugin plan` fills an argv list and prints it. It does not pass the line through a shell.

A relative manifest that escapes the project root is rejected. That check is a path rule. It is not an operating-system sandbox. A declared permission in a manifest is not isolation. This slice does not claim a sandbox.

The open pull-request cap in the profile is a ceiling. Environment and flags may lower it. They cannot raise it. `pr_cap_decision` is a pure function. It does not take a distributed lock. Claim races remain the existing claim script's problem.

Secrets in config are `env:NAME` references or they are redacted. The value of the environment variable is not printed.
