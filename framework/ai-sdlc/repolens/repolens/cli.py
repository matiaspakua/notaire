"""`repolens` command line. Exit codes: 0 ok, 1 not found / no results, 2 usage or configuration error."""

from __future__ import annotations

import argparse
import json
import sys
import time
from pathlib import Path

from . import __version__

# Commands that must start fast and work before the dependencies are installed (the harness hook runs on
# every tool call): they import only the standard library. The rest import the engine lazily.
LIGHT = ("hook", "report", "setup", "doctor", "refresh")


def _parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(prog="repolens", description="Local code knowledge graph (index, search, serve).")
    p.add_argument("-c", "--config", type=Path,
                   help="repolens.toml (default: $REPOLENS_CONFIG, the nearest repolens.toml, else the repository)")
    p.add_argument("--db", type=Path, help="SQLite store (default: .repolens/graph.db or $REPOLENS_DB)")
    p.add_argument("--version", action="version", version=f"repolens {__version__}")
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("init", help="write a repolens.toml for one or more folders")
    s.add_argument("paths", nargs="+")
    s.add_argument("-o", "--output", type=Path, default=Path("repolens.toml"))

    s = sub.add_parser("index", help="index configured sources or the given folders (incremental)")
    s.add_argument("paths", nargs="*")
    s.add_argument("--name", help="project name when indexing a single folder")
    s.add_argument("--source", action="append", help="only re-index this configured source")
    s.add_argument("--full", action="store_true", help="re-extract every file")
    s.add_argument("--no-embed", action="store_true")
    s.add_argument("--embedder", choices=["auto", "fastembed", "lsa", "none"])
    s.add_argument("--no-wait", action="store_true", help="exit at once when another index run holds the store")
    s.add_argument("--quiet", action="store_true", help="print nothing (git hooks, background refresh)")
    s.add_argument("--json", action="store_true")

    s = sub.add_parser("setup", help="register repolens with this repository's agent harnesses (idempotent)")
    s.add_argument("--only", action="append", choices=["claude", "codex", "opencode", "cursor", "git", "gitignore"],
                   help="only these targets (default: all)")
    s.add_argument("--install", action="store_true", help="also install the dependencies into $REPOLENS_HOME/venv")
    s.add_argument("--index", action="store_true", help="also build the index now (in the background)")
    s.add_argument("--dry-run", action="store_true")

    sub.add_parser("doctor", help="check dependencies, config, store freshness, harness wiring and telemetry")

    s = sub.add_parser("report", help="usage efficiency report (adoption, hit rate, fallbacks, tokens, latency)")
    s.add_argument("--since", help="7d, 12h or an ISO date (default: everything)")
    s.add_argument("--json", action="store_true")
    s.add_argument("--md", type=Path, help="also write the report as Markdown")
    s.add_argument("--json-out", type=Path, help="also write the report as JSON")
    s.add_argument("--gate", action="store_true", help="one advisory line for the factory's evidence ledger; exit 0")

    s = sub.add_parser("hook", help="harness hook: record one tool call read from stdin (always exits 0)")
    s.add_argument("--harness", default="claude-code")

    s = sub.add_parser("refresh", help="incremental index of an existing store (git hooks)")
    s.add_argument("--background", action="store_true", help="detach and return at once")

    s = sub.add_parser("search", help="hybrid semantic + lexical + graph search")
    s.add_argument("query")
    s.add_argument("-k", type=int, default=10)
    s.add_argument("--kind", action="append", help="restrict to node kinds (file, class, method, section, route...)")
    s.add_argument("--project")
    s.add_argument("--scope", choices=["all", "code", "docs", "tests"], default="all")
    s.add_argument("--files", action="store_true", help="group results by file")
    s.add_argument("--also", type=int, default=0, metavar="N", help="list N more results, one line each")
    s.add_argument("--no-graph", action="store_true", help="disable the PageRank expansion")
    s.add_argument("--json", action="store_true")

    for name, helptext in (("node", "a node and its typed relations"), ("outline", "symbols of a file"),
                           ("snippet", "source text of a node")):
        s = sub.add_parser(name, help=helptext)
        s.add_argument("ref", help="node id, path, path:line or symbol name")
        s.add_argument("--json", action="store_true")
        if name == "snippet":
            s.add_argument("--lines", type=int, default=80)

    s = sub.add_parser("neighbors", help="expand the graph around a node")
    s.add_argument("ref")
    s.add_argument("--depth", type=int, default=1)
    s.add_argument("--kind", action="append", help="edge kinds to follow")
    s.add_argument("--direction", choices=["in", "out", "both"], default="both")
    s.add_argument("--json", action="store_true")

    s = sub.add_parser("impact", help="what depends on a node (reverse closure) and which tests cover it")
    s.add_argument("ref")
    s.add_argument("--depth", type=int, default=3)
    s.add_argument("--json", action="store_true")

    s = sub.add_parser("path", help="shortest relation path between two nodes")
    s.add_argument("a")
    s.add_argument("b")
    s.add_argument("--json", action="store_true")

    s = sub.add_parser("stats", help="store statistics")
    s.add_argument("--json", action="store_true")

    s = sub.add_parser("serve", help="HTTP API + graph UI")
    s.add_argument("--host", default="127.0.0.1")
    s.add_argument("--port", type=int, default=7474)

    s = sub.add_parser("mcp", help="MCP server (stdio by default)")
    s.add_argument("--transport", choices=["stdio", "sse", "streamable-http"], default="stdio")
    s.add_argument("--all-tools", action="store_true", help="also expose neighbors, stats and reindex")

    s = sub.add_parser("export", help="export the graph (json or graphml)")
    s.add_argument("--format", choices=["json", "graphml"], default="json")
    s.add_argument("-o", "--output", type=Path, required=True)
    return p


def main(argv: list[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    try:
        if args.cmd in LIGHT:
            return _light(args)
        return _dispatch(args)
    except (ValueError, FileNotFoundError, KeyError) as exc:
        print(f"repolens: {exc}", file=sys.stderr)
        return 2


def _light(args) -> int:
    if args.cmd == "hook":
        from .usage import hook_main

        return hook_main(harness=args.harness)
    if args.cmd == "report":
        return _report(args)
    if args.cmd == "setup":
        return _setup(args)
    if args.cmd == "doctor":
        from .setup import doctor

        code, lines = doctor()
        print("\n".join(lines))
        return code
    return _refresh(args)


def _dispatch(args) -> int:  # noqa: C901 - flat command table
    from . import render
    from .service import KnowledgeGraph

    if args.cmd == "init":
        return _init(args)
    kg = KnowledgeGraph.open(args.config, args.db)
    if args.cmd == "index":
        if args.embedder:
            kg.cfg.embedder = args.embedder
        only = kg.add_paths(args.paths, args.name) if args.paths else args.source
        if args.paths:
            kg.sources_from_store()
        quiet = args.json or args.quiet
        log = (lambda *_a, **_k: None) if quiet else (lambda msg: print(msg, file=sys.stderr))
        report = kg.index(only=only, full=args.full, embed=not args.no_embed, log=log, wait=not args.no_wait)
        if args.quiet:
            return 0
        if report.get("busy"):
            print("repolens: another index run holds this store; nothing done", file=sys.stderr)
            return 0
        print(json.dumps(report, indent=2) if args.json else _index_summary(report))
        return 0
    if args.cmd == "serve":
        from .api import run

        run(kg, args.host, args.port)
        return 0
    if args.cmd == "mcp":
        from .mcp_server import ALL_TOOLS, CORE_TOOLS, build

        build(kg, ALL_TOOLS if args.all_tools else CORE_TOOLS).run(transport=args.transport)
        return 0
    if args.cmd == "export":
        from .export import export

        n = export(kg.engine, args.format, args.output)
        print(f"wrote {n} nodes to {args.output}")
        return 0

    eng = kg.engine
    if args.cmd == "search":
        started = time.perf_counter()
        hits = eng.search(args.query, k=args.k + args.also, kinds=args.kind, project=args.project,
                          mode="files" if args.files else "nodes", graph=not args.no_graph, scope=args.scope)
        text = (json.dumps([h.to_dict() for h in hits], indent=2) if args.json
                else render.hits(hits, args.query, detailed=args.k))
        print(text)
        _record_cli(args, "search", args.query, text, len(hits), started)
        return 0 if hits else 1
    if args.cmd == "stats":
        s = eng.stats()
        print(json.dumps(s, indent=2) if args.json else render.stats(s))
        return 0
    started = time.perf_counter()
    result, text = {
        "node": lambda: (r := eng.node(args.ref), render.node(r)),
        "outline": lambda: (r := eng.outline(args.ref), render.outline(r)),
        "snippet": lambda: (r := eng.snippet(args.ref, getattr(args, "lines", 80)), render.snippet(r)),
        "neighbors": lambda: (r := eng.neighbors(args.ref, args.kind, args.direction, args.depth), render.neighbors(r)),
        "impact": lambda: (r := eng.impact(args.ref, args.depth), render.impact(r)),
        "path": lambda: (r := eng.path_between(args.a, args.b), render.path(r)),
    }[args.cmd]()
    printed = json.dumps(result, indent=2) if args.json else text
    print(printed)
    given = getattr(args, "ref", None) or f"{getattr(args, 'a', '')} {getattr(args, 'b', '')}".strip()
    _record_cli(args, args.cmd, str(given), printed, 1 if result else 0, started)
    return 0 if result else 1


def _record_cli(args, tool: str, given: str, text: str, results: int, started: float) -> None:
    """Append one CLI query to the usage log. Telemetry must not change the command's result."""
    from .paths import usage_log
    from .usage import files_in, record

    record(usage_log(_state_dir(args)), {
        "src": "cli", "client": "cli", "tool": tool, "input": (given or "")[:200],
        "ms": round((time.perf_counter() - started) * 1000, 1), "bytes": len(text.encode()),
        "tokens": len(text.encode()) // 4, "results": results, "files": files_in(text),
    })


def _report(args) -> int:
    from . import report as rep
    from .paths import usage_log

    state = _state_dir(args)
    data = rep.build(usage_log(state), args.since)
    rep.write(data, args.json_out, args.md)
    if args.gate:
        print(rep.gate_line(data))
        return 0
    print(json.dumps(data, indent=1) if args.json else rep.render(data))
    return 0


def _setup(args) -> int:
    from .paths import project_root
    from .setup import TARGETS, install_dependencies, setup

    if args.install and not args.dry_run and install_dependencies() != 0:
        print("repolens: dependency installation failed", file=sys.stderr)
        return 2
    changed = setup(args.only or list(TARGETS), dry_run=args.dry_run)
    root = project_root()
    verb = "would update" if args.dry_run else "updated"
    for target, files in changed.items():
        print(f"{target:<9} {verb + ': ' + ', '.join(files) if files else 'already set up'}")
    if args.index and not args.dry_run:
        _spawn_index(root)
        print(f"index     building in the background (log: {root / '.repolens' / 'refresh.log'})")
    print("next: repolens doctor; agents pick the server up on their next session")
    return 0


def _refresh(args) -> int:
    """Git hooks call this: refresh an existing store, never create one, never fail, never block."""
    from .paths import project_root

    root = project_root()
    state = _state_dir(args)
    if not any(state.glob("*.db")):
        return 0
    if args.background:
        _spawn_index(root)
        return 0
    return main(["index", "--no-wait", "--quiet"])


def _spawn_index(root: Path) -> None:
    import subprocess

    state = root / ".repolens"
    state.mkdir(parents=True, exist_ok=True)
    with open(state / "refresh.log", "a", encoding="utf-8") as log:
        subprocess.Popen([sys.executable, "-m", "repolens", "index", "--no-wait", "--quiet"], cwd=root,  # noqa: S603
                         stdout=log, stderr=log, stdin=subprocess.DEVNULL, start_new_session=True)


def _state_dir(args) -> Path:
    """The folder holding the store (and the usage log next to it), without loading the engine."""
    import tomllib

    from .paths import find_config, state_dir

    if args.db:
        return Path(args.db).resolve().parent
    config = args.config or find_config()
    if config:
        db = tomllib.loads(Path(config).read_text(encoding="utf-8")).get("index", {}).get("db")
        if db:
            path = Path(db).expanduser()
            return (path if path.is_absolute() else Path(config).resolve().parent / path).parent
    return state_dir()


def _init(args) -> int:
    if args.output.exists():
        print(f"repolens: {args.output} exists; not overwriting", file=sys.stderr)
        return 2
    base = args.output.resolve().parent
    blocks = []
    for raw in args.paths:
        path = Path(raw).expanduser().resolve()
        try:
            shown = path.relative_to(base).as_posix() or "."
        except ValueError:
            shown = path.as_posix()
        blocks.append(f'[[source]]\nname = "{path.name}"\npath = "{shown}"\n')
    args.output.write_text(
        '[index]\ndb = ".repolens/graph.db"\n\n' + "\n".join(blocks)
        + '\n[embed]\nbackend = "auto"   # fastembed | lsa | none | auto\n'
        + '\n[ranking]\ndemote = []      # e.g. ["deprecated/**", "**/archive/**"]\n',
        encoding="utf-8")
    print(f"wrote {args.output}; next: repolens -c {args.output} index")
    return 0


def _index_summary(report: dict) -> str:
    lines = []
    for name, src in report["sources"].items():
        lines.append(f"{name}: {src['files']} files, {src['extracted']} extracted, {src['removed']} removed,"
                     f" {src['errors']} errors")
    s, t = report["stats"], report["timings"]
    lines.append(f"graph: {s['files']} files, {s['nodes']} nodes, {s['edges']} edges, {s['embeddings']} embeddings "
                 f"({report['embed'].get('embedder')})")
    lines.append("time: " + ", ".join(f"{k} {v}" for k, v in t.items()))
    return "\n".join(lines)


if __name__ == "__main__":
    sys.exit(main())
