#!/usr/bin/env python3
"""Render benchmark results as a self-contained HTML report, standalone SVG charts and a Markdown summary.

    python3 bench/report.py [--results bench/results]

Reads retrieval.json and every agent-<model>.json in the results folder. No third-party dependencies.
Colours follow the entity everywhere: grep/baseline = slot 1, hybrid = slot 2, graph = slot 3.
"""

from __future__ import annotations

import argparse
import html
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))

STYLE = """
.viz-root { --surface:#fcfcfb; --ink:#0b0b0b; --ink2:#52514e; --grid:#e4e3df;
  --s1:#2a78d6; --s2:#eb6834; --s3:#1baf7a; }
@media (prefers-color-scheme: dark) { :root:where(:not([data-theme="light"])) .viz-root {
  --surface:#1a1a19; --ink:#f0efec; --ink2:#c3c2b7; --grid:#353431; --s1:#3987e5; --s2:#d95926; --s3:#199e70; } }
:root[data-theme="dark"] .viz-root { --surface:#1a1a19; --ink:#f0efec; --ink2:#c3c2b7; --grid:#353431;
  --s1:#3987e5; --s2:#d95926; --s3:#199e70; }
.viz-root text { font: 11px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; fill: var(--ink2); }
.viz-root .title { font-size: 13px; font-weight: 600; fill: var(--ink); }
.viz-root .value { fill: var(--ink); font-weight: 600; }
.viz-root .axis { stroke: var(--grid); stroke-width: 1; }
.viz-root .grep, .viz-root .baseline { fill: var(--s1); }
.viz-root .hybrid { fill: var(--s2); }
.viz-root .graph { fill: var(--s3); }
.viz-root .version { fill: var(--ink2); }
.viz-root rect.hit { fill: transparent; }
.viz-root .bar:hover { opacity: .8; }
"""

SHORT = {"grep": "grep", "hybrid": "hybrid", "graph": "graph", "baseline": "baseline", "graph_agent": "+ repolens"}
ARM_LABEL = {"grep": "grep (keyword ripgrep)", "hybrid": "repolens hybrid (no graph)", "graph": "repolens graph",
             "baseline": "baseline: Read/Grep/Glob", "graph_agent": "+ repolens MCP"}


def esc(text) -> str:
    return html.escape(str(text))


def _bar_path(x: float, y: float, w: float, h: float, r: float = 4.0, horizontal: bool = False) -> str:
    """A bar with 4px rounded data-end, square at the baseline."""
    if h <= 0 or w <= 0:
        return ""
    if horizontal:
        r = min(r, w, h / 2)
        return (f"M{x},{y} H{x + w - r} Q{x + w},{y} {x + w},{y + r} V{y + h - r} "
                f"Q{x + w},{y + h} {x + w - r},{y + h} H{x} Z")
    r = min(r, h, w / 2)
    return (f"M{x},{y + h} V{y + r} Q{x},{y} {x + r},{y} H{x + w - r} Q{x + w},{y} {x + w},{y + r} "
            f"V{y + h} Z")


def svg_wrap(body: str, width: int, height: int, label: str, standalone: bool) -> str:
    style = f"<style>{STYLE}</style>" if standalone else ""
    return (f'<svg class="viz-root" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" '
            f'width="{width}" height="{height}" role="img" aria-label="{esc(label)}">{style}'
            f'<rect width="{width}" height="{height}" fill="var(--surface)" rx="8"/>{body}</svg>')


def column_chart(title: str, bars: list[tuple[str, str, float]], fmt, standalone=False, note: str = "") -> str:
    """Small-multiple column chart: one metric, one bar per arm. bars = [(css class, label, value)]."""
    width, height, top, bottom, left = 250, 210, 62 if note else 46, 30, 14
    plot_h = height - top - bottom
    vmax = max((v for _, _, v in bars), default=1) or 1
    slot = (width - 2 * left) / len(bars)
    bw = min(46, slot - 14)
    parts = [f'<text class="title" x="{left}" y="20">{esc(title)}</text>']
    if note:
        parts.append(f'<text x="{left}" y="35">{esc(note)}</text>')
    base = top + plot_h
    parts.append(f'<line class="axis" x1="{left}" x2="{width - left}" y1="{base}" y2="{base}"/>')
    for k, (cls, label, value) in enumerate(bars):
        h = plot_h * value / vmax
        x = left + slot * k + (slot - bw) / 2
        y = base - h
        parts.append(f'<g class="bar"><title>{esc(label)}: {esc(fmt(value))}</title>'
                     f'<path class="{cls}" d="{_bar_path(x, y, bw, h)}"/>'
                     f'<rect class="hit" x="{x - 6}" y="{top}" width="{bw + 12}" height="{plot_h}"/></g>')
        parts.append(f'<text class="value" x="{x + bw / 2}" y="{y - 6}" text-anchor="middle">{esc(fmt(value))}</text>')
        short = SHORT.get(cls if cls != "graph" or "MCP" not in label else "graph_agent", label)
        parts.append(f'<text x="{x + bw / 2}" y="{base + 16}" text-anchor="middle">{esc(short)}</text>')
    return svg_wrap("".join(parts), width, height, title, standalone)


def grouped_rows(title: str, rows: list[str], series: list[tuple[str, str]], values: dict, fmt,
                 standalone=False, vmax: float | None = None) -> str:
    """Horizontal grouped bars: one row per task, one bar per arm (series = [(css class, label)])."""
    label_w, width, bar_h, gap, top = 190, 760, 9, 2, 54
    row_h = len(series) * (bar_h + gap) + 12
    height = top + row_h * len(rows) + 12
    plot_w = width - label_w - 70
    vmax = vmax or max((values[r][s] for r in rows for s, _ in series), default=1) or 1
    parts = [f'<text class="title" x="14" y="22">{esc(title)}</text>']
    lx = 14
    for cls, label in series:
        parts.append(f'<path class="{cls}" d="{_bar_path(lx, 33, 12, 10, 2, True)}"/>'
                     f'<text x="{lx + 17}" y="42">{esc(label)}</text>')
        lx += 30 + 7 * len(label)
    parts.append(f'<line class="axis" x1="{label_w}" x2="{label_w}" y1="{top - 4}" y2="{height - 8}"/>')
    for i, row in enumerate(rows):
        y0 = top + i * row_h
        parts.append(f'<text x="{label_w - 8}" y="{y0 + row_h / 2}" text-anchor="end">{esc(row)}</text>')
        for j, (cls, label) in enumerate(series):
            v = values[row][series[j][0]]
            w = plot_w * v / vmax
            y = y0 + j * (bar_h + gap)
            parts.append(f'<g class="bar"><title>{esc(row)} - {esc(label)}: {esc(fmt(v))}</title>'
                         f'<path class="{cls}" d="{_bar_path(label_w, y, w, bar_h, 4, True)}"/>'
                         f'<rect class="hit" x="{label_w}" y="{y - 1}" width="{plot_w}" height="{bar_h + 2}"/></g>'
                         f'<text x="{label_w + w + 5}" y="{y + bar_h - 1}">{esc(fmt(v))}</text>')
    return svg_wrap("".join(parts), width, height, title, standalone)


def pct(new: float, old: float) -> str:
    if not old:
        return "n/a"
    change = (new - old) / old * 100
    return f"{change:+.0f}%"


def table(headers: list[str], rows: list[list]) -> str:
    head = "".join(f"<th>{esc(h)}</th>" for h in headers)
    body = "".join("<tr>" + "".join(f"<td>{esc(c)}</td>" for c in r) + "</tr>" for r in rows)
    return f"<table><thead><tr>{head}</tr></thead><tbody>{body}</tbody></table>"


def md_table(headers: list[str], rows: list[list]) -> str:
    lines = ["| " + " | ".join(headers) + " |", "|" + "---|" * len(headers)]
    lines += ["| " + " | ".join(str(c) for c in r) + " |" for r in rows]
    return "\n".join(lines)


def _chart_tag(data: dict, *, model: str | None = None) -> str:
    """Filename fragment. Set chart_tag in the result JSON to keep a historical name."""
    if "chart_tag" in data:
        return str(data["chart_tag"])
    taskset = str(data.get("taskset") or "")
    if model:
        return f"{model}-{taskset}" if taskset else model
    return f"-{taskset}" if taskset else ""


def _taskset_name(data: dict) -> str:
    return str(data.get("taskset") or "tasks")


def retrieval_section(data: dict, out: Path) -> tuple[str, str, list[str]]:
    s = data["summary"]
    tag = _chart_tag(data)
    arms = ["grep", "hybrid", "graph"]
    metrics = [("recall@10", "Recall@10 (higher is better)", lambda v: f"{v:.2f}"),
               ("mrr", "MRR (higher is better)", lambda v: f"{v:.2f}"),
               ("latency_ms", "Latency per query, ms", lambda v: f"{v:,.0f}"),
               ("output_bytes", "Output to read, KB", lambda v: f"{v / 1024:,.1f}")]
    charts, files = [], []
    for key, title, fmt in metrics:
        svg = column_chart(title, [(a, ARM_LABEL[a], s[a][key]) for a in arms], fmt)
        charts.append(svg)
        name = f"retrieval{tag}-{key.replace('@', '')}.svg"
        (out / name).write_text(column_chart(title, [(a, ARM_LABEL[a], s[a][key]) for a in arms], fmt, True))
        files.append(name)
    tasks = list(dict.fromkeys(r["task"] for r in data["rows"]))
    values = {t: {r["arm"]: r["recall@10"] for r in data["rows"] if r["task"] == t} for t in tasks}
    per_task = grouped_rows("Recall@10 per task", tasks, [(a, ARM_LABEL[a]) for a in arms], values,
                            lambda v: f"{v:.2f}", vmax=1.0)
    (out / f"retrieval{tag}-per-task.svg").write_text(grouped_rows(
        "Recall@10 per task", tasks, [(a, ARM_LABEL[a]) for a in arms], values, lambda v: f"{v:.2f}", True, 1.0))
    files.append(f"retrieval{tag}-per-task.svg")
    rows = [[ARM_LABEL[a], f"{s[a]['recall@5']:.2f}", f"{s[a]['recall@10']:.2f}",
             f"{s[a].get('recall@20', 0):.2f}", f"{s[a]['mrr']:.2f}", f"{s[a]['latency_ms']:,.0f}",
             f"{s[a]['output_bytes'] / 1024:,.1f}",
             f"{s[a].get('output_bytes_tail', 0) / 1024:,.1f}" if a != "grep" else "n/a"] for a in arms]
    headers = ["arm", "recall@5", "recall@10", "recall@20", "MRR", "latency ms", "output KB (top 10)",
               "output KB (top 10 + 10 one-line)"]
    cats = sorted({r["category"] for r in data["rows"]})
    cat_rows = []
    for c in cats:
        sel = [r for r in data["rows"] if r["category"] == c]
        cat_rows.append([c] + [f"{_mean([r['recall@10'] for r in sel if r['arm'] == a]):.2f}" for a in arms])
    html_part = (
        f"<h2>Retrieval, task set <code>{esc(_taskset_name(data))}</code>: same question, "
        f"top-{data['k']} files</h2>"
        "<p>Each task prompt is sent verbatim to three retrievers. <b>grep</b> runs one ripgrep per prompt keyword "
        "and ranks files by TF-IDF of the matches; <b>hybrid</b> is repolens's BM25 + embedding anchors without the "
        "graph walk; <b>graph</b> adds the typed, hub-damped Personalized PageRank. Recall counts the expected "
        "files found in the top 10; recall@20 is what one MCP <code>search</code> call carries (10 detailed "
        "results plus 10 one-line entries).</p>"
        f'<div class="grid">{"".join(charts)}</div>{per_task}'
        f"<h3>Table</h3>{table(headers, rows)}<h3>Recall@10 by task category</h3>"
        f"{table(['category', *[ARM_LABEL[a] for a in arms]], cat_rows)}")
    md = (f"### Retrieval, task set `{_taskset_name(data)}` (top-{data['k']} files, "
          f"{data['tasks']} tasks, commit {data['commit']})\n\n"
          + md_table(headers, rows) + "\n\n" + md_table(["category", *[ARM_LABEL[a] for a in arms]], cat_rows))
    return html_part, md, files


def _mean(values: list[float]) -> float:
    return sum(values) / len(values) if values else 0.0


def agent_section(data: dict, out: Path) -> tuple[str, str, list[str]]:
    from agent_bench import summarize

    s, model = summarize(data["rows"]), data["model"]
    tag = _chart_tag(data, model=model)
    arms = [a for a in ("baseline", "graph") if a in s]
    if not arms:
        return "", "", []
    label = {"baseline": ARM_LABEL["baseline"], "graph": ARM_LABEL["graph_agent"]}
    metrics = [("recall", "Answer recall", lambda v: f"{v:.2f}"),
               ("tool_calls", "Tool calls per task", lambda v: f"{v:.1f}"),
               ("turns", "Turns per task", lambda v: f"{v:.1f}"),
               ("duration_s", "Time per task, s", lambda v: f"{v:.1f}"),
               ("input_tokens", "Input tokens per task", lambda v: f"{v / 1000:.1f}k"),
               ("cost_usd", "Cost per task, USD", lambda v: f"${v:.4f}" if v < 0.01 else f"${v:.3f}")]
    charts, files = [], []
    for key, title, fmt in metrics:
        note = f"graph vs baseline: {pct(s['graph'][key], s['baseline'][key])}" if len(arms) == 2 else ""
        bars = [(a, label[a], s[a][key]) for a in arms]
        charts.append(column_chart(title, bars, fmt, note=note))
        name = f"agent-{tag}-{key}.svg"
        (out / name).write_text(column_chart(title, bars, fmt, True, note))
        files.append(name)
    tasks = list(dict.fromkeys(r["task"] for r in data["rows"]))
    values = {}
    for t in tasks:
        values[t] = {a: _mean([r["duration_s"] for r in data["rows"] if r["task"] == t and r["arm"] == a
                               and not r["is_error"]]) for a in arms}
    per_task = grouped_rows(f"Time per task, s ({tag})", tasks, [(a, label[a]) for a in arms], values,
                            lambda v: f"{v:.1f}")
    (out / f"agent-{tag}-per-task.svg").write_text(grouped_rows(
        f"Time per task, s ({tag})", tasks, [(a, label[a]) for a in arms], values, lambda v: f"{v:.1f}", True))
    files.append(f"agent-{tag}-per-task.svg")
    headers = ["arm", "runs", "recall", "tool calls", "turns", "time s", "input tokens", "output tokens", "cost USD"]
    rows = [[label[a], s[a]["runs"], f"{s[a]['recall']:.2f}", f"{s[a]['tool_calls']:.2f}", f"{s[a]['turns']:.2f}",
             f"{s[a]['duration_s']:.1f}", f"{s[a]['input_tokens']:,.0f}", f"{s[a]['output_tokens']:,.0f}",
             f"{s[a]['cost_usd']:.5f}"] for a in arms]
    if len(arms) == 2:
        rows.append(["change", "", pct(s["graph"]["recall"], s["baseline"]["recall"]),
                     *[pct(s["graph"][k], s["baseline"][k]) for k in
                       ("tool_calls", "turns", "duration_s", "input_tokens", "output_tokens", "cost_usd")]])
    task_rows = []
    for t in tasks:
        cells = [t]
        for a in arms:
            sel = [r for r in data["rows"] if r["task"] == t and r["arm"] == a and not r["is_error"]]
            cells += [f"{_mean([r['recall'] for r in sel]):.2f}", f"{_mean([r['tool_calls'] for r in sel]):.1f}",
                      f"{_mean([r['duration_s'] for r in sel]):.1f}"]
        used = [r for r in data["rows"] if r["task"] == t and r["arm"] == "graph"]
        cells.append(f"{sum(1 for r in used if r['graph_calls'])}/{len(used)}")
        task_rows.append(cells)
    task_headers = ["task", "base recall", "base tools", "base s", "graph recall", "graph tools", "graph s",
                    "graph used"]
    total = sum(r["cost_usd"] for r in data["rows"])
    html_part = (
        f"<h2>Agent: headless, model <code>{esc(model)}</code>, task set "
        f"<code>{esc(_taskset_name(data))}</code></h2>"
        f"<p>{len(data['rows'])} runs ({data['repeat']} repetition(s) per task and arm, total cost "
        f"${total:.2f}). Both arms get the same prompt, the same model and the same built-in tools (Read, Grep, "
        "Glob); the graph arm also has the repolens MCP server. No system prompt is added to either arm. Recall is "
        "scored from the <code>FILES:</code> line of the final answer.</p>"
        f'<div class="grid">{"".join(charts)}</div>{per_task}<h3>Table</h3>{table(headers, rows)}'
        f"<h3>Per task (means)</h3>{table(task_headers, task_rows)}")
    md = (f"### Agent: {model}, task set `{_taskset_name(data)}` ({len(data['rows'])} runs, {data['repeat']} rep., "
          f"${total:.2f} total)\n\n"
          + md_table(headers, rows))
    return html_part, md, files


PERF_METRICS = [
    ("db_mb", "Store size, MB", lambda v: f"{v:,.0f}"),
    ("rss_mb", "Server memory, MB", lambda v: f"{v:,.0f}"),
    ("index_noop_s", "Re-index, no changes, s", lambda v: f"{v:.2f}"),
    ("index_one_file_s", "Re-index, one file changed, s", lambda v: f"{v:.1f}"),
    ("index_full_extract_s", "Full re-extract, s", lambda v: f"{v:.1f}"),
    ("embed_cold_min", "First embedding pass, min", lambda v: f"{v:.1f}"),
    ("p50_ms", "Query latency p50, ms", lambda v: f"{v:.0f}"),
    ("p95_ms", "Query latency p95, ms", lambda v: f"{v:.0f}"),
]


def perf_section(before: dict, after: dict, out: Path) -> tuple[str, str]:
    """Before/after of the performance iteration; one neutral series, identity is on the x axis."""
    from perf_bench import _flat

    a, b = {**_flat(before), **before.get("extra", {})}, {**_flat(after), **after.get("extra", {})}
    charts, rows = [], []
    for key, title, fmt in PERF_METRICS:
        if a.get(key) is None or b.get(key) is None:
            continue
        bars = [("version", before["label"], a[key]), ("version", after["label"], b[key])]
        note = f"{a[key] / b[key]:.1f}x lower" if b[key] and a[key] > b[key] else ""
        charts.append(column_chart(title, bars, fmt, note=note))
        (out / f"perf-{key}.svg").write_text(column_chart(title, bars, fmt, True, note))
        rows.append([title, fmt(a[key]), fmt(b[key]), note])
    quality = [[name, f"{a[k1]:.2f} / {a[k2]:.2f}", f"{b[k1]:.2f} / {b[k2]:.2f}", ""] for name, k1, k2 in (
        ("Tuning set recall@10 / MRR", "tune_R@10", "tune_MRR"),
        ("Held-out set recall@10 / MRR", "hold_R@10", "hold_MRR"))]
    headers = ["metric", before["label"], after["label"], "change"]
    html_part = (
        "<h2>Performance iteration (0.1.0 to 0.2.0)</h2>"
        "<p>The same corpus measured with <code>bench/perf_bench.py</code> "
        f"before (<b>{esc(before['label'])}</b>) and after (<b>{esc(after['label'])}</b>) the storage, indexing "
        "and query optimisations. Quality is retrieval recall@10 / MRR on the tuning tasks and on a held-out "
        "task set that was never used for tuning.</p>"
        f'<div class="grid">{"".join(charts)}</div>{table(headers, rows + quality)}')
    md = "### Performance iteration\n\n" + md_table(headers, rows + quality)
    return html_part, md


# Concrete colours (no CSS variables): the headline image must render the same in browsers, IDE previews and
# rasterisers such as rsvg. Same hues as STYLE; validated with the dataviz palette checker in both modes.
HEADLINE_STYLE = """
.bg { fill: #fcfcfb; } text { font: 12px -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  fill: #52514e; } .title { font-size: 13px; font-weight: 600; fill: #0b0b0b; } .value { font-weight: 600;
  fill: #0b0b0b; } .axis { stroke: #e4e3df; stroke-width: 1; } .grep { fill: #2a78d6; } .graph { fill: #1baf7a; }
@media (prefers-color-scheme: dark) { .bg { fill: #1a1a19; } text { fill: #c3c2b7; } .title, .value { fill: #f0efec; }
  .axis { stroke: #353431; } .grep { fill: #3987e5; } .graph { fill: #199e70; } }
"""


def headline_chart(out: Path) -> str | None:
    """One picture: navigating with grep versus with repolens, from the benchmark results (no typed numbers).
    Each metric row has its own scale; the factor on the right says better/worse in plain words.
    Which result files and which caption to draw come from results/headline.json."""
    manifest_path = out / "headline.json"
    if not manifest_path.is_file():
        return None
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    tune, hold = out / manifest["tuning"], out / manifest["holdout"]
    agent_name = manifest.get("agent")
    agent = out / agent_name if agent_name else None
    if not (tune.exists() and hold.exists()):
        return None
    t, h = json.loads(tune.read_text())["summary"], json.loads(hold.read_text())["summary"]
    rows: list[tuple] = [
        ("Accuracy — higher is better", None),
        ("Right files in top 10, tuning tasks", t["grep"]["recall@10"], t["graph"]["recall@10"], "{:.2f}",
         True, "more"),
        ("Right files in top 10, held-out tasks", h["grep"]["recall@10"], h["graph"]["recall@10"], "{:.2f}",
         True, "more"),
        ("Rank of first right file (MRR), held-out", h["grep"]["mrr"], h["graph"]["mrr"], "{:.2f}", True, "higher"),
        ("Speed and size per query — lower is better", None),
        ("Latency (ms)", t["grep"]["latency_ms"], t["graph"]["latency_ms"], "{:,.0f}", False, "faster"),
        ("Text returned to the model (KB)", t["grep"]["output_bytes"] / 1024, t["graph"]["output_bytes"] / 1024,
         "{:,.1f}", False, "less"),
    ]
    if agent is not None and agent.exists():
        a = json.loads(agent.read_text())["summary"]
        b, g = a["baseline"], a["graph"]
        title = manifest.get("agent_title", "Agent, held-out tasks ({runs} sessions)").format(
            runs=b["runs"] + g["runs"], baseline=b["runs"], graph=g["runs"],
        )
        rows += [
            (title, None),
            ("Answer accuracy — higher is better", b["recall"], g["recall"], "{:.2f}", True, "more"),
            ("Tool calls per task — lower is better", b["tool_calls"], g["tool_calls"], "{:.1f}", False, "fewer"),
            ("Seconds per task — lower is better", b["duration_s"], g["duration_s"], "{:.1f}", False, "faster"),
        ]
    width, left, bar_x, bar_w, bar_h, gap = 760, 20, 300, 290, 11, 2
    y = 110
    parts = [
        f'<text class="title" x="{left}" y="28" style="font-size:16px">repolens vs grep-driven navigation</text>',
        f'<text x="{left}" y="50">{esc(manifest["captions"][0])}</text>',
        f'<text x="{left}" y="66">{esc(manifest["captions"][1])}</text>',
        f'<rect class="grep" x="{left}" y="78" width="12" height="11" rx="2"/>'
        f'<text x="{left + 18}" y="88">without repolens (grep / Read / Glob)</text>',
        f'<rect class="graph" x="{left + 270}" y="78" width="12" height="11" rx="2"/>'
        f'<text x="{left + 288}" y="88">with repolens</text>',
    ]
    for row in rows:
        if row[1] is None:
            y += 12
            parts.append(f'<text class="title" x="{left}" y="{y}">{esc(row[0])}</text>')
            parts.append(f'<line class="axis" x1="{left}" x2="{width - left}" y1="{y + 6}" y2="{y + 6}"/>')
            y += 20
            continue
        label, before, after, fmt, higher_better, word = row
        vmax = max(before, after) or 1
        parts.append(f'<text x="{left}" y="{y + bar_h + 4}">{esc(label)}</text>')
        for k, (cls, value, who) in enumerate((("grep", before, "without repolens"),
                                                ("graph", after, "with repolens"))):
            by = y + k * (bar_h + gap)
            w = max(2.0, bar_w * value / vmax)
            text = fmt.format(value)
            parts.append(f'<g class="bar"><title>{esc(label)}, {who}: {esc(text)}</title>'
                         f'<path class="{cls}" d="{_bar_path(bar_x, by, w, bar_h, horizontal=True)}"/></g>'
                         f'<text class="value" x="{bar_x + w + 6}" y="{by + bar_h - 1}">{esc(text)}</text>')
        parts.append(f'<text class="value" x="{width - left}" y="{y + bar_h + 4}" text-anchor="end">'
                     f'{esc(_factor(before, after, higher_better, word))}</text>')
        y += 2 * bar_h + gap + 18
    parts.append(f'<text x="{left}" y="{y + 8}">Data: bench/results/retrieval*.json, agent-*.json. Cost per task '
                 f'and every other measure: README, Benchmark.</text>')
    height = int(y + 24)
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" width="{width}" '
           f'height="{height}" role="img" aria-label="repolens vs grep-driven navigation">'
           f'<style>{HEADLINE_STYLE}</style><rect class="bg" width="{width}" height="{height}" rx="8"/>'
           f'{"".join(parts)}</svg>')
    (out / "charts" / "headline.svg").write_text(svg, encoding="utf-8")
    return svg


def _factor(before: float, after: float, higher_better: bool, word: str) -> str:
    """'4.9x more', '43x faster', '-50%', 'same': the size of the difference in plain words."""
    if before == after:
        return "same"
    if not before or not after:
        return ""
    better = after > before if higher_better else after < before
    ratio = max(before, after) / min(before, after)
    if ratio >= 2:
        shown = f"{ratio:.0f}x" if ratio >= 10 else f"{ratio:.1f}x"
        return f"{shown} {word}" if better else f"{shown} worse"
    return f"{(after - before) / before:+.0%}"


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--results", type=Path, default=HERE / "results")
    ap.add_argument("--perf", nargs=2, metavar=("BEFORE", "AFTER"), default=["v0-baseline", "final"],
                    help="labels of two perf_bench runs to compare")
    ap.add_argument("--perf-names", nargs=2, default=["0.1.0", "0.2.0"], help="display names of those runs")
    args = ap.parse_args()
    out = args.results
    charts_dir = out / "charts"
    charts_dir.mkdir(parents=True, exist_ok=True)
    sections, md_parts = [], []
    headline = headline_chart(out)
    if headline:
        sections.append(f'<section><h2>At a glance</h2>{headline}</section>')
        md_parts.append("![repolens vs grep-driven navigation](charts/headline.svg)\n")
    for retrieval in sorted(out.glob("retrieval*.json")):
        h, m, _ = retrieval_section(json.loads(retrieval.read_text()), charts_dir)
        sections.append(h)
        md_parts.append(m)
    for path in sorted(out.glob("agent-*.json")):
        h, m, _ = agent_section(json.loads(path.read_text()), charts_dir)
        if h:
            sections.append(h)
            md_parts.append(m)
    perf = [out / "perf" / f"{label}.json" for label in args.perf]
    if all(p.exists() for p in perf):
        runs = [{**json.loads(p.read_text()), "label": name} for p, name in zip(perf, args.perf_names, strict=True)]
        h, m = perf_section(runs[0], runs[1], charts_dir)
        sections.append(h)
        md_parts.append(m)
    page = f"""<!doctype html><html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>repolens benchmark</title>
<style>{STYLE}
body {{ margin: 0; padding: 24px 16px 48px; background: #f4f4f2; color: #1d1d1f;
  font: 14px/1.5 -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }}
@media (prefers-color-scheme: dark) {{ body {{ background: #121211; color: #ecebe8; }} }}
main {{ max-width: 1080px; margin: 0 auto; }}
h1 {{ font-size: 22px; letter-spacing: -.01em; }} h2 {{ margin-top: 36px; font-size: 18px; }}
.grid {{ display: flex; flex-wrap: wrap; gap: 12px; margin: 12px 0; }}
svg {{ max-width: 100%; height: auto; }}
table {{ border-collapse: collapse; margin: 8px 0 16px; font-size: 13px; }}
th, td {{ border-bottom: 1px solid #8884; padding: 4px 10px; text-align: right; }}
th:first-child, td:first-child {{ text-align: left; }}
code {{ font-size: 12px; }}
</style></head><body><main>
<h1>repolens benchmark: grep-driven harness vs. code knowledge graph</h1>
<p>Corpus: a clean clone of the repository HEAD (the benchmark and its answer key are not in it).
Regenerate with <code>bench/run_all.sh</code>.</p>
{''.join(sections)}
</main></body></html>"""
    (out / "report.html").write_text(page, encoding="utf-8")
    (out / "summary.md").write_text("\n\n".join(md_parts) + "\n", encoding="utf-8")
    print(f"wrote {out / 'report.html'}, {out / 'summary.md'} and charts/*.svg")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
