"""HTTP API (FastAPI) + the graph explorer UI at `/`. Binds to 127.0.0.1 by default."""

from __future__ import annotations

import json
import os
from pathlib import Path

from .service import KnowledgeGraph

UI = Path(__file__).parent / "ui" / "index.html"
BENCH_ENV = "REPOLENS_BENCH_RESULTS"
# The arms a benchmark result compares, as `bench/` writes them: an agent with and without the tools, or retrieval
# by grep against retrieval through the graph.
BENCH_KINDS = (("agent", {"baseline", "graph"}), ("retrieval", {"grep", "graph"}))


def bench_kind(summary) -> str:
    arms = set(summary) if isinstance(summary, dict) else set()
    return next((kind for kind, needed in BENCH_KINDS if needed <= arms), "other")


def bench_results(folder: Path | None = None) -> list[dict]:
    """The benchmark result files of `folder` (default: `$REPOLENS_BENCH_RESULTS`), each with what it measured.
    Nothing here names an adopter: the folder and its files are the adopter's."""
    folder = folder or (Path(os.environ[BENCH_ENV]) if os.environ.get(BENCH_ENV) else None)
    if folder is None or not folder.is_dir():
        return []
    out = []
    for path in sorted(folder.glob("*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, ValueError):
            continue
        if not isinstance(data, dict):
            continue
        summary = data.get("summary")
        out.append({"name": path.stem, "label": data.get("label", path.stem), "kind": bench_kind(summary),
                    "model": data.get("model"), "taskset": data.get("taskset"), "runs": data.get("repeat"),
                    "commit": data.get("commit"), "date": data.get("stamp") or data.get("updated"),
                    "summary": summary})
    return out


def create_app(kg: KnowledgeGraph, bench: Path | None = None):
    from fastapi import FastAPI, HTTPException, Query
    from fastapi.responses import HTMLResponse

    app = FastAPI(title="repolens", description="Code knowledge graph: hybrid search and graph navigation.")

    def found(result):
        if not result:
            raise HTTPException(status_code=404, detail="not found")
        return result

    @app.get("/", response_class=HTMLResponse, include_in_schema=False)
    def ui():
        return UI.read_text(encoding="utf-8")

    @app.get("/api/stats")
    def stats():
        return kg.engine.stats()

    @app.get("/api/search")
    def search(q: str, k: int = 10, scope: str = "all", files: bool = False, project: str | None = None,
               kind: list[str] | None = Query(None), graph: bool = True):
        hits = kg.engine.search(q, k=k, kinds=kind, project=project, scope=scope,
                                mode="files" if files else "nodes", graph=graph)
        return [h.to_dict() for h in hits]

    @app.get("/api/node")
    def node(ref: str):
        return found(kg.engine.node(ref))

    @app.get("/api/neighbors")
    def neighbors(ref: str, depth: int = 1, direction: str = "both", kind: list[str] | None = Query(None)):
        return found(kg.engine.neighbors(ref, kind, direction, min(depth, 3)))

    @app.get("/api/impact")
    def impact(ref: str, depth: int = 3):
        return found(kg.engine.impact(ref, min(depth, 6)))

    @app.get("/api/path")
    def path(a: str, b: str):
        return found(kg.engine.path_between(a, b))

    @app.get("/api/outline")
    def outline(path: str):
        return found(kg.engine.outline(path))

    @app.get("/api/snippet")
    def snippet(ref: str, lines: int = 80):
        return found(kg.engine.snippet(ref, min(lines, 400)))

    @app.get("/api/orphans")
    def orphan_list(symbols: bool = False, all: bool = False):  # noqa: A002 - the query parameter is `all`
        from . import orphans

        return orphans.find(kg.engine, symbols=symbols, include_demoted=all, include_conventional=all)

    @app.get("/api/subgraph")
    def subgraph(q: str | None = None, ref: str | None = None, k: int = 25, scope: str = "all", depth: int = 1):
        eng = kg.engine
        if q:
            hits = eng.search(q, k=k, scope=scope)
            timings = {stage: round(seconds * 1000, 2) for stage, seconds in eng.timings.items()}
            graph = eng.subgraph([h.id for h in hits], expand=0)
            graph["hits"] = [h.to_dict() for h in hits]
            graph["timings"] = timings
            graph["answer"] = agent_answer(q, scope)
            return graph
        if ref:
            result = found(eng.neighbors(ref, None, "both", min(depth, 2), limit=120))
            return eng.subgraph([n["id"] for n in result["nodes"]])
        raise HTTPException(status_code=400, detail="pass q or ref")

    def agent_answer(q: str, scope: str) -> dict:
        """What the MCP `search` tool returns for the same question, and its size: what an agent reads."""
        from . import render
        from .mcp_server import ALSO, DETAILED

        hits = kg.engine.search(q, k=DETAILED + ALSO, scope=scope, mode="files")
        text = render.hits(hits, q, detailed=DETAILED)
        size = len(text.encode("utf-8"))
        return {"text": text, "hits": len(hits), "bytes": size, "tokens": size // 4}

    @app.post("/api/index")
    def reindex(full: bool = False):
        return kg.index(full=full, log=lambda *_: None)

    # ── Metrics: what this store knows and how agents use it ──

    @app.get("/api/metrics")
    def metrics(since: str | None = None):
        """The served store's index size, orphan counts and usage report (from its own usage log). Benchmark
        results are optional: the JSON files of `serve --bench` (or `$REPOLENS_BENCH_RESULTS`)."""
        from . import orphans
        from . import report as usage_report
        from .paths import usage_log

        log = usage_log(Path(kg.cfg.db_path).parent)
        return {
            "index": kg.engine.stats(),
            "orphans": orphans.counts(kg.engine),
            "usage": usage_report.build(log, since),
            "bench": bench_results(bench),
        }

    @app.get("/api/metrics/usage")
    def usage_events(hours: int = 24):
        """Recent usage events from the usage log (last N hours)."""
        import time

        from . import usage as usage_mod
        from .paths import usage_log

        since = time.time() - (hours * 3600)
        events = usage_mod.read(usage_log(Path(kg.cfg.db_path).parent), since=since)
        return {
            "hours": hours,
            "total_events": len(events),
            "events": events[-500:],
        }

    return app


def run(kg: KnowledgeGraph, host: str = "127.0.0.1", port: int = 7474, bench: Path | None = None) -> None:
    import uvicorn

    kg.engine.warm()
    uvicorn.run(create_app(kg, bench), host=host, port=port, log_level="warning")
