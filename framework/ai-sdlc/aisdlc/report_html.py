"""Static HTML monitor for evidence report snapshots (#2142).

Read-only presentation of summarize() JSON (+ optional lead-time fixture).
Never renders prompts, diffs, or secret-shaped keys.
"""
from __future__ import annotations

import html
from pathlib import Path
from typing import Any

from aisdlc.metrics import load_lead_time_fixture
from aisdlc.telemetry import redact_attributes

_BANNED_SUBSTRINGS = ("prompt", "diff", "secret", "token", "password", "api_key", "authorization")


def _esc(value: Any) -> str:
    return html.escape(str(value), quote=True)


def _safe_text(value: Any) -> str:
    text = str(value)
    lowered = text.lower()
    if any(b in lowered for b in _BANNED_SUBSTRINGS):
        return ""
    return _esc(text)


def render_monitor(data: dict[str, Any], lead_time: dict[str, Any] | None = None) -> str:
    """Build a self-contained HTML document from a report payload (no secret fields)."""
    profile = _safe_text(data.get("profile") or "aisdlc")
    runs = data.get("runs", 0)
    failed = data.get("failed_runs", 0)
    gates = data.get("gates") or []
    stages = data.get("stages") or []
    cost = data.get("cost") or {}
    pair = data.get("lead_time_event_pair") or {}
    autonomy = data.get("autonomy") or {}

    gate_rows = []
    for g in gates:
        rate = g.get("pass_rate")
        rate_s = "—" if rate is None else f"{rate}%"
        median = g.get("median_seconds")
        median_s = "—" if median is None else f"{median}"
        gate_rows.append(
            "<tr>"
            f"<td>{_safe_text(g.get('stage'))}</td>"
            f"<td>{_safe_text(g.get('gate'))}</td>"
            f"<td>{_esc(g.get('runs', 0))}</td>"
            f"<td>{_esc(rate_s)}</td>"
            f"<td>{_esc(median_s)}</td>"
            f"<td>{_safe_text(g.get('last_status'))}</td>"
            "</tr>"
        )

    stage_rows = []
    for s in stages:
        stage_rows.append(
            "<tr>"
            f"<td>{_safe_text(s.get('stage'))}</td>"
            f"<td>{_esc(s.get('runs', 0))}</td>"
            f"<td>{_esc(s.get('ok_runs', 0))}</td>"
            f"<td>{_esc(s.get('median_seconds', '—'))}</td>"
            f"<td>{_esc(s.get('total_seconds', '—'))}</td>"
            "</tr>"
        )

    cost_rows = []
    by_model = cost.get("by_model") or {}
    for model, bucket in sorted(by_model.items()):
        cost_rows.append(
            "<tr>"
            f"<td>{_safe_text(model)}</td>"
            f"<td>{_esc(bucket.get('entries', 0))}</td>"
            f"<td>{_esc(bucket.get('input', 0))}</td>"
            f"<td>{_esc(bucket.get('output', 0))}</td>"
            f"<td>{_esc(round(float(bucket.get('usd') or 0), 4))}</td>"
            "</tr>"
        )

    lead_block = ""
    if lead_time is not None:
        lt = redact_attributes({k: v for k, v in lead_time.items() if k != "prompt"})
        status = _safe_text(lt.get("status"))
        seconds = lt.get("seconds")
        seconds_s = "—" if seconds is None else str(seconds)
        lead_block = f"""
<section>
  <h2>Lead time</h2>
  <p>Event pair: <code>{_safe_text(lt.get('start_event') or pair.get('start'))}</code>
     → <code>{_safe_text(lt.get('end_event') or pair.get('end'))}</code></p>
  <p>Status: <strong>{status}</strong> · seconds: <strong>{_esc(seconds_s)}</strong>
     · issue: {_esc(lt.get('issue') or '—')} · PR: {_esc(lt.get('pull_request') or '—')}</p>
</section>
"""
    elif pair:
        lead_block = f"""
<section>
  <h2>Lead time</h2>
  <p>Event pair: <code>{_safe_text(pair.get('start'))}</code>
     → <code>{_safe_text(pair.get('end'))}</code>
     (pass <code>--lead-time-fixture</code> to embed a sample).</p>
</section>
"""

    total_usd = round(float(cost.get("total_usd") or 0), 4)
    auto_status = _safe_text(autonomy.get("status") or "no-evaluable")
    # Honest label: never advertise L2/L3 even if a stale payload tried.
    level_label = "L1 (demoted)" if autonomy.get("level") == 1 else "L0"
    wilson = autonomy.get("wilson_lower")
    wilson_s = "—" if wilson is None else f"{float(wilson):.4f}"
    auto_trials = autonomy.get("trials", 0)
    auto_ok = autonomy.get("successes", 0)
    auto_detail = _safe_text(autonomy.get("detail") or "")
    detail_bit = f" · detail: <strong>{_esc(auto_detail)}</strong>" if auto_detail else ""
    autonomy_block = f"""
<section>
  <h2>Autonomy</h2>
  <p class="meta">Wilson L0 from allowlisted stages (skip ≠ pass). L1 stays no-evaluable until ≥2 stages have min_n. Never claims level 3.</p>
  <p>Status: <strong>{auto_status}</strong> · level: <strong>{_esc(level_label)}</strong>
     · wilson_lower: <strong>{_esc(wilson_s)}</strong>
     · gates: {_esc(auto_ok)}/{_esc(auto_trials)}{detail_bit}</p>
</section>
"""
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>AI SDLC monitor — {_esc(profile)}</title>
<style>
  :root {{ color-scheme: light; font-family: ui-sans-serif, system-ui, sans-serif; }}
  body {{ margin: 1.5rem; max-width: 56rem; line-height: 1.45; color: #1a1a1a; }}
  h1 {{ font-size: 1.35rem; margin-bottom: 0.25rem; }}
  h2 {{ font-size: 1.05rem; margin-top: 1.5rem; }}
  table {{ border-collapse: collapse; width: 100%; font-size: 0.92rem; }}
  th, td {{ border-bottom: 1px solid #ddd; padding: 0.35rem 0.5rem; text-align: left; }}
  th {{ font-weight: 600; }}
  .meta {{ color: #555; font-size: 0.9rem; }}
  code {{ font-size: 0.88em; }}
</style>
</head>
<body>
<header>
  <h1>AI SDLC monitor</h1>
  <p class="meta">Read-only snapshot of the evidence ledger. No policy write path.</p>
  <p>{_esc(runs)} stage runs, {_esc(failed)} failed.</p>
</header>
{autonomy_block}
<section>
  <h2>Gates</h2>
  <table>
    <thead><tr><th>Stage</th><th>Gate</th><th>Runs</th><th>Pass rate</th><th>Median s</th><th>Last</th></tr></thead>
    <tbody>
      {''.join(gate_rows) or '<tr><td colspan="6">No gate runs</td></tr>'}
    </tbody>
  </table>
</section>
<section>
  <h2>Stages</h2>
  <table>
    <thead><tr><th>Stage</th><th>Runs</th><th>OK</th><th>Median s</th><th>Total s</th></tr></thead>
    <tbody>
      {''.join(stage_rows) or '<tr><td colspan="5">No stage runs</td></tr>'}
    </tbody>
  </table>
</section>
{lead_block}
<section>
  <h2>Cost</h2>
  <table>
    <thead><tr><th>Model</th><th>Entries</th><th>Input</th><th>Output</th><th>USD</th></tr></thead>
    <tbody>
      {''.join(cost_rows) or '<tr><td colspan="5">No cost entries</td></tr>'}
    </tbody>
  </table>
  <p>Total: ${_esc(total_usd)} ({_esc(cost.get('unpriced_entries', 0))} unpriced)</p>
</section>
</body>
</html>
"""


def write_monitor(
    path: Path,
    data: dict[str, Any],
    lead_time_fixture: Path | None = None,
) -> Path:
    lead = None
    if lead_time_fixture is not None:
        lead = load_lead_time_fixture(lead_time_fixture)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(render_monitor(data, lead), encoding="utf-8")
    return path
