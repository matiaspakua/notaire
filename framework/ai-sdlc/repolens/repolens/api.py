"""HTTP API (FastAPI) + the graph explorer UI at `/`. Binds to 127.0.0.1 by default."""

from __future__ import annotations

import json
from pathlib import Path

from .service import KnowledgeGraph

UI = Path(__file__).parent / "ui" / "index.html"

# Bench results directory — sibling of repolens/repolens/
_BENCH_RESULTS = Path(__file__).resolve().parent.parent / "bench" / "results"


def create_app(kg: KnowledgeGraph):
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
            graph = eng.subgraph([h.id for h in hits], expand=0)
            graph["hits"] = [h.to_dict() for h in hits]
            return graph
        if ref:
            result = found(eng.neighbors(ref, None, "both", min(depth, 2), limit=120))
            return eng.subgraph([n["id"] for n in result["nodes"]])
        raise HTTPException(status_code=400, detail="pass q or ref")

    @app.post("/api/index")
    def reindex(full: bool = False):
        return kg.index(full=full, log=lambda *_: None)

    # ── Metrics associations ──

    def _load_json(path: Path):
        if not path.exists():
            return None
        return json.loads(path.read_text(encoding="utf-8"))

    @app.get("/api/metrics")
    def metrics():
        """Aggregate RepoLens benchmark metrics: retrieval, agent, and factory-loop comparisons."""
        retrieval = _load_json(_BENCH_RESULTS / "retrieval.json")
        holdout = _load_json(_BENCH_RESULTS / "retrieval-notaire-holdout.json")
        agent_haiku = _load_json(_BENCH_RESULTS / "agent-haiku.json")
        agent_haiku_holdout = _load_json(_BENCH_RESULTS / "agent-haiku-notaire-holdout.json")
        agent_sonnet = _load_json(_BENCH_RESULTS / "agent-sonnet-holdout-0.3.0.json")
        agent_sonnet_scale = _load_json(_BENCH_RESULTS / "agent-sonnet-scale.json")
        factory_kpis = _load_json(_BENCH_RESULTS / "factory-kpis.json")
        headline = _load_json(_BENCH_RESULTS / "headline.json")
        cli_comparison = _load_json(_BENCH_RESULTS / "cli-comparison.json")

        def _summary():
            summaries = {}
            for label, data in [
                ("haiku", agent_haiku),
                ("haiku-holdout", agent_haiku_holdout),
                ("sonnet", agent_sonnet),
                ("sonnet-scale", agent_sonnet_scale),
            ]:
                if not data:
                    continue
                # agent-*.json uses "summary" with baseline/graph keys
                s = data.get("summary", {})
                if not isinstance(s, dict) or "baseline" not in s or "graph" not in s:
                    continue
                baseline = s["baseline"]
                graph = s["graph"]
                summaries[label] = {
                    "baseline": baseline,
                    "graph": graph,
                    "improvement": {
                        "tool_calls_ratio": round(
                            baseline.get("tool_calls", 0) / max(graph.get("tool_calls", 1), 0.01), 2
                        ),
                        "input_tokens_ratio": round(
                            baseline.get("input_tokens", 0) / max(graph.get("input_tokens", 1), 0.01), 2
                        ),
                        "cost_ratio": round(
                            baseline.get("cost_usd", 0) / max(graph.get("cost_usd", 0.0001), 0.0001), 2
                        ),
                        "duration_ratio": round(
                            baseline.get("duration_s", 0) / max(graph.get("duration_s", 0.01), 0.01), 2
                        ),
                    },
                }
            return summaries

        def _retrieval_summary(data):
            if not data:
                return None
            # retrieval.json uses "summary" with arm-level keys like "recall@10"
            summary = data.get("summary", {})
            if not summary:
                return None
            grep_arm = summary.get("grep", {})
            hybrid_arm = summary.get("hybrid", {})
            graph_arm = summary.get("graph", {})
            return {
                "tasks": data.get("tasks", 0),
                "grep_recall_at_10": grep_arm.get("recall@10"),
                "hybrid_recall_at_10": hybrid_arm.get("recall@10"),
                "graph_recall_at_10": graph_arm.get("recall@10"),
                "grep_latency_ms": grep_arm.get("latency_ms"),
                "hybrid_latency_ms": hybrid_arm.get("latency_ms"),
                "graph_latency_ms": graph_arm.get("latency_ms"),
                "grep_bytes": grep_arm.get("output_bytes"),
                "hybrid_bytes": hybrid_arm.get("output_bytes"),
                "graph_bytes": graph_arm.get("output_bytes"),
            }

        return {
            "headlines": headline.get("captions", []) if headline else [],
            "retrieval": {
                "tuning": _retrieval_summary(retrieval),
                "holdout": _retrieval_summary(holdout),
            },
            "agents": _summary(),
            "factory_kpis": factory_kpis,
            "cli_comparison": cli_comparison,
            "index_stats": kg.engine.stats(),
        }

    @app.get("/api/metrics/usage")
    def usage_events(hours: int = 24):
        """Recent usage events from the usage log (last N hours)."""
        from . import usage as usage_mod
        from .paths import state_dir, usage_log
        import time

        since = time.time() - (hours * 3600)
        root = Path(kg.engine.root) if hasattr(kg.engine, "root") else Path.cwd()
        log = usage_log(state_dir(root))
        events = usage_mod.read(log, since=since)
        return {
            "hours": hours,
            "total_events": len(events),
            "events": events[-500:],
        }

    return app


def run(kg: KnowledgeGraph, host: str = "127.0.0.1", port: int = 7474) -> None:
    import uvicorn

    kg.engine.warm()
    uvicorn.run(create_app(kg), host=host, port=port, log_level="warning")
