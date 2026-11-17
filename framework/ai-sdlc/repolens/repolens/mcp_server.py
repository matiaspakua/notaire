"""MCP server (FastMCP from the official `mcp` SDK). Tools return compact text: path:line first."""

from __future__ import annotations

import threading

from . import render
from .service import KnowledgeGraph

INSTRUCTIONS = """repolens is a pre-built knowledge graph of the indexed folders: every file, class, method,
function, doc section, REST route, DB table, test and git co-change, linked by typed edges (calls, imports,
inherits/implements, tests, http_call, handles, uses_table, mentions, co_change).
Start with `search` (hybrid semantic + keyword + graph ranking) instead of grep/glob: one call returns the
relevant files with path:line and the relation that put them there. Then use `node` (relations of one
symbol), `impact` (what breaks / which tests cover a change), `path` (how two things connect),
`outline`/`snippet` (read code without opening whole files). Arguments accept a node id, a path,
path:line or a symbol name."""


ALSO = 10  # one-line results listed after the detailed top k
CORE_TOOLS = ("search", "node", "impact", "path", "outline", "snippet")
ALL_TOOLS = (*CORE_TOOLS, "neighbors", "stats", "reindex")


def build(kg: KnowledgeGraph, tools: tuple[str, ...] = CORE_TOOLS):
    """FastMCP server; only `tools` are registered (each schema costs the agent input tokens per turn)."""
    from mcp.server.fastmcp import FastMCP

    mcp = FastMCP("repolens", instructions=INSTRUCTIONS)
    unknown = set(tools) - set(ALL_TOOLS)
    if unknown:
        raise ValueError(f"unknown MCP tools: {sorted(unknown)}")
    threading.Thread(target=lambda: kg.engine.warm(), daemon=True).start()

    @_tool(mcp, tools)
    def search(query: str, k: int = 10, scope: str = "all", group_by_file: bool = True,
               kinds: list[str] | None = None, project: str | None = None) -> str:
        """Find code, tests, docs, configs, routes and tables relevant to a natural-language question or
        identifier. Ranks by semantic + keyword similarity, then expands through the code graph (calls,
        implementations, tests, HTTP routes, co-changed files) so related files that never mention the
        query words still surface. scope: all | code | docs | tests. Returns path:line per result: the top
        `k` in detail, then 10 more as one-line "also relevant" entries (often the rest of the answer)."""
        hits = kg.engine.search(query, k=k + ALSO, kinds=kinds, project=project, scope=scope,
                                mode="files" if group_by_file else "nodes")
        return render.hits(hits, query, detailed=k)

    @_tool(mcp, tools)
    def node(ref: str) -> str:
        """Describe one node (file, class, method, route, table, doc section...) and list its typed
        relations both ways: callers/callees, implementations, imports, tests, routes, tables, docs."""
        return render.node(kg.engine.node(ref))

    @_tool(mcp, tools)
    def impact(ref: str, depth: int = 3) -> str:
        """Reverse-dependency closure of a node: everything that calls, implements, imports, routes to or
        tests it (transitively up to `depth`), plus the list of affected test files."""
        return render.impact(kg.engine.impact(ref, depth))

    @_tool(mcp, tools)
    def neighbors(ref: str, edge_kinds: list[str] | None = None, direction: str = "both", depth: int = 1) -> str:
        """Expand the graph around a node. edge_kinds filters e.g. ["calls","inherits","tests","http_call",
        "handles","imports","references","co_change","uses_table","mentions"]. direction: in | out | both."""
        return render.neighbors(kg.engine.neighbors(ref, edge_kinds, direction, depth))

    @_tool(mcp, tools)
    def path(source: str, target: str) -> str:
        """Shortest chain of typed relations connecting two nodes (e.g. a UI component and a DB table)."""
        return render.path(kg.engine.path_between(source, target))

    @_tool(mcp, tools)
    def outline(file_path: str) -> str:
        """Symbols (or doc sections) of one file with line ranges and signatures."""
        return render.outline(kg.engine.outline(file_path))

    @_tool(mcp, tools)
    def snippet(ref: str, max_lines: int = 80) -> str:
        """Source text of a node (a method, class, section...) without reading the whole file."""
        return render.snippet(kg.engine.snippet(ref, max_lines))

    @_tool(mcp, tools)
    def stats() -> str:
        """Indexed projects and graph size by node/edge kind."""
        return render.stats(kg.engine.stats())

    @_tool(mcp, tools)
    def reindex() -> str:
        """Incrementally re-index the configured folders (only changed files are re-parsed)."""
        report = kg.index(log=lambda *_: None)
        return f"reindexed: {report['stats']} in {report['timings']['total_s']}s"

    return mcp


def _tool(mcp, enabled: tuple[str, ...]):
    def register(fn):
        return mcp.tool()(fn) if fn.__name__ in enabled else fn

    return register
