"""Static / served HTML for the AI SDLC factory runtime (#2242).

Kit-neutral brand. Separate from backoffice: Projects, Start run, Monitor.
Never offers commit or approve controls. Never paints ``running`` without
evidence-derived status from the snapshot.
"""
from __future__ import annotations

import html
import json
from pathlib import Path
from typing import Any

from aisdlc.runtime import BRAND, MODULE_LABELS, MODULES, module_label


def _esc(value: Any) -> str:
    return html.escape("" if value is None else str(value), quote=True)


def render_shell(data: dict[str, Any], *, form_action: str | None = None) -> str:
    """Build a self-contained runtime HTML document."""
    brand = _esc(data.get("brand") or BRAND)
    module = data.get("module") or "projects"
    modules = data.get("modules") or list(MODULES)
    labels = data.get("module_labels") or dict(MODULE_LABELS)
    error = data.get("error")
    action = form_action or "#"
    title_label = _esc(labels.get(module) or module_label(str(module)))

    nav_items = []
    for name in modules:
        active = ' aria-current="page"' if name == module else ""
        label = labels.get(name) or module_label(str(name))
        nav_items.append(
            f'<li><a href="?module={_esc(name)}"{active}>{_esc(label)}</a></li>'
        )

    error_block = ""
    if error:
        error_block = f'<p class="error" role="alert">error: {_esc(error)}</p>'

    if module == "projects":
        body = _render_projects(data, action, title_label)
    elif module == "start":
        body = _render_start(data, action, title_label)
    else:
        body = _render_monitor(data, title_label)

    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>{brand} — runtime</title>
<style>
  :root {{
    color-scheme: light;
    --ink: #1c1917;
    --muted: #57534e;
    --line: #d6d3d1;
    --bg: #fafaf9;
    --panel: #ffffff;
    --accent: #0f766e;
    --err: #b91c1c;
    font-family: "IBM Plex Sans", "Segoe UI", sans-serif;
  }}
  * {{ box-sizing: border-box; }}
  body {{
    margin: 0;
    background:
      radial-gradient(circle at 88% 12%, #ccfbf1 0, transparent 40%),
      linear-gradient(160deg, #fafaf9 0%, #f5f5f4 55%, #ecfdf5 100%);
    color: var(--ink);
    min-height: 100vh;
  }}
  .layout {{
    display: grid;
    grid-template-columns: minmax(0, 1fr) 14rem;
    gap: 0;
    min-height: 100vh;
  }}
  main {{
    padding: 1.75rem 1.5rem 2.5rem;
    max-width: 52rem;
  }}
  .module-nav.right {{
    border-left: 1px solid var(--line);
    background: color-mix(in srgb, var(--panel) 88%, #ccfbf1);
    padding: 1.5rem 1rem;
    position: sticky;
    top: 0;
    align-self: start;
    min-height: 100vh;
  }}
  .module-nav.right h2 {{
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--muted);
    margin: 0 0 0.75rem;
  }}
  .module-nav.right ul {{ list-style: none; margin: 0; padding: 0; }}
  .module-nav.right a {{
    display: block;
    padding: 0.45rem 0.6rem;
    color: var(--ink);
    text-decoration: none;
    border-radius: 0.35rem;
  }}
  .module-nav.right a[aria-current="page"] {{
    background: #0f766e;
    color: #ecfdf5;
  }}
  h1 {{
    font-family: "IBM Plex Serif", "Source Serif 4", Georgia, serif;
    font-size: clamp(1.6rem, 2.4vw, 2.1rem);
    margin: 0 0 0.35rem;
    letter-spacing: -0.02em;
  }}
  h2 {{ font-size: 1.05rem; margin-top: 1.6rem; }}
  .meta {{ color: var(--muted); font-size: 0.92rem; }}
  .error {{ color: var(--err); font-weight: 600; }}
  table {{ border-collapse: collapse; width: 100%; font-size: 0.92rem; }}
  th, td {{ border-bottom: 1px solid var(--line); padding: 0.4rem 0.5rem; text-align: left; }}
  form {{
    display: grid;
    gap: 0.55rem;
    max-width: 28rem;
    margin-top: 0.75rem;
  }}
  label {{ display: grid; gap: 0.2rem; font-size: 0.9rem; }}
  input, button, select {{
    font: inherit;
    padding: 0.4rem 0.55rem;
    border: 1px solid var(--line);
    border-radius: 0.35rem;
    background: var(--panel);
  }}
  button {{
    background: var(--accent);
    color: #ecfdf5;
    border-color: var(--accent);
    cursor: pointer;
    width: fit-content;
  }}
  button:disabled, .action-disabled {{
    opacity: 0.45;
    cursor: not-allowed;
  }}
  .status {{ font-weight: 600; color: var(--accent); }}
  .actions {{ display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.75rem; }}
  pre.json {{
    background: #1c1917;
    color: #f5f5f4;
    padding: 0.75rem;
    overflow: auto;
    font-size: 0.8rem;
    border-radius: 0.4rem;
  }}
  @media (max-width: 720px) {{
    .layout {{ grid-template-columns: 1fr; }}
    .module-nav.right {{
      position: static;
      min-height: 0;
      border-left: 0;
      border-top: 1px solid var(--line);
      order: 2;
    }}
  }}
  @media (prefers-reduced-motion: reduce) {{
    * {{ animation: none !important; transition: none !important; }}
  }}
</style>
</head>
<body data-surface="runtime">
<div class="layout">
  <main>
    <header>
      <h1>{brand}</h1>
      <p class="meta">Runtime · project → repos → approved workflow → evidence progress</p>
    </header>
    {error_block}
    {body}
  </main>
  <nav id="module-nav" class="module-nav right" aria-label="Runtime">
    <h2>Runtime</h2>
    <ul>
      {''.join(nav_items)}
    </ul>
  </nav>
</div>
</body>
</html>
"""


def _render_projects(data: dict[str, Any], action: str, title_label: str) -> str:
    entries = data.get("entries") or []
    detail = data.get("detail")
    rows = []
    for entry in entries:
        eid = entry.get("id") or "—"
        pref = entry.get("profile_ref") or ""
        n_repos = len(entry.get("repos") or [])
        rows.append(
            "<tr>"
            f'<td><a href="?module=projects&amp;id={_esc(eid)}">{_esc(eid)}</a></td>'
            f"<td>{_esc(pref)}</td>"
            f"<td>{_esc(n_repos)}</td>"
            "</tr>"
        )

    detail_block = ""
    if detail:
        repo_rows = []
        for repo in detail.get("repos") or []:
            repo_rows.append(
                "<tr>"
                f"<td>{_esc(repo.get('id'))}</td>"
                f"<td>{_esc(repo.get('forge'))}</td>"
                f"<td>{_esc(repo.get('url'))}</td>"
                f"<td>{_esc(repo.get('default_branch'))}</td>"
                f"<td>{_esc(repo.get('mode'))}</td>"
                "</tr>"
            )
        detail_block = f"""
<section data-module="projects" data-view="detail" data-id="{_esc(detail.get('id'))}">
  <h2>Project {_esc(detail.get('id'))}</h2>
  <p class="meta">profile_ref: <code>{_esc(detail.get('profile_ref'))}</code></p>
  <table>
    <thead><tr><th>Repo</th><th>Forge</th><th>URL</th><th>Branch</th><th>Mode</th></tr></thead>
    <tbody>
      {''.join(repo_rows) or '<tr><td colspan="5">No repositories bound</td></tr>'}
    </tbody>
  </table>
  <h2>Bind repository</h2>
  <p class="meta">ForgePort-honest refs. Mode defaults to dry_run when omitted.</p>
  <form method="post" action="{_esc(action)}">
    <input type="hidden" name="module" value="projects"/>
    <input type="hidden" name="action" value="add_repo"/>
    <input type="hidden" name="id" value="{_esc(detail.get('id'))}"/>
    <label>Repo id <input name="repo_id" required/></label>
    <label>Forge <input name="forge" placeholder="github"/></label>
    <label>URL <input name="url" required/></label>
    <label>Default branch <input name="default_branch" value="main"/></label>
    <label>Mode
      <select name="mode">
        <option value="">dry_run (default)</option>
        <option value="dry_run">dry_run</option>
        <option value="live">live</option>
      </select>
    </label>
    <button type="submit">Bind repo</button>
  </form>
  <pre class="json">{_esc(json.dumps(detail, indent=2, sort_keys=True))}</pre>
</section>
"""

    return f"""
<section data-module="projects" data-view="list">
  <h2>{title_label}</h2>
  <p class="meta">One job: bind a profile root and repositories (file/profile — not SaaS tenancy).</p>
  <table>
    <thead><tr><th>Id</th><th>Profile</th><th>Repos</th></tr></thead>
    <tbody>
      {''.join(rows) or '<tr><td colspan="3">No projects</td></tr>'}
    </tbody>
  </table>
</section>
<section data-module="projects" data-action="create">
  <h2>Create project</h2>
  <form method="post" action="{_esc(action)}">
    <input type="hidden" name="module" value="projects"/>
    <input type="hidden" name="action" value="create"/>
    <label>Id <input name="id" required/></label>
    <label>Profile ref <input name="profile_ref" value="aisdlc.toml" required/></label>
    <button type="submit">Create</button>
  </form>
</section>
{detail_block}
"""


def _render_start(data: dict[str, Any], action: str, title_label: str) -> str:
    approved = data.get("approved_workflows") or []
    project_list = data.get("projects") or []

    project_opts = [
        f'<option value="{_esc(p.get("id"))}">{_esc(p.get("id"))}</option>'
        for p in project_list
        if p.get("id")
    ]
    workflow_opts = []
    for wf in approved:
        wid = wf.get("id")
        if not wid:
            continue
        gov = wf.get("governance") or "approved"
        ver = wf.get("version")
        label = f"{wid} · {gov}" + (f" · v{ver}" if ver is not None else "")
        workflow_opts.append(
            f'<option value="{_esc(wid)}">{_esc(label)}</option>'
        )

    empty_note = ""
    if not workflow_opts:
        empty_note = (
            '<p class="meta" role="status">No approved workflows. '
            "Approve a workflow in the backoffice/CLI before starting a run.</p>"
        )

    return f"""
<section data-module="start" data-view="form">
  <h2>{title_label}</h2>
  <p class="meta">Picker lists governance state <strong>approved</strong> only.
  Start creates a RunRequest; status stays queued until a harness adapter reports.</p>
  {empty_note}
  <form method="post" action="{_esc(action)}">
    <input type="hidden" name="module" value="start"/>
    <input type="hidden" name="action" value="start"/>
    <label>Project
      <select name="project" required>
        <option value="">Select project…</option>
        {''.join(project_opts)}
      </select>
    </label>
    <label>Approved workflow
      <select name="workflow" required data-filter="approved">
        <option value="">Select approved workflow…</option>
        {''.join(workflow_opts)}
      </select>
    </label>
    <button type="submit">Start run</button>
  </form>
</section>
"""


def _render_monitor(data: dict[str, Any], title_label: str) -> str:
    entries = data.get("entries") or []
    detail = data.get("detail")
    events = data.get("events") or []
    cancel_reason = data.get("cancel_reason") or "Cancel unavailable."
    cancel_ok = bool(data.get("cancel_supported"))

    rows = []
    for entry in entries:
        rid = entry.get("id") or "—"
        status = entry.get("status") or "—"
        rows.append(
            "<tr>"
            f'<td><a href="?module=monitor&amp;id={_esc(rid)}">{_esc(rid)}</a></td>'
            f'<td><span class="status" data-status="{_esc(status)}">{_esc(status)}</span></td>'
            f"<td>{_esc((entry.get('workflow') or {}).get('id'))}</td>"
            f"<td>{_esc(entry.get('project'))}</td>"
            "</tr>"
        )

    detail_block = ""
    if detail:
        status = detail.get("status") or "—"
        step_rows = []
        for step in detail.get("steps") or []:
            step_status = step.get("status") or "—"
            # Honesty: never present skip as pass in the label.
            label = step_status
            if step_status == "skip":
                label = "skip (not pass)"
            step_rows.append(
                "<tr>"
                f"<td>{_esc(step.get('id'))}</td>"
                f'<td data-step-status="{_esc(step_status)}">{_esc(label)}</td>'
                "</tr>"
            )

        event_rows = []
        harness_events = []
        for event in events:
            kind = event.get("kind")
            event_rows.append(
                "<tr>"
                f"<td>{_esc(event.get('ts'))}</td>"
                f"<td>{_esc(kind)}</td>"
                f"<td>{_esc(event.get('adapter') or event.get('step') or '')}</td>"
                "</tr>"
            )
            # run.request is kit-side RunRequest — not a harness adapter report.
            if kind and kind != "run.request":
                harness_events.append(event)

        if harness_events:
            events_block = f"""
  <h2>Evidence events</h2>
  <table aria-live="polite">
    <thead><tr><th>Time</th><th>Kind</th><th>Detail</th></tr></thead>
    <tbody>{''.join(event_rows)}</tbody>
  </table>
"""
        else:
            request_note = ""
            if event_rows:
                request_note = (
                    "<p class=\"meta\">RunRequest recorded "
                    f"({len(event_rows)} ledger event(s)); awaiting harness.</p>"
                )
            events_block = f"""
  {request_note}
  <p class="meta" role="status" aria-live="polite">
    No adapter has reported yet. Status stays queued/requested until
    <code>run.started</code> evidence arrives — the UI never fakes running.
  </p>
"""

        detail_block = f"""
<section data-module="monitor" data-view="detail" data-id="{_esc(detail.get('id'))}"
         data-status="{_esc(status)}">
  <h2>Run {_esc(detail.get('id'))}</h2>
  <p class="meta">Status:
    <span class="status" data-status="{_esc(status)}">{_esc(status)}</span>
    · project <code>{_esc(detail.get('project'))}</code>
    · workflow <code>{_esc((detail.get('workflow') or {}).get('id'))}</code>
    @v{_esc((detail.get('workflow') or {}).get('version'))}
  </p>
  <h2>Steps</h2>
  <table>
    <thead><tr><th>Step</th><th>Status</th></tr></thead>
    <tbody>
      {''.join(step_rows) or '<tr><td colspan="2">No steps seeded</td></tr>'}
    </tbody>
  </table>
  {events_block}
  <div class="actions" data-actions="run">
    <button type="button" class="action-disabled" disabled
            data-action="cancel" data-supported="{'true' if cancel_ok else 'false'}">
      Cancel
    </button>
    <p class="meta">{_esc(cancel_reason)}</p>
  </div>
</section>
"""

    return f"""
<section data-module="monitor" data-view="list">
  <h2>{title_label}</h2>
  <p class="meta">Progress from evidence/harness signals only. Skip ≠ pass.</p>
  <table>
    <thead><tr><th>Run</th><th>Status</th><th>Workflow</th><th>Project</th></tr></thead>
    <tbody>
      {''.join(rows) or '<tr><td colspan="4">No runs</td></tr>'}
    </tbody>
  </table>
</section>
{detail_block}
"""


def write_html(path: Path, data: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(render_shell(data, form_action="#"), encoding="utf-8")
