# Changelog

All notable changes to repolens. Versions follow [SemVer](https://semver.org); the version lives in
`repolens/__init__.py` and release tags are `repolens-v<version>`.

## [0.6.1] - 2026-11-19

### Changed

- Search defaults to 4 detailed hits plus 4 one-line hits. Snippet defaults to 32 lines. Impact defaults to depth 2. Signatures in search hits are capped at 80 characters and symbol lists at 6. On the session fixture, 10 detailed hits for `code verification` were 1697 bytes; the new default was 940 bytes.
- A stale index no longer blocks the query. The query starts the same detached `index --no-wait` the `post-merge` hook starts, and answers from the previous graph. An index written by other extractors uses that same background refresh. The next query sees the result.

## [0.6.0] - 2026-11-19

### Added

- Orphans: `repolens orphans`, `GET /api/orphans`, the MCP `orphans` tool and the explorer's Orphans view list
  the files that relate to no other file, then the files related only through git history; `--symbols` adds
  symbols with no relation. Demoted paths and files a tool reads by name (`[orphans] conventional`:
  `package-info.java`, `LICENSE`, `__init__.py`, `*.snap`...) are counted, `--all` lists them. `repolens index`
  prints the counts (#2338).
- Any language: a file name the tree-sitter pack knows gets its language; a language without a hand-written spec
  but with a tags query (71 in pack 1.14: Ruby, PHP, Dart, Elixir, Scala, Swift, C, C++, Lua...) yields its
  definitions, calls and type references through it. A grammar that cannot load is tried once per process and
  the file is indexed as text. On the scale corpus, 18 code languages yield symbols (was 15).
- Any text links the files and directories it names (full, `./`, `../`, web-root and bare names; code files
  too) and configuration and text link the code identifiers they name. An ambiguous name links the nearest
  candidate, or nothing when more than three tie.
- `bench/orphan_bench.py`: orphans, coverage and a precision proxy on real repositories.

### Fixed

- ESM TypeScript imports that name the emitted file (`import ... from './x.js'` for `x.ts`, as vscode and
  NodeNext projects write them) resolve to the source. On the scale corpus, resolved imports 109,129 ->
  255,070 and calls 547,866 -> 1,028,599: calls are scoped by the imports they go through.
- An ambiguous path mention linked the first three files with that name; it now links the nearest one.
- An index written by other extractors than the running code's is stale, not only one behind HEAD: a branch
  switch could leave the older checkout's index stamped with the current HEAD (Notaire served version 6 at
  the 0.6.0 commit). An index run never downgrades: a process started before an upgrade (a long-lived MCP
  server) leaves a newer store as is instead of re-extracting it with its older extractors.

### Changed

- Measured on the same content before and after (`bench/orphan_bench.py`): files related to nothing, Notaire
  195 -> 62 and scale 3,821 -> 638; of those, named by another file (a link repolens missed) 27 -> 9 and
  1,486 -> 110. Retrieval unchanged on Notaire (tuning 0.904 / 0.788, held-out 0.786 / 0.882) and on the scale
  tasks (recall@10 0.825, MRR 0.420 -> 0.418).
- Costs: a full re-extract of Notaire 5.8 -> 6.7 s (link 1.4 -> 1.9 s), of the scale corpus 36.8 -> 41.0 s;
  scale query p50 64 -> 83 ms (39% more edges walked).
- MCP: seven core tools (`orphans` added), instructions trimmed; tool list plus instructions 3,941 bytes,
  under the 4,000-byte budget.
- `.txt`, `.inc` and `.log` stay plain text (the pack detects vimdoc and sourcepawn).
- Extractor version 7: the first index after the upgrade re-extracts every file.

## [0.5.0] - 2026-11-19

### Added

- Java calls link through the receiver's type: locals, fields, parameters, lambda parameters, return types,
  generic arguments and Spring Data repository methods. Against the javac call graph of `backend-api`
  (`bench/caller_bench.py`, 6,745 pairs) precision 0.714 -> 0.989, recall 0.852 -> 0.954. `impact` on 150
  shared method names finds 0.968 of the real callers (was 0.768); 1.3% find none (was 14%) (#2336).
- `impact` starts from the interface and superclass methods a method overrides, so callers through a port count.
- A resident per-worktree query service answers CLI queries over a Unix socket in a private per-user folder.
  `search` 1.252 -> 0.086 s, `impact` 0.767 -> 0.095 s p50 on Notaire (`bench/cli_latency.py`).
  `repolens daemon status|stop`; `REPOLENS_DAEMON=0` turns it off.
- `repolens cache export|import` carries the vector cache to another machine: a fresh Notaire worktree indexed
  in 9.1 s with 84,964 of 84,965 vectors from an imported cache. `[embed] skip` keeps paths out of the vector
  index (opt-in: half the vectors, held-out MRR -0.042).
- Several repositories: every index run registers its store (`repolens repos`); `search --repos all|a,b` and
  MCP `search(repos=...)` ask each one and label hits `[repo] path`. Answers are weighted by each store's
  relevance to the question, so federated recall@10 equals the owning store's (0.786 Notaire held-out, 0.825
  scale) where plain interleaving dropped it to 0.628 / 0.575. A repository that cannot place a hit in the
  top k is not searched: federated p50 45 ms on Notaire (`bench/federation_bench.py`).

### Changed

- `impact` and `node` list each file once with its symbols as `name:line` and count `[ranking] demote` paths
  instead of listing them: ten `impact` answers 61,232 -> 24,524 bytes, ten `node` answers 28,212 -> 12,837,
  no other file dropped. `--json` keeps every entry and adds a `demoted` flag.
- MCP tools return plain text once: FastMCP added an output schema per tool and a structured copy of every
  answer. Tool list plus instructions 4,701 -> 3,803 bytes, pinned under 4,000 by a test.
- The extractor version is 6: the first index run after the upgrade re-extracts every file.

### Fixed

- An unknown name is no longer answered with the top semantic hit. `impact TestimonyService` reported the
  `TestimonyWriteService` constructor with "0 files, 0 tests"; it now answers `not found` with the five
  closest symbols, types first (CLI and MCP).
- `verify.sh` runs its smoke index with a private `REPOLENS_HOME` and no resident service.

## [0.4.5] - 2026-11-19

### Fixed

- A query (`search`, `impact`, and the other read commands) compares the commit stored at index time with `HEAD`. When they differ it runs an incremental index before it answers and says so on stderr. Git `post-checkout` / `post-merge` hooks are unchanged. A cloud VM that never ran those hooks still picks up a merge on the next query (#2331).
- On a two-file fixture the old graph returned 0 hits for `brand_new_symbol` in 0.0019s. The query then printed `index refreshed in 0.01s` and listed `src/new.py`. The whole query took 0.0164s. This is not a measurement of a full Notaire reindex.

## [0.4.4] - 2026-11-19

### Fixed

- A search whose query is one identifier, and whose name index already hits that identifier, does not run the embedding query. A prose question still does. On the fixture, `CodeVerificationService` made zero `embed_query` calls and `verify one-time code service` made one (#2325).
- That same exact hit adds nodes that call it (`calls` edges) even when full-text search returns nothing. `codeGateway.verify` with an empty full-text result and `graph=False` still lists `VerifyForm.tsx` (#2325).
- `repolens report --gate` prints `adoption n/a` when the log has tool calls and zero harness sessions. CLI events have no session id, so `adoption 0%` was not a measured rate. A log with harness sessions still prints the percentage (#2325).

## [0.4.3] - 2026-11-18

### Added

- `repolens miss "<query>"` records a grep/read fallback in `.repolens/usage.jsonl` (`tool` is `miss`, `results` is 0). Agents run it when a RepoLens answer is unusable. It does not search.

## [0.4.2] - 2026-11-18

### Fixed

- A strict call whose name matches several methods in the same directory is no longer linked to the first three of them. `impact Folio.getDto` on the indexed checkout listed `History.getDto`, which does not call `Folio.getDto`. A uniquely named method in that directory still links. Measured on a three-class fixture: before the change `Reader.show` linked both `Deed.getDto` and `Folio.getDto`; after it, that call links neither, and `Reader.once` still links `Deed.onlyOnce` (#2307).

## [0.4.1] - 2026-11-18

### Fixed

- CLI query commands (`search`, `node`, `outline`, `snippet`, `neighbors`, `impact`, `path`) append
  `.repolens/usage.jsonl`. `repolens report` counts those events with the server calls (`client` is `cli`).
  Before this, a CLI search left the report at 0 events (#2307).
- The MCP extra now requires `mcp>=1.2,<2`. mcp 2.0 removes `mcp.server.fastmcp.FastMCP`, and a fresh
  install of `mcp>=1.2` resolved to 2.x and failed `verify.sh` (4 MCP tests). Measured on this VM after
  `pip install pytest` upgraded mcp.

## [0.4.0] - 2026-11-17

The factory release (#2298): repolens becomes the repository-knowledge tool of every agent harness, works
while many agents create branches, worktrees and pull requests, and measures its own use.

### Added

- **Zero configuration**: from anywhere in a repository, repolens uses the nearest `repolens.toml`, else
  indexes the repository; the store lives in `<worktree>/.repolens/` (one index per worktree and branch).
- **`repolens setup`**: registers the MCP server with Claude Code (`.mcp.json`, `.claude/settings.json` with
  the usage hook), Codex (`.codex/config.toml`), OpenCode (`opencode.json`, path from `[harness] opencode`),
  Cursor (`.cursor/mcp.json`), adds git `post-checkout`/`post-merge` refresh hooks and ignores `.repolens/`;
  idempotent and merging. `--install` creates the shared dependency environment, `--index` builds the first
  index in the background. **`repolens doctor`** checks the result.
- **Launcher `bin/repolens`**: runs the repository's own checkout from any directory with the shared
  environment, so each worktree serves its own code.
- **Shared vector cache** (`~/.cache/repolens/vectors`, with the PCA basis): vectors are content-addressed,
  so a new worktree or branch embeds only text no store has embedded (Notaire: a new worktree indexed in 11 s
  with 82,379 of 83,241 vectors from the cache, against ~4 min cold).
- **Background refresh**: before a tool call the MCP server starts an incremental index (at most every
  `REPOLENS_REFRESH_SECONDS`, default 15) on its own connection; an inter-process index lock serialises runs
  from several agents and git hooks. A first start builds the index in the background and says so.
- **Usage telemetry and `repolens report`**: the server logs every tool call (client, latency, size, files
  returned), the Claude Code hook logs Read/Grep/Glob; the report gives adoption, hit rate, grep fallbacks,
  navigation tokens, latency, zero-result queries and recommendations; `--gate` feeds the factory's evidence
  ledger.
- `bench/results/charts/headline.svg`: one chart, repolens against grep-driven navigation.

### Fixed

- A long-lived engine never saw a refreshed index: the query-term stemmer wrote a scratch table through the
  store's connection, leaving a transaction open that pinned the reader to its first snapshot (and stalled the
  writer's checkpoint). The stemmer now has its own in-memory connection.
- After a refresh refitted the embedding space (LSA, a new PCA basis) the engine kept the old query embedder;
  it is rebuilt on every reload.
- The embedding model loads from the local cache first (no network round trip on every server start).

### Changed

- Module folder `framework/ai-sdlc/graph-db` -> `framework/ai-sdlc/repolens`.
- Benchmark clones, the scale corpus and agent transcripts live in `$REPOLENS_BENCH_HOME` (default
  `~/.cache/repolens/bench`), outside the repository tree; the benchmark deletes this module (its answer
  key) from the corpus clone.
- Removed `examples/` (Notaire's profile is the root `repolens.toml`; harness files come from `setup`) and
  superseded benchmark snapshots.

## [0.3.0] - 2026-11-17

**The project was renamed from `codekg` to `repolens`** (package, `repolens` command, `python -m repolens`,
`REPOLENS_*` environment variables, `.repolens/` default folder, `~/.cache/repolens` model cache, MCP server
name `repolens`, release tags `repolens-v<version>`). The name had not been released before, so there is no
compatibility shim; move an old model cache with `mv ~/.cache/codekg ~/.cache/repolens`.

Scale iteration: measured with `bench/scale_bench.py` on a 33k-file, ~9M-line multi-repository corpus
(spring-framework + vscode) and re-validated on the Notaire task sets. Stores re-extract on first index
(extractor version 4) and re-embed only the texts that changed.

### Fixed

- **License headers were the documentation of every file**: the file node's text (and so its embedding and
  keywords) was the leading comment, which in a licensed codebase is the same Apache/MIT header everywhere.
  Leading license blocks are now skipped, also for the first symbol of a file.
- **Exported TypeScript declarations lost their JSDoc** (`export function f` keeps its comment on the export
  statement); comment terminators (`*/`) no longer leak into docs.
- **Locals of anonymous functions were module symbols**: variables inside test-suite callbacks and IIFEs
  became `variable` nodes (107k of 545k nodes at scale). They are no longer extracted.
- **Query-term pruning used wrong document frequencies**: the stem was guessed by prefix, so frequent words
  (`exception`, `request`) looked rare and were never pruned. Stems now come from SQLite's own tokenizer.
- The LSA fallback refitted and re-embedded everything on every run, even when nothing changed.

### Changed

- **Linker**: same-named candidates are grouped once per name, kind and language and indexed by file,
  directory and project (69 s -> 8 s at scale, identical edges); symbol references are streamed from the
  store instead of held in memory, and edges are written in bounded batches (link peak 2.1 -> 1.2 GB).
- **PageRank** runs on sparse vectors: mass below `ppr_eps` (1e-5) is not propagated, so the cost follows the
  walk's reach, not the graph size (83 -> 30 ms at scale; same rankings on the Notaire sets).
- **Engine memory**: node ids, names and qualified names are read from SQLite per rendered result instead of
  being held for every node; containment is resolved by one SQL join (engine heap 310 -> 133 MB at scale).
- **Embedding**: the embedding text is 300 characters (was 600; same recall on both task sets, ~30% less
  compute on long texts); the PCA basis is fitted on a sample and applied in bounded chunks (indexing peak
  3.6 -> 1.3 GB at scale).
- **Ranking**: test files weigh half unless the query asks about tests (`test_factor`; recall@10 on the tuning
  set 0.86 -> 0.89).
- **Indexing**: the graph is published (`last_index`) as soon as it is linked, so a running server answers
  with keyword + graph ranking during a long first embedding; a run with no changes skips the vector scan.

### Added

- `bench/scale_bench.py` and the scale task set `bench/tasks/scale.yaml` (validation only); benchmark task
  files may point at a prebuilt multi-repository store (`corpus.config`).

## [0.2.0] - 2026-11-16

Performance iteration, measured with `bench/perf_bench.py` on the Notaire corpus (before -> after in
`bench/results/perf/`). Existing stores migrate in place on first open and keep their embeddings.

### Changed

- **Store schema v4** (5x smaller): integer-keyed derived edges without secondary indexes; containment kept
  as a relative `parent` column instead of edge rows; unresolved references stored as one compressed blob
  per file; contentless FTS5 updated incrementally (no full rebuild); long node text zlib-compressed;
  redundant `owner` column replaced by a `derived` flag and a partial index.
- **Vectors**: content-addressed by text hash (identical texts share one vector), int8-quantised and reduced
  to 128 dimensions with a PCA basis fitted once per model (`[embed] reduce`, 0 keeps model dimensions).
  The engine keeps them as float16 (half the memory, ~1 ms per query).
- **Indexing**: stat-first change detection (unchanged files are not read); a run with no changes skips
  link, FTS and embedding; full re-extract clears once instead of deleting per file; FTS tokens and content
  hashes are computed in the extraction workers; `git log` output is cached by HEAD; the embedding model
  loads only when there is something to embed.
- **Embedding**: texts are embedded shortest-first in small batches, and large jobs use one pool of worker
  processes for the whole stream (`[embed] workers`).
- **Query**: PageRank runs 20 iterations (identical rankings to 30); very common query terms are dropped
  from the FTS query using the index vocabulary (`max_df`), the two rarest always stay.
- A one-word query is treated as an identifier lookup; `path#Symbol` references resolve without the project
  prefix.
- **Ranking**: fusion re-tuned on the tuning tasks and confirmed on the held-out set (dense list weighted
  1.5x, RRF constant 60, 7 code anchors + 1 doc anchor); every parameter is overridable per profile under
  `[ranking.tuning]` (the former `[ranking] restart` key was read but never applied).
- **Search output** (MCP, `--also` on the CLI): the top `k` results in detail plus ten one-line "also
  relevant" entries; one call now carries the recall of the top 20 for about 1 KB more.
- **Memory**: the engine streams nodes, edges and vectors from SQLite in bounded chunks and keeps vectors as
  float16; peak server memory 788 -> 504 MB.
- `bench/agent_bench.py` stops at the first account usage-limit error and never overwrites earlier results
  with a failed batch.

### Added

- `bench/perf_bench.py` (size, index, load, memory, latency by stage, MCP cold start, quality) and a held-out
  task set `bench/tasks/notaire-holdout.yaml` that is never used for tuning.

## [0.1.0] - 2026-11-16

### Added

- Recursive scanner: honours `.gitignore` in git repositories, include/exclude globs, size limits, never
  indexes secrets (`.env`, keys).
- Extractors: tree-sitter code walker (Java, Kotlin, C#, TypeScript/TSX/JavaScript, Python, Go, Rust, plus a
  generic fallback for other grammars), Markdown, SQL, JSON/YAML/TOML/XML/properties, CSV/TSV, PDF (optional
  `pypdf`), plain text and binary assets. Plugin entry point group `repolens.extractors`.
- Linker: imports (Java FQN, TS path aliases from `tsconfig.json`, Python modules, Go packages), calls, type
  references, inheritance and method overrides, tests-to-subject, HTTP route matching (Spring, JAX-RS,
  FastAPI/Flask, NestJS, Express servers; fetch/axios/api helpers, MockMvc, API-test collections as callers),
  SQL tables and JPA/SQLAlchemy mappings, doc links and mentions, issue/ADR tags, git co-change.
- SQLite store (WAL, FTS5 BM25) with incremental re-index by content hash; embeddings cached by text hash.
- Embeddings: fastembed (ONNX, local) by default, dependency-free LSA fallback, or none.
- Engine: hybrid anchors (BM25 + dense + exact identifiers) and typed, hub-damped Personalized PageRank;
  node, neighbors, impact, weighted path, outline, snippet; `demote` globs and `scope` filters.
- Interfaces: `repolens` CLI, FastAPI HTTP API, MCP server (FastMCP), Cytoscape.js graph explorer, JSON and
  GraphML export.
- Benchmark: retrieval (grep vs hybrid vs graph) and agent (Claude Code with and without the MCP server)
  with an HTML/SVG report.
