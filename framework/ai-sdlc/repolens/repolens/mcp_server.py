"""MCP server (FastMCP from the official `mcp` SDK). Tools return compact text: path:line first.

Every tool call (1) starts a throttled background refresh of the index, so answers follow the agent's own
edits, branch switches and pulls; (2) is recorded in the usage log (tool, latency, size, files returned,
calling client) for `repolens report`. A first start on an unindexed repository builds the index in the
background and says so instead of answering from an empty graph.
"""

from __future__ import annotations

import threading
import time

from . import render
from .paths import usage_log
from .service import KnowledgeGraph
from .usage import files_in, record

INSTRUCTIONS = """repolens is a knowledge graph of this repository (files, symbols, docs, routes, tables, tests,
git co-change) linked by typed edges and kept in sync with the working tree.
Start with `search` instead of grep/glob: one call returns the relevant files with path:line and why. Then
`node` (relations of one symbol), `impact` (what breaks, which tests cover it), `path` (how two things connect),
`outline`/`snippet` (code without whole files), `orphans` (files nothing relates to). A reference is a node id,
a path, path:line or a symbol name. Use grep for exact literal strings (log messages, config values)."""


ALSO = 4  # one-line results listed after the detailed top k
CORE_TOOLS = ("search", "node", "impact", "path", "outline", "snippet", "orphans")
ALL_TOOLS = (*CORE_TOOLS, "neighbors", "stats", "reindex")
BUILDING = ("repolens is building the index of this repository for the first time (graph and keyword search "
            "are ready in seconds to a few minutes; semantic vectors follow). Retry shortly, or use Grep/Glob "
            "meanwhile.")


class Tracker:
    """Wraps tool bodies: refresh the index when due, time the call, record it."""

    def __init__(self, kg: KnowledgeGraph, server):
        self.kg = kg
        self.server = server
        self.log = usage_log(kg.cfg.db_path.parent)
        self._client = ""

    def client(self) -> str:
        if not self._client:
            try:
                info = self.server.get_context().session.client_params.clientInfo
                self._client = f"{info.name}"[:40]
            except Exception:  # noqa: BLE001 - no live session (tests, direct calls)
                return "unknown"
        return self._client

    def refreshed(self, result: dict) -> None:
        record(self.log, {"src": "server", "tool": "refresh", **result})

    def call(self, tool: str, given: str, body) -> str:
        self.kg.maybe_refresh(on_done=self.refreshed)
        clock = time.perf_counter()
        if self.kg.refreshing and not self.kg.store.file_signatures_count():
            text, results = BUILDING, 0
        else:
            text, results = body()
        record(self.log, {"src": "server", "client": self.client(), "tool": tool, "input": given[:200],
                          "ms": round((time.perf_counter() - clock) * 1000, 1), "bytes": len(text.encode()),
                          "tokens": len(text.encode()) // 4, "results": results, "files": files_in(text)})
        return text


def build(kg: KnowledgeGraph, tools: tuple[str, ...] = CORE_TOOLS):
    """FastMCP server; only `tools` are registered (each schema costs the agent input tokens per turn)."""
    from mcp.server.fastmcp import FastMCP

    mcp = FastMCP("repolens", instructions=INSTRUCTIONS, log_level="WARNING")
    unknown = set(tools) - set(ALL_TOOLS)
    if unknown:
        raise ValueError(f"unknown MCP tools: {sorted(unknown)}")
    track = Tracker(kg, mcp)
    if kg.cfg.sources and not kg.store.file_signatures_count():
        kg.maybe_refresh(on_done=track.refreshed, force=True)  # first start: build the index in the background
    threading.Thread(target=lambda: kg.engine.warm(), daemon=True).start()

    @_tool(mcp, tools)
    def search(query: str, k: int = 4, scope: str = "all", group_by_file: bool = True,
               kinds: list[str] | None = None, project: str | None = None, repos: str | None = None) -> str:
        """Find code, tests, docs, configs, routes and tables relevant to a natural-language question or
        identifier. Ranks by semantic + keyword similarity, then expands through the code graph (calls,
        implementations, tests, HTTP routes, co-changed files) so related files that never mention the
        query words still surface. scope: all | code | docs | tests. Returns path:line per result: the top
        `k` in detail (default 4), then 4 more as one-line "also relevant" entries."""
        def body():
            mode = "files" if group_by_file else "nodes"
            if repos:
                from . import repos as federation

                pairs = federation.search(kg, query, k + ALSO, repos, mode, scope)
                hits = [federation.labelled(name, hit) for name, hit in pairs]
            else:
                hits = kg.engine.search(query, k=k + ALSO, kinds=kinds, project=project, scope=scope, mode=mode)
            return render.hits(hits, query, detailed=k), len(hits)
        return track.call("search", query, body)

    @_tool(mcp, tools)
    def node(ref: str) -> str:
        """Describe one node (file, class, method, route, table, doc section...) and list its typed
        relations both ways: callers/callees, implementations, imports, tests, routes, tables, docs."""
        return track.call("node", ref, lambda: _found(kg, render.node(kg.engine.node(ref)), ref))

    @_tool(mcp, tools)
    def impact(ref: str, depth: int = 2) -> str:
        """Reverse-dependency closure of a node: everything that calls, implements, imports, routes to or
        tests it (transitively up to `depth`), plus the list of affected test files."""
        return track.call("impact", ref, lambda: _found(kg, render.impact(kg.engine.impact(ref, depth)), ref))

    @_tool(mcp, tools)
    def neighbors(ref: str, edge_kinds: list[str] | None = None, direction: str = "both", depth: int = 1) -> str:
        """Expand the graph around a node. edge_kinds filters e.g. ["calls","inherits","tests","http_call",
        "handles","imports","references","co_change","uses_table","mentions"]. direction: in | out | both."""
        return track.call("neighbors", ref, lambda: _found(
            kg, render.neighbors(kg.engine.neighbors(ref, edge_kinds, direction, depth)), ref))

    @_tool(mcp, tools)
    def path(source: str, target: str) -> str:
        """Shortest chain of typed relations connecting two nodes (e.g. a UI component and a DB table)."""
        return track.call("path", f"{source} -> {target}", lambda: _found(
            kg, render.path(kg.engine.path_between(source, target)), source, target))

    @_tool(mcp, tools)
    def outline(file_path: str) -> str:
        """Symbols (or doc sections) of one file with line ranges and signatures."""
        return track.call("outline", file_path, lambda: _found(
            kg, render.outline(kg.engine.outline(file_path)), file_path))

    @_tool(mcp, tools)
    def snippet(ref: str, max_lines: int = 32) -> str:
        """Source text of a node (a method, class, section...) without reading the whole file."""
        return track.call("snippet", ref, lambda: _found(kg, render.snippet(kg.engine.snippet(ref, max_lines)), ref))

    @_tool(mcp, tools)
    def orphans(symbols: bool = False, limit: int = 50) -> str:
        """Files that relate to no other file (review: unused, misplaced or missing a link), then files related
        only through git history; `symbols` adds symbols with no relation."""
        from . import orphans as orphan_report

        def body():
            found = orphan_report.find(kg.engine, symbols=symbols)
            return render.orphans(found, limit), len(found["isolated"]) + len(found["history_only"])
        return track.call("orphans", "", body)

    @_tool(mcp, tools)
    def stats() -> str:
        """Indexed projects and graph size by node/edge kind."""
        return track.call("stats", "", lambda: _counted(render.stats(kg.engine.stats())))

    @_tool(mcp, tools)
    def reindex() -> str:
        """Incrementally re-index the configured folders (only changed files are re-parsed)."""
        def body():
            report = kg.index(log=lambda *_: None)
            return f"reindexed: {report['stats']} in {report['timings']['total_s']}s", 1
        return track.call("reindex", "", body)

    return mcp


def _found(kg: KnowledgeGraph, text: str, *refs: str) -> tuple[str, int]:
    """A lookup's answer; on a miss, what was not found and the closest real symbols (one call, not two)."""
    if text in ("node not found", "file not found"):
        return kg.engine.missing(*refs), 0
    return _counted(text)


def _counted(text: str) -> tuple[str, int]:
    return text, 0 if not text or "not found" in text[:40] or text.startswith("no ") else 1


def _tool(mcp, enabled: tuple[str, ...]):
    """Register a tool with plain-text results only: a structured copy would send every answer twice and add
    an output schema to the tool list each session pays for."""
    def register(fn):
        if fn.__name__ not in enabled:
            return fn
        try:
            return mcp.tool(structured_output=False)(fn)
        except TypeError:  # mcp < 1.10 has no structured output
            return mcp.tool()(fn)

    return register
