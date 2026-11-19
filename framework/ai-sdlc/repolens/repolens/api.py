"""HTTP API (FastAPI) + the graph explorer UI at `/`. Binds to 127.0.0.1 by default."""

from __future__ import annotations

from pathlib import Path

from .service import KnowledgeGraph

UI = Path(__file__).parent / "ui" / "index.html"


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

    return app


def run(kg: KnowledgeGraph, host: str = "127.0.0.1", port: int = 7474) -> None:
    import uvicorn

    kg.engine.warm()
    uvicorn.run(create_app(kg), host=host, port=port, log_level="warning")
