# AWS AI-DLC method boundary (#2124)

Upstream: [awslabs/aidlc-workflows](https://github.com/awslabs/aidlc-workflows)
(AI-Driven Development Life Cycle). Evidence read from its published guide
`docs/guide/04-phases-and-stages.md` (5 phases, 33 stages) and the project
README (native `aidlc` command, harness hooks that can run commands).

This document is the **design required before** `aws-ai-dlc` may become an
available factory plugin. It does **not** install AWS services, does **not**
ship the `aidlc` binary, and does **not** turn this kit into a second lifecycle
runner.

## Problem

AI-DLC is already a full phase runner. Loading it beside `aisdlc` without a
boundary would execute **two** lifecycles (double orchestration). Its harness
hooks can execute shell commands; auto-trusting them would bypass factory
gates.

## Phase ownership (who runs what)

Map AI-DLC's five phases onto this kit's stages (`intake` … `release`).
**"Own"** means: may drive the stage as orchestrator. The factory remains the
only orchestrator for delivery gates.

| AI-DLC phase | AI-DLC stages (summary) | Owns aisdlc stage? | Factory stance |
|---|---|---|---|
| **0 Initialization** | Workspace scaffold, detection, state init | **No** | Adopter profile + `aisdlc init` / project layout. Do not run `aidlc` init beside it. |
| **1 Ideation** | Intent, research, feasibility, scope, approval | **No** (content only) | May *inform* `intake` / `plan` prose. OpenSpec (or Spec Kit) stays the method store on Notaire. |
| **2 Inception** | Reverse eng, requirements, stories, units, delivery plan | **No** (content only) | Same as Ideation: optional input to `plan`. Never replaces Gate 1 validators. |
| **3 Construction** | Design units, code generation, CI pipeline | **No — forbidden** | `red` / `green` / `pre-push` / `pre-pr` stay on aisdlc gates, ratchets, and adopter scripts. |
| **4 Operation** | Deploy pipeline, feedback | **No — forbidden** | `release` and product ops stay on aisdlc + adopter CI. |

### Hard rule

AI-DLC **owns zero** aisdlc stages as orchestrator. If a future change ever
wires a dry-plan argv, it may only print a consultative command (for example
documentation or `aidlc doctor` inspection). It must never start `/aidlc`
Construction or Operation workflows from factory `plugin plan` / gate runners.

## Hooks: never auto-trust

AI-DLC installs harness hooks that can run commands on the machine. This
factory:

- Requires `auto_trust_hooks = false` on the `aws-ai-dlc` manifest.
- Rejects any plugin manifest with `auto_trust_hooks = true` during
  `config validate`.
- Does not execute hook installers from `plugin list`, `config validate`, or
  `plugin plan`.

## Availability

Until a later Issue explicitly opts the plugin into `[plugins] allow` **and**
adds a non-orchestrating dry-plan command, `aws-ai-dlc` remains:

- Declared in the profile (so the boundary is visible),
- `trusted = false`,
- **not available** on `plugin list`,
- `plugin plan` exits 2.

Status in the manifest: `boundary` (design implemented; not an executable
method adapter yet).
