"""`repolens` command line. Exit codes: 0 ok, 1 not found / no results, 2 usage or configuration error."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from . import __version__, render
from .service import KnowledgeGraph


def _parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(prog="repolens", description="Local code knowledge graph (index, search, serve).")
    p.add_argument("-c", "--config", type=Path, help="repolens.toml (default: ./repolens.toml or $REPOLENS_CONFIG)")
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
    s.add_argument("--json", action="store_true")

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
        return _dispatch(args)
    except (ValueError, FileNotFoundError, KeyError) as exc:
        print(f"repolens: {exc}", file=sys.stderr)
        return 2


def _dispatch(args) -> int:  # noqa: C901 - flat command table
    if args.cmd == "init":
        return _init(args)
    kg = KnowledgeGraph.open(args.config, args.db)
    if args.cmd == "index":
        if args.embedder:
            kg.cfg.embedder = args.embedder
        only = kg.add_paths(args.paths, args.name) if args.paths else args.source
        if args.paths:
            kg.sources_from_store()
        log = (lambda *_a, **_k: None) if args.json else (lambda msg: print(msg, file=sys.stderr))
        report = kg.index(only=only, full=args.full, embed=not args.no_embed, log=log)
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
        hits = eng.search(args.query, k=args.k + args.also, kinds=args.kind, project=args.project,
                          mode="files" if args.files else "nodes", graph=not args.no_graph, scope=args.scope)
        print(json.dumps([h.to_dict() for h in hits], indent=2) if args.json
              else render.hits(hits, args.query, detailed=args.k))
        return 0 if hits else 1
    if args.cmd == "stats":
        s = eng.stats()
        print(json.dumps(s, indent=2) if args.json else render.stats(s))
        return 0
    result, text = {
        "node": lambda: (r := eng.node(args.ref), render.node(r)),
        "outline": lambda: (r := eng.outline(args.ref), render.outline(r)),
        "snippet": lambda: (r := eng.snippet(args.ref, getattr(args, "lines", 80)), render.snippet(r)),
        "neighbors": lambda: (r := eng.neighbors(args.ref, args.kind, args.direction, args.depth), render.neighbors(r)),
        "impact": lambda: (r := eng.impact(args.ref, args.depth), render.impact(r)),
        "path": lambda: (r := eng.path_between(args.a, args.b), render.path(r)),
    }[args.cmd]()
    print(json.dumps(result, indent=2) if args.json else text)
    return 0 if result else 1


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
