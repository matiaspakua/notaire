"""`repolens report`: how efficiently agents use repolens, from the usage log. Standard library only.

Measured (not estimated):
  adoption        share of agent sessions that called repolens at least once
  first tool      share of sessions whose first navigation call was repolens instead of grep/glob/read
  hit rate        of the files an agent read after a repolens call, the share repolens had already returned
                  in that session: the direct measure of retrieval quality in real work
  fallback        share of repolens calls followed by a grep/glob before the next repolens call
  tokens          what navigation tools returned to the model (bytes / 4), per session and per tool
  latency, zero-result queries, index refreshes (server side, any harness)

Sessions come from harness hook events and from CLI events that carry a session id.
Server events without a session still count as calls, not as adoption.
Comparisons between sessions with and without repolens are observational, not controlled: the benchmark in
bench/ is the controlled measurement.
"""

from __future__ import annotations

import json
import re
import statistics
import time
from collections import Counter, defaultdict
from pathlib import Path

from .usage import read

NAVIGATION = ("grep", "glob", "read")


def since_ts(value: str | None) -> float:
    """'7d', '12h', an ISO date, or nothing (everything)."""
    if not value:
        return 0.0
    m = re.fullmatch(r"(\d+)([dh])", value.strip())
    if m:
        return time.time() - int(m.group(1)) * (86400 if m.group(2) == "d" else 3600)
    return time.mktime(time.strptime(value.strip()[:10], "%Y-%m-%d"))


def _pct(values: list[float], q: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    return round(ordered[min(len(ordered) - 1, int(q * len(ordered)))], 1)


def server_metrics(events: list[dict]) -> dict:
    calls = [e for e in events if e.get("src") in ("server", "cli") and e.get("tool") != "refresh"]
    refresh = [e for e in events if e.get("src") == "server" and e.get("tool") == "refresh"]
    searches = [e for e in calls if e.get("tool") == "search"]
    zero = [e for e in searches if not e.get("results")]
    by_tool: dict[str, dict] = {}
    for tool, group in sorted(_group(calls, "tool").items()):
        ms = [float(e.get("ms", 0)) for e in group]
        by_tool[tool] = {"calls": len(group), "p50_ms": _pct(ms, 0.5), "p95_ms": _pct(ms, 0.95),
                         "avg_tokens": round(statistics.mean(e.get("tokens", 0) for e in group))}
    ms_all = [float(e.get("ms", 0)) for e in calls]
    return {
        "calls": len(calls), "clients": dict(Counter(e.get("client") or "unknown" for e in calls)),
        "by_tool": by_tool, "p50_ms": _pct(ms_all, 0.5), "p95_ms": _pct(ms_all, 0.95),
        "tokens_served": sum(e.get("tokens", 0) for e in calls),
        "zero_result_rate": round(len(zero) / len(searches), 3) if searches else 0.0,
        "zero_result_queries": [q for q, _ in Counter(e.get("input", "") for e in zero).most_common(10)],
        "refreshes": len(refresh), "refresh_busy": sum(1 for e in refresh if e.get("busy")),
        "refresh_errors": sum(1 for e in refresh if e.get("error")),
        "refresh_p95_ms": _pct([float(e.get("ms", 0)) for e in refresh if not e.get("busy")], 0.95),
    }


def _repolens_call(event: dict) -> bool:
    tool = str(event.get("tool") or "")
    if tool.startswith("repolens:"):
        return True
    return event.get("src") == "cli" and tool not in ("", "miss", "refresh")


def session_metrics(events: list[dict]) -> dict:
    sessions: dict[str, list[dict]] = defaultdict(list)
    for e in events:
        if not e.get("session"):
            continue
        if e.get("src") == "harness" or e.get("src") == "cli":
            sessions[e["session"]].append(e)
    if not sessions:
        return {"sessions": 0}
    adopted, first_repolens, hits, reads_after, fallbacks, rl_calls = 0, 0, 0, 0, 0, 0
    tokens_with: list[int] = []
    tokens_without: list[int] = []
    calls_with: list[int] = []
    calls_without: list[int] = []
    tool_tokens: Counter = Counter()
    for evs in sessions.values():
        evs.sort(key=lambda e: e.get("ts", 0))
        returned: set[str] = set()
        seen_repolens = False
        waiting_fallback = False
        for e in evs:
            tool = e.get("tool", "")
            tool_tokens["repolens" if _repolens_call(e) else tool] += e.get("bytes", 0) // 4
            if _repolens_call(e):
                if waiting_fallback:
                    waiting_fallback = False
                seen_repolens = True
                rl_calls += 1
                waiting_fallback = True
                returned.update(e.get("files") or [])
            elif tool in ("grep", "glob", "miss"):
                if waiting_fallback:
                    fallbacks += 1
                    waiting_fallback = False
            elif tool == "read" and seen_repolens:
                path = (e.get("files") or [""])[0]
                reads_after += 1
                hits += any(path == f or path.endswith("/" + f) or f.endswith("/" + path) for f in returned)
        first = next(iter(evs), {})
        first_repolens += _repolens_call(first)
        total_tokens = sum(e.get("bytes", 0) for e in evs) // 4
        if seen_repolens:
            adopted += 1
            tokens_with.append(total_tokens)
            calls_with.append(len(evs))
        else:
            tokens_without.append(total_tokens)
            calls_without.append(len(evs))
    n = len(sessions)
    return {
        "sessions": n, "adoption": round(adopted / n, 3), "first_tool_repolens": round(first_repolens / n, 3),
        "hit_rate": round(hits / reads_after, 3) if reads_after else None, "reads_after_repolens": reads_after,
        "fallback_rate": round(fallbacks / rl_calls, 3) if rl_calls else None, "repolens_calls": rl_calls,
        "navigation_tokens_by_tool": dict(tool_tokens),
        "with_repolens": _summary(calls_with, tokens_with), "without_repolens": _summary(calls_without, tokens_without),
    }


def _summary(calls: list[int], tokens: list[int]) -> dict:
    if not calls:
        return {"sessions": 0}
    return {"sessions": len(calls), "navigation_calls": round(statistics.mean(calls), 1),
            "navigation_tokens": round(statistics.mean(tokens))}


def recommendations(server: dict, sessions: dict) -> list[dict]:
    out: list[dict] = []

    def add(level: str, finding: str, action: str) -> None:
        out.append({"level": level, "finding": finding, "action": action})

    if not server.get("calls") and not sessions.get("sessions"):
        add("info", "no usage recorded yet", "run `repolens setup` and `repolens doctor`; agents must start from the "
            "repository so the MCP server and the hook find `.repolens/`")
        return out
    n = sessions.get("sessions", 0)
    if n >= 3 and sessions["adoption"] < 0.5:
        add("high", f"only {sessions['adoption']:.0%} of agent sessions called repolens",
            "check the harness registration (`repolens doctor`) and the AGENTS.md pointer; agents that never see "
            "the tool fall back to grep loops")
    if n >= 3 and sessions.get("first_tool_repolens", 1) < 0.5 and sessions.get("adoption", 0) >= 0.5:
        share = sessions["first_tool_repolens"]
        add("medium", f"repolens is the first navigation call in only {share:.0%} of sessions",
            "agents explore with grep first and use repolens late: make `search` the documented first step")
    hit = sessions.get("hit_rate")
    if hit is not None and sessions.get("reads_after_repolens", 0) >= 10 and hit < 0.5:
        add("high", f"hit rate {hit:.0%}: agents mostly read files repolens did not return",
            "add the missed questions to bench/tasks/ and re-tune; check `demote` globs and edge weights in "
            "repolens.toml")
    fallback = sessions.get("fallback_rate")
    if fallback is not None and sessions.get("repolens_calls", 0) >= 10 and fallback > 0.5:
        add("medium", f"{fallback:.0%} of repolens calls are followed by grep/glob",
            "agents do not trust or cannot use the answer: review those queries (zero results, wrong project, "
            "stale index)")
    if server.get("zero_result_rate", 0) > 0.1:
        add("medium", f"{server['zero_result_rate']:.0%} of searches returned nothing",
            "queries: " + "; ".join(server["zero_result_queries"][:5]))
    if server.get("p95_ms", 0) > 1500:
        add("medium", f"p95 latency {server['p95_ms']:.0f} ms", "large repository or frequent reloads: check "
            "`repolens stats`, raise REPOLENS_REFRESH_SECONDS, or split the profile")
    if server.get("refresh_errors"):
        add("high", f"{server['refresh_errors']} index refreshes failed", "run `repolens index` and read the error")
    if not out:
        add("ok", "no problem found", "keep collecting; compare reports over time")
    return out


def build(log: Path, since: str | None = None) -> dict:
    events = read(log, since_ts(since))
    server = server_metrics(events)
    sessions = session_metrics(events)
    return {"log": str(log), "since": since or "all", "events": len(events), "server": server,
            "sessions": sessions, "recommendations": recommendations(server, sessions)}


def render(rep: dict) -> str:
    s, h = rep["server"], rep["sessions"]
    lines = [f"repolens usage report ({rep['events']} events since {rep['since']})", ""]
    if s["calls"]:
        clients = ", ".join(f"{k} {v}" for k, v in s["clients"].items())
        lines += [f"server: {s['calls']} tool calls ({clients}); latency p50 {s['p50_ms']:.0f} ms, p95 "
                  f"{s['p95_ms']:.0f} ms; {s['tokens_served']:,} tokens served; zero-result searches "
                  f"{s['zero_result_rate']:.0%}; index refreshes {s['refreshes']} (busy {s['refresh_busy']}, "
                  f"errors {s['refresh_errors']})"]
        lines += [f"  {tool:<10} {v['calls']:>5} calls  p50 {v['p50_ms']:>6.0f} ms  p95 {v['p95_ms']:>6.0f} ms  "
                  f"~{v['avg_tokens']} tokens/call" for tool, v in s["by_tool"].items()]
    if h.get("sessions"):
        hit = "n/a" if h["hit_rate"] is None else f"{h['hit_rate']:.0%} of {h['reads_after_repolens']} reads"
        fb = "n/a" if h["fallback_rate"] is None else f"{h['fallback_rate']:.0%}"
        lines += ["", f"agent sessions: {h['sessions']}; adoption {h['adoption']:.0%}; repolens first "
                  f"{h['first_tool_repolens']:.0%}; hit rate {hit}; grep fallback {fb}"]
        for label in ("with_repolens", "without_repolens"):
            g = h[label]
            if g["sessions"]:
                lines.append(f"  {label.replace('_', ' ')}: {g['sessions']} sessions, {g['navigation_calls']} "
                             f"navigation calls, ~{g['navigation_tokens']:,} navigation tokens per session")
        tokens = ", ".join(f"{k} {v:,}" for k, v in sorted(h["navigation_tokens_by_tool"].items()))
        lines.append(f"  navigation tokens by tool: {tokens}")
    lines += ["", "recommendations:"]
    lines += [f"  [{r['level']}] {r['finding']} -> {r['action']}" for r in rep["recommendations"]]
    return "\n".join(lines)


def markdown(rep: dict) -> str:
    return "# repolens usage report\n\n```text\n" + render(rep) + "\n```\n"


def gate_line(rep: dict) -> str:
    """One line for the factory's evidence ledger (advisory: the gate never fails on usage numbers)."""
    s, h = rep["server"], rep["sessions"]
    worst = next((r for r in rep["recommendations"] if r["level"] == "high"), rep["recommendations"][0])
    hit = h.get("hit_rate")
    sessions = h.get("sessions", 0)
    # CLI events with no session id are calls, not adoption. Zero sessions is not 0%.
    adoption = "n/a" if not sessions else f"{h.get('adoption', 0):.0%}"
    return (f"repolens usage: {s['calls']} calls, {sessions} sessions, adoption "
            f"{adoption}, hit rate {'n/a' if hit is None else f'{hit:.0%}'}, p95 {s['p95_ms']:.0f} "
            f"ms; {worst['level']}: {worst['finding']}")


def write(rep: dict, json_path: Path | None = None, md_path: Path | None = None) -> None:
    if json_path:
        json_path.write_text(json.dumps(rep, indent=1), encoding="utf-8")
    if md_path:
        md_path.write_text(markdown(rep), encoding="utf-8")


def _group(events: list[dict], key: str) -> dict[str, list[dict]]:
    out: dict[str, list[dict]] = defaultdict(list)
    for e in events:
        out[str(e.get(key, ""))].append(e)
    return out
