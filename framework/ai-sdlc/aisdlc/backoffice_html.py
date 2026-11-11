"""Static / served HTML for the AI SDLC factory backoffice (#2240).

Kit-neutral brand. Right-side module nav. Never renders plaintext secrets —
callers must pass already-redacted snapshots from ``backoffice.snapshot``.
"""
from __future__ import annotations

import html
import json
from pathlib import Path
from typing import Any

from aisdlc.backoffice import BRAND, MODULES


def _esc(value: Any) -> str:
    return html.escape("" if value is None else str(value), quote=True)


def render_shell(data: dict[str, Any], *, form_action: str | None = None) -> str:
    """Build a self-contained backoffice HTML document."""
    brand = _esc(data.get("brand") or BRAND)
    module = data.get("module") or "workflows"
    modules = data.get("modules") or list(MODULES)
    entries = data.get("entries") or []
    detail = data.get("detail")
    history = data.get("history") or {}
    error = data.get("error")
    action = form_action or "#"

    nav_items = []
    for name in modules:
        active = ' aria-current="page"' if name == module else ""
        nav_items.append(
            f'<li><a href="?module={_esc(name)}"{active}>{_esc(name.capitalize())}</a></li>'
        )

    error_block = ""
    if error:
        error_block = f'<p class="error" role="alert">error: {_esc(error)}</p>'

    rows = []
    for entry in entries:
        eid = entry.get("id") or "—"
        summary = entry.get("summary") or ""
        gov = entry.get("governance")
        version = entry.get("version")
        gov_bit = f' · <span class="gov">{_esc(gov)}</span>' if gov else ""
        ver_bit = f" · v{_esc(version)}" if version is not None else ""
        rows.append(
            "<tr>"
            f'<td><a href="?module={_esc(module)}&amp;id={_esc(eid)}">{_esc(eid)}</a></td>'
            f"<td>{_esc(summary)}{gov_bit}{ver_bit}</td>"
            "</tr>"
        )

    list_section = f"""
<section data-module="{_esc(module)}" data-view="list">
  <h2>{_esc(str(module).capitalize())}</h2>
  <p class="meta">One job: configure this catalog kind via Phase A ports.</p>
  <table>
    <thead><tr><th>Id</th><th>Summary</th></tr></thead>
    <tbody>
      {''.join(rows) or '<tr><td colspan="2">No entries</td></tr>'}
    </tbody>
  </table>
</section>
"""

    create_section = f"""
<section data-module="{_esc(module)}" data-action="create">
  <h2>Create</h2>
  <form method="post" action="{_esc(action)}">
    <input type="hidden" name="module" value="{_esc(module)}"/>
    <input type="hidden" name="action" value="create"/>
    <label>Id <input name="id" required/></label>
    <label>Ref <input name="ref"/></label>
    <label>Summary <input name="summary"/></label>
    {_workflow_create_extras(module)}
    <button type="submit">Create</button>
  </form>
</section>
"""

    detail_section = ""
    if detail:
        detail_section = _render_detail(module, detail, history, action)

    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>{brand} — backoffice</title>
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
      radial-gradient(circle at 12% 8%, #ccfbf1 0, transparent 42%),
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
  .gov {{ font-weight: 600; color: var(--accent); }}
  .transitions {{ display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.75rem; }}
  code {{ font-size: 0.88em; }}
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
</style>
</head>
<body>
<div class="layout">
  <main>
    <header>
      <h1>{brand}</h1>
      <p class="meta">Backoffice control plane · adapters over Phase A catalogs &amp; workflows</p>
    </header>
    {error_block}
    {list_section}
    {create_section}
    {detail_section}
  </main>
  <nav id="module-nav" class="module-nav right" aria-label="Modules">
    <h2>Modules</h2>
    <ul>
      {''.join(nav_items)}
    </ul>
  </nav>
</div>
</body>
</html>
"""


def _workflow_create_extras(module: str) -> str:
    if module != "workflows":
        return ""
    return """
    <label>Agents (comma ids) <input name="agents"/></label>
    <label>Skills (comma ids) <input name="skills"/></label>
    <label>MCP (comma ids) <input name="mcp"/></label>
    <label>Providers (comma ids) <input name="providers"/></label>
    """


def _render_detail(module: str, detail: dict[str, Any], history: dict[str, Any], action: str) -> str:
    eid = detail.get("id") or ""
    gov = detail.get("governance")
    gov_attr = f' data-governance="{_esc(gov)}"' if gov else ""
    allowed = detail.get("allowed_transitions") or []
    version_rows = []
    for row in history.get("versions") or []:
        version_rows.append(
            "<tr>"
            f'<td data-version="{_esc(row.get("version"))}">{_esc(row.get("version"))}</td>'
            f"<td><code>{_esc(row.get('digest'))}</code></td>"
            f"<td>{_esc(row.get('summary') or '')}</td>"
            f"<td>{_esc(row.get('governance') or '')}</td>"
            "</tr>"
        )

    transitions = ""
    if module == "workflows":
        forms = []
        for name in allowed:
            extra = ""
            if name == "request_validation":
                extra = '<label>Waive reason <input name="waive_reason" required/></label>'
            if name == "approve":
                extra = (
                    '<label>Signature path <input name="signature"/></label>'
                    '<label><input type="checkbox" name="owner_override" value="1"/> Owner override</label>'
                    '<label>Reason <input name="reason"/></label>'
                )
            forms.append(
                f"""
<form method="post" action="{_esc(action)}" data-transition="{_esc(name)}">
  <input type="hidden" name="module" value="workflows"/>
  <input type="hidden" name="action" value="transition"/>
  <input type="hidden" name="id" value="{_esc(eid)}"/>
  <input type="hidden" name="transition" value="{_esc(name)}"/>
  {extra}
  <button type="submit">{_esc(name)}</button>
</form>
"""
            )
        transitions = f'<div class="transitions">{"".join(forms) or "<p class=meta>No transitions from this state.</p>"}</div>'

    pretty = html.escape(json.dumps(detail, indent=2, sort_keys=True), quote=False)

    return f"""
<section data-module="{_esc(module)}" data-view="detail" data-id="{_esc(eid)}"{gov_attr}>
  <h2>Detail · {_esc(eid)}</h2>
  <p class="meta">Version {_esc(detail.get('version'))}{' · governance <strong>' + _esc(gov) + '</strong>' if gov else ''}</p>
  {transitions}
  <h3>Edit (new version)</h3>
  <form method="post" action="{_esc(action)}" data-action="edit">
    <input type="hidden" name="module" value="{_esc(module)}"/>
    <input type="hidden" name="action" value="edit"/>
    <input type="hidden" name="id" value="{_esc(eid)}"/>
    <label>Ref <input name="ref" value="{_esc(detail.get('ref') or '')}"/></label>
    <label>Summary <input name="summary" value="{_esc(detail.get('summary') or '')}"/></label>
    <button type="submit">Save new version</button>
  </form>
  <h3>Version history</h3>
  <table>
    <thead><tr><th>Version</th><th>Digest</th><th>Summary</th><th>Governance</th></tr></thead>
    <tbody>
      {''.join(version_rows) or '<tr><td colspan="4">No history</td></tr>'}
    </tbody>
  </table>
  <h3>Current (redacted)</h3>
  <pre class="json">{pretty}</pre>
</section>
"""


def write_html(path: Path, data: dict[str, Any]) -> Path:
    path.parent.mkdir(parents=True, exist_ok=True)
    # Static export: forms point at "#"; mutations require `backoffice serve`.
    path.write_text(render_shell(data, form_action="#"), encoding="utf-8")
    return path
