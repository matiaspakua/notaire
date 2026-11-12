"""Factory backoffice adapters over Phase A catalogs/workflows + evidence (#2240/#2241).

HTML/serve are driving adapters only — CLI/OpenAPI catalogs and the evidence ledger
remain the system of record. No second config truth or parallel metrics store.
Loopback serve binds 127.0.0.1 by default. Does not implement ``aisdlc serve`` (#2253)
or If-Match writes (#2254).
"""
from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any
from urllib.parse import parse_qs, urlparse

from aisdlc import catalogs, evidence, workflows
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import redact
from aisdlc.profile import ProfileError

BRAND = "AI SDLC factory"

# FACTORY-UI-SPEC order: Workflows … Runs
MODULES = (
    "workflows",
    "agents",
    "skills",
    "mcp",
    "providers",
    "memory",
    "rules",
    "integrations",
    "runs",
)

MODULE_KIND = {
    "workflows": "workflows",
    "agents": "agents",
    "skills": "skills",
    "mcp": "mcp",
    "providers": "providers",
    "memory": "memory",
    "rules": "rules",
    "integrations": "integrations",
}

MODULE_LABELS = {
    "workflows": "Workflows",
    "agents": "Agents",
    "skills": "Skills",
    "mcp": "Tools / MCP",
    "providers": "Providers",
    "memory": "Knowledge",
    "rules": "Rules / Gates",
    "integrations": "Integrations",
    "runs": "Runs",
}

CATALOG_MODULES = tuple(m for m in MODULES if m != "runs")

RUNS_ACTIONS_REASON = (
    "Cancel/retry unavailable: Runner/Forge ports do not honestly support them yet "
    "(A.4 Project/Run entities shipped — #2255). Runs monitor is evidence-derived only."
)


def module_label(module: str) -> str:
    return MODULE_LABELS.get(module, module.replace("_", " ").title())


def allowed_transitions(governance: str) -> list[str]:
    """Phase A–legal transition actions for the current governance state."""
    return [action for action, (frm, _) in workflows.TRANSITIONS.items() if frm == governance]


def snapshot(
    ctx: Context,
    *,
    module: str = "workflows",
    entity_id: str | None = None,
    error: str | None = None,
) -> dict[str, Any]:
    """Assemble a redacted read model for HTML rendering."""
    if module not in MODULES:
        raise ProfileError(f"unknown backoffice module {module!r} (expected {', '.join(MODULES)})")

    if module == "runs":
        return _runs_snapshot(ctx, error=error)

    kind = MODULE_KIND[module]
    listed = catalogs.list_entries(ctx, kind)
    entries = []
    for row in listed.get("entries") or []:
        eid = row.get("id")
        if not eid or row.get("error"):
            entries.append(row)
            continue
        try:
            full = catalogs.show(ctx, kind, eid, redacted=True)
        except (ProfileError, KitError) as exc:
            entries.append({**row, "error": str(exc)})
            continue
        if kind == "workflows":
            gov = full.get("governance") or "draft"
            full = dict(full)
            full["allowed_transitions"] = allowed_transitions(gov)
        entries.append(full)

    detail = None
    history = None
    if entity_id:
        detail = catalogs.show(ctx, kind, entity_id, redacted=True)
        if kind == "workflows":
            gov = detail.get("governance") or "draft"
            detail = dict(detail)
            detail["allowed_transitions"] = allowed_transitions(gov)
        history = catalogs.history(ctx, kind, entity_id)

    payload = {
        "brand": BRAND,
        "module": module,
        "modules": list(MODULES),
        "module_labels": dict(MODULE_LABELS),
        "kind": kind,
        "entries": entries,
        "detail": detail,
        "history": history,
        "error": error,
        "runs_report": None,
        "runs_actions": None,
    }
    return redact("", payload)  # type: ignore[return-value]


def _runs_snapshot(ctx: Context, *, error: str | None = None) -> dict[str, Any]:
    report = evidence.report_summary(ctx)
    records = evidence.ledger_records(ctx)
    # Keep monitor payload lean: stage/ok/ts + gate count (no prompts/secrets).
    recent = []
    for row in records[-50:]:
        recent.append(
            {
                "ts": row.get("ts"),
                "stage": row.get("stage"),
                "ok": row.get("ok"),
                "gates": len(row.get("gates") or []),
            }
        )
    payload = {
        "brand": BRAND,
        "module": "runs",
        "modules": list(MODULES),
        "module_labels": dict(MODULE_LABELS),
        "kind": None,
        "entries": recent,
        "detail": None,
        "history": None,
        "error": error,
        "runs_report": {
            "runs": report.get("runs"),
            "failed_runs": report.get("failed_runs"),
            "stages": report.get("stages"),
            "gates": report.get("gates"),
            "cost": report.get("cost"),
            "autonomy": report.get("autonomy"),
            "profile": report.get("profile"),
            "source": "evidence.summarize + cost (aisdlc report)",
        },
        "runs_actions": {
            "cancel_supported": False,
            "retry_supported": False,
            "reason": RUNS_ACTIONS_REASON,
        },
    }
    return redact("", payload)  # type: ignore[return-value]


def create_entity(
    ctx: Context,
    module: str,
    entity_id: str,
    *,
    ref: str | None = None,
    summary: str = "",
    agents: list[str] | None = None,
    skills: list[str] | None = None,
    mcp: list[str] | None = None,
    providers: list[str] | None = None,
) -> dict[str, Any]:
    if module not in CATALOG_MODULES:
        raise ProfileError(f"unknown backoffice module {module!r}")
    if module == "runs":
        raise ProfileError("runs module is read-only (no create)")
    try:
        if module == "workflows":
            workflows.create(
                ctx,
                entity_id,
                summary=summary,
                agents=agents,
                skills=skills,
                mcp=mcp,
                providers=providers,
            )
            return catalogs.show(ctx, "workflows", entity_id, redacted=True)
        kind = MODULE_KIND[module]
        catalogs.add(ctx, kind, entity_id, ref=ref, summary=summary)
        return catalogs.show(ctx, kind, entity_id, redacted=True)
    except KitError as exc:
        raise ProfileError(str(exc)) from exc


def edit_entity(
    ctx: Context,
    module: str,
    entity_id: str,
    *,
    ref: str | None = None,
    summary: str | None = None,
    agents: list[str] | None = None,
    skills: list[str] | None = None,
    mcp: list[str] | None = None,
    providers: list[str] | None = None,
) -> dict[str, Any]:
    if module not in CATALOG_MODULES:
        raise ProfileError(f"unknown backoffice module {module!r}")
    if module == "runs":
        raise ProfileError("runs module is read-only (no edit)")
    try:
        if module == "workflows":
            workflows.new_version(
                ctx,
                entity_id,
                summary=summary,
                agents=agents,
                skills=skills,
                mcp=mcp,
                providers=providers,
            )
            return catalogs.show(ctx, "workflows", entity_id, redacted=True)
        kind = MODULE_KIND[module]
        catalogs.update(ctx, kind, entity_id, ref=ref, summary=summary)
        return catalogs.show(ctx, kind, entity_id, redacted=True)
    except KitError as exc:
        raise ProfileError(str(exc)) from exc


def transition_workflow(
    ctx: Context,
    workflow_id: str,
    action: str,
    *,
    signature: str | None = None,
    owner_override: bool = False,
    reason: str | None = None,
    waive_reason: str | None = None,
) -> dict[str, Any]:
    try:
        workflows.transition(
            ctx,
            workflow_id,
            action,
            signature=signature,
            owner_override=owner_override,
            reason=reason,
            waive_reason=waive_reason,
        )
    except KitError as exc:
        raise ProfileError(str(exc)) from exc
    return catalogs.show(ctx, "workflows", workflow_id, redacted=True)


def export_html(
    ctx: Context,
    path,
    *,
    module: str = "workflows",
    entity_id: str | None = None,
) -> int:
    from pathlib import Path

    from aisdlc.backoffice_html import write_html

    data = snapshot(ctx, module=module, entity_id=entity_id)
    write_html(Path(path), data)
    print(f"backoffice html: {path}")
    return 0


def start_server(ctx: Context, *, host: str = "127.0.0.1", port: int = 8765) -> ThreadingHTTPServer:
    """Start a thin loopback HTTP adapter. Caller owns serve_forever/shutdown."""
    from aisdlc.backoffice_html import render_shell

    class Handler(BaseHTTPRequestHandler):
        def log_message(self, format: str, *args) -> None:  # noqa: A003
            return

        def _send(self, status: int, body: str, *, content_type: str = "text/html; charset=utf-8") -> None:
            payload = body.encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(payload)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(payload)

        def _redirect(self, location: str) -> None:
            self.send_response(302)
            self.send_header("Location", location)
            self.end_headers()

        def do_GET(self) -> None:  # noqa: N802
            parsed = urlparse(self.path)
            qs = parse_qs(parsed.query)
            module = (qs.get("module") or ["workflows"])[0]
            entity_id = (qs.get("id") or [None])[0]
            error = (qs.get("error") or [None])[0]
            try:
                data = snapshot(ctx, module=module, entity_id=entity_id, error=error)
                html = render_shell(data, form_action="/mutate")
                self._send(200, html)
            except ProfileError as exc:
                data = snapshot(ctx, module="workflows", error=str(exc))
                self._send(400, render_shell(data, form_action="/mutate"))

        def do_POST(self) -> None:  # noqa: N802
            parsed = urlparse(self.path)
            if parsed.path != "/mutate":
                self._send(404, "not found")
                return
            length = int(self.headers.get("Content-Length") or 0)
            raw = self.rfile.read(length).decode("utf-8") if length else ""
            form = {k: (v[0] if v else "") for k, v in parse_qs(raw, keep_blank_values=True).items()}
            module = form.get("module") or "workflows"
            action = form.get("action") or ""
            entity_id = form.get("id") or ""
            try:
                if module == "runs" or action in {"cancel", "retry"}:
                    raise ProfileError(
                        "runs module is read-only; cancel/retry unavailable until Runner/Forge "
                        "ports honestly support them (#2255)"
                    )
                if action == "create":
                    if not entity_id:
                        raise ProfileError("id is required")
                    create_entity(
                        ctx,
                        module,
                        entity_id,
                        ref=form.get("ref") or None,
                        summary=form.get("summary") or "",
                        agents=_csv(form.get("agents")),
                        skills=_csv(form.get("skills")),
                        mcp=_csv(form.get("mcp")),
                        providers=_csv(form.get("providers")),
                    )
                elif action == "edit":
                    if not entity_id:
                        raise ProfileError("id is required")
                    edit_entity(
                        ctx,
                        module,
                        entity_id,
                        ref=form.get("ref") or None,
                        summary=form.get("summary") if "summary" in form else None,
                        agents=_csv(form.get("agents")) if form.get("agents") is not None and form.get("agents") != "" else None,
                        skills=_csv(form.get("skills")) if form.get("skills") else None,
                        mcp=_csv(form.get("mcp")) if form.get("mcp") else None,
                        providers=_csv(form.get("providers")) if form.get("providers") else None,
                    )
                elif action == "transition":
                    if module != "workflows":
                        raise ProfileError("transition is only valid for workflows")
                    transition_workflow(
                        ctx,
                        entity_id,
                        form.get("transition") or "",
                        signature=form.get("signature") or None,
                        owner_override=form.get("owner_override") in {"1", "true", "on", "yes"},
                        reason=form.get("reason") or None,
                        waive_reason=form.get("waive_reason") or None,
                    )
                else:
                    raise ProfileError(f"unknown action {action!r}")
                self._redirect(f"/?module={module}&id={entity_id}")
            except ProfileError as exc:
                accept = self.headers.get("Accept") or ""
                err = str(exc)
                if "application/json" in accept:
                    self._send(400, json.dumps({"error": err}), content_type="application/json")
                    return
                data = snapshot(ctx, module=module if module in MODULES else "workflows", entity_id=entity_id or None, error=err)
                self._send(400, render_shell(data, form_action="/mutate"))

    return ThreadingHTTPServer((host, port), Handler)


def serve(ctx: Context, *, host: str = "127.0.0.1", port: int = 8765) -> int:
    server = start_server(ctx, host=host, port=port)
    bound = server.server_address[1]
    print(f"backoffice serve: http://{host}:{bound}/ (Ctrl+C to stop)")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nbackoffice serve: stopped")
    finally:
        server.server_close()
    return 0


def _csv(raw: str | None) -> list[str] | None:
    if raw is None:
        return None
    parts = [p.strip() for p in str(raw).split(",") if p.strip()]
    return parts
