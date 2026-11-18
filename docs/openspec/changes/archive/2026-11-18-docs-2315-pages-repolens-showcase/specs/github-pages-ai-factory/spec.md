<!-- Governed by CONSTITUTION.md. CU76, issue #2315. -->

## Purpose

Extend the AI Factory GitHub Pages section with a RepoLens showcase grounded in committed benchmark files.

## ADDED Requirements

### Requirement: RepoLens page lives in the AI Factory

The AI Factory section SHALL include `/ai-sdlc/repolens/` in the shared document shell, linked from the factory navigation and kept outside product `/docs/`.

#### Scenario: Visitor opens RepoLens from the factory sidebar

- **WHEN** a visitor opens any `/ai-sdlc/` page
- **THEN** the sidebar lists a RepoLens entry whose href is `/ai-sdlc/repolens/`

#### Scenario: Home and reading path surface RepoLens

- **WHEN** a visitor opens the home page or the factory reading path
- **THEN** a link to `/ai-sdlc/repolens/` is present

### Requirement: RepoLens story covers basics, mechanics, factory plug-in, and the learning loop

The RepoLens page SHALL explain in plain English what RepoLens is, how it works (SQLite+FTS5, tree-sitter, embeddings, typed Personalized PageRank, CLI/HTTP/MCP), how it plugs into factory stages (plan/red/pre-pr, advisory signal gate, per-worktree index, shared vector cache), and the self-improvement loop with cited fixes such as CLI usage telemetry and ambiguous same-directory call edges.

#### Scenario: Reader learns the factory plug-in

- **WHEN** a reader opens `/ai-sdlc/repolens/`
- **THEN** the page names plan, red, and pre-pr MCP stages and the advisory `repolens report --gate` signal

#### Scenario: Learning loop cites real fixes

- **WHEN** the learning-loop section is read
- **THEN** it cites repository evidence for usage telemetry and ambiguous-call fixes without inventing outcomes

### Requirement: Benchmark charts are generated from committed result files

The page SHALL render interactive charts for the latest committed offline retrieval and agent benches (hit@10 / recall@10, latency, bytes per query, agent tool calls) and the live factory-loop A/B comparison from committed markdown/JSON under `framework/ai-sdlc/repolens/bench/results/`. Numbers SHALL match those files (or the report generator's published headline labels derived from them); charts SHALL show sample size and caveats; dark mode and ES/EN SHALL work, keeping English technical terms where clearer.

#### Scenario: Chart data tracks the committed snapshot

- **WHEN** the Pages app builds
- **THEN** chart values come from a build-time or committed snapshot sourced from `framework/ai-sdlc/repolens/bench/results/` and unit tests fail if key published ratios drift from the JSON sources

#### Scenario: Theme and locale

- **WHEN** a visitor toggles dark mode or Spanish
- **THEN** the RepoLens page remains readable and translated routes include `/ai-sdlc/repolens/`
