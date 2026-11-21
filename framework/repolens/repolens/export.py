"""Export the whole graph as JSON (nodes + edges) or GraphML (Gephi, yEd, Cytoscape desktop, networkx)."""

from __future__ import annotations

import json
from pathlib import Path
from xml.sax.saxutils import quoteattr


def export(engine, fmt: str, output: Path) -> int:
    columns = (engine.ids, engine.name, engine.qual)
    for column in columns:  # one scan per column instead of one lookup per node
        column.cache = dict(enumerate(column))
    try:
        ids = engine.ids.cache
        nodes = [engine.brief(i) for i in range(len(ids))]
        edges = []
        for i in range(len(ids)):
            for pos in range(engine.out_ptr[i], engine.out_ptr[i + 1]):
                edges.append((ids[i], ids[int(engine.out_nbr[pos])], engine.kind_names[engine.out_kind[pos]]))
    finally:
        for column in columns:
            column.cache = {}
    output.parent.mkdir(parents=True, exist_ok=True)
    if fmt == "json":
        output.write_text(json.dumps({
            "nodes": nodes, "edges": [{"source": s, "target": d, "kind": k} for s, d, k in edges]}), encoding="utf-8")
        return len(nodes)
    with open(output, "w", encoding="utf-8") as fh:
        fh.write('<?xml version="1.0" encoding="UTF-8"?>\n<graphml xmlns="http://graphml.graphdrawing.org/xmlns">\n')
        for key, target in (("kind", "node"), ("name", "node"), ("path", "node"), ("line", "node"), ("ekind", "edge")):
            fh.write(f'<key id="{key}" for="{target}" attr.name="{key.lstrip("e")}" attr.type="string"/>\n')
        fh.write('<graph edgedefault="directed">\n')
        for n in nodes:
            fh.write(f'<node id={quoteattr(n["id"])}><data key="kind">{n["kind"]}</data>'
                     f'<data key="name">{_x(n["name"])}</data><data key="path">{_x(n["path"])}</data>'
                     f'<data key="line">{n["line"]}</data></node>\n')
        for s, d, k in edges:
            fh.write(f'<edge source={quoteattr(s)} target={quoteattr(d)}><data key="ekind">{k}</data></edge>\n')
        fh.write("</graph>\n</graphml>\n")
    return len(nodes)


def _x(text: str) -> str:
    return (text or "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
