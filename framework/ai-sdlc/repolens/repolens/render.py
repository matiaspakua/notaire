"""Compact plain-text renderers: what an AI agent reads (few tokens, every line actionable: path:line)."""

from __future__ import annotations

from .engine import Hit


def loc(path: str, start: int, end: int = 0) -> str:
    if not path:
        return ""
    if start and end and end > start:
        return f"{path}:{start}-{end}"
    return f"{path}:{start}" if start else path


def hits(results: list[Hit], query: str = "", detailed: int | None = None) -> str:
    """Ranked hits. The first `detailed` (all by default) carry symbols and the relation that surfaced them;
    the rest are one line each: recall keeps rising past the top 10, detail is what costs tokens."""
    if not results:
        return f"no results for {query!r}" if query else "no results"
    detailed = len(results) if detailed is None else detailed
    lines = []
    for n, h in enumerate(results, 1):
        where = loc(h.path, h.start, h.end) or h.name
        label = h.qualname if h.kind not in ("file", "section", "chunk") else h.name
        if n > detailed:
            if n == detailed + 1:
                lines.append("also relevant:")
            hint = h.symbols[0]["name"] if h.symbols else (label if h.kind != "file" else "")
            place = h.path if h.kind == "file" and h.path else (loc(h.path, h.start) or h.name)
            lines.append(f"{n}. {place}" + (f"  {hint[:48]}" if hint and hint not in where else ""))
            continue
        head = f"{n}. [{h.kind}] {where}"
        if h.kind not in ("file",) and label and label not in where:
            head += f"  {label}"
        head += f"  ({h.score:.2f})"
        lines.append(head)
        if h.signature and h.kind not in ("section", "chunk", "route", "table", "tag"):
            lines.append(f"   sig: {h.signature[:160]}")
        if h.symbols:
            lines.append("   symbols: " + ", ".join(f"{s['name']}:{s['line']}" for s in h.symbols))
        if h.why and h.why != "match":
            lines.append(f"   via: {h.why}")
    return "\n".join(lines)


def node(info: dict | None) -> str:
    if not info:
        return "node not found"
    lines = [f"[{info['kind']}] {info['name']}  {loc(info['path'], info['line'], info['end'])}", f"id: {info['id']}"]
    if info.get("signature"):
        lines.append(f"sig: {info['signature']}")
    if info.get("text"):
        lines.append(f"text: {info['text'][:400]}")
    for _title, bucket, arrow in (("outgoing", info["out"], "->"), ("incoming", info["in"], "<-")):
        for kind, items in sorted(bucket.items()):
            if kind == "dir_contains":
                continue
            shown = ", ".join(_short(i) for i in items[:12])
            more = f" (+{len(items) - 12})" if len(items) > 12 else ""
            lines.append(f"{arrow} {kind}: {shown}{more}")
    return "\n".join(lines)


def _short(item: dict) -> str:
    where = loc(item["path"], item["line"])
    if item["kind"] in ("file",):
        return where
    if item["kind"] in ("route", "table", "tag", "external", "dir"):
        return f"{item['kind']}:{item['name']}"
    return f"{item['name']} ({where})" if where else item["name"]


def neighbors(result: dict | None) -> str:
    if not result:
        return "node not found"
    by_id = {n["id"]: n for n in result["nodes"]}
    lines = [f"root: {result['root']}"]
    for src, kind, arrow, dst in result["edges"]:
        a, b = by_id.get(src), by_id.get(dst)
        if a and b:
            left, right = ("-", "->") if arrow == "->" else ("<-", "-")
            lines.append(f"{_short(a)} {left}{kind}{right} {_short(b)}")
    return "\n".join(lines)


def impact(result: dict | None) -> str:
    if not result:
        return "node not found"
    lines = [f"impact of {result['root']}: {len(result['files'])} files, {len(result['tests'])} tests"]
    for a in result["affected"]:
        if a["kind"] == "file" and a["via"] == "contains":
            continue
        lines.append(f"  d{a['depth']} {a['via']:<12} {_short(a)}")
    if result["tests"]:
        lines.append("tests: " + ", ".join(result["tests"][:25]))
    return "\n".join(lines)


def path(result: dict | None) -> str:
    if not result:
        return "node not found"
    if not result["found"]:
        return f"no path between {result['from']} and {result['to']}"
    lines = []
    for hop in result["hops"]:
        lines.append(f"{_short(hop['from'])} {hop['dir']} {hop['edge']} {hop['dir']} {_short(hop['to'])}")
    return "\n".join(lines)


def outline(result: dict | None) -> str:
    if not result:
        return "file not found"
    lines = [result["file"]["path"]]
    for item in result["items"]:
        sig = f"  {item['signature'][:120]}" if item["signature"] else ""
        lines.append(f"{'  ' * item['depth']}{item['line']}-{item['end']} {item['kind']} {item['name']}{sig}")
    return "\n".join(lines)


def snippet(result: dict | None) -> str:
    if not result:
        return "node not found"
    head = f"{loc(result['path'], result['from'], result['to'])} [{result['kind']}] {result['name']}"
    if result["truncated"]:
        head += " (truncated)"
    return head + "\n" + result["code"]


def stats(s: dict) -> str:
    lines = [f"files {s['files']}, nodes {s['nodes']}, edges {s['edges']}, embeddings {s['embeddings']} "
             f"({s.get('embedder')}), db {s['db_bytes'] / 1e6:.0f} MB"]
    for p in s["projects"]:
        lines.append(f"project {p['name']}: {p['root']} ({p['files']} files)")
    lines.append("nodes: " + ", ".join(f"{k} {v}" for k, v in s["nodes_by_kind"].items()))
    lines.append("edges: " + ", ".join(f"{k} {v}" for k, v in s["edges_by_kind"].items()))
    return "\n".join(lines)
