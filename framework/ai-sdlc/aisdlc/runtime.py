"""Factory runtime adapters over Project / Run ports (#2242 Phase D).

HTML/serve are driving adapters only — CLI/OpenAPI Project and Run paths remain
the system of record. Loopback serve binds 127.0.0.1 by default. Distinct from
the backoffice control plane (FACTORY-UI-SPEC §6). Agent never commits or
approves from this surface.
"""
from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any
from urllib.parse import parse_qs, urlparse

from aisdlc import projects, runs, workflows
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.profile import ProfileError

BRAND = "AI SDLC factory"

MODULES = ("projects", "start", "monitor")

MODULE_LABELS = {
    "projects": "Projects",
    "start": "Start run",
    "monitor": "Monitor",
}

CANCEL_REASON = (
    "Cancel unavailable: Runner port not proven — harness stubs are inventory only "
    "(POST /factory/runs/{id}/cancel → 501)."
)


def module_label(module: str) -> str:
    return MODULE_LABELS.get(module, module.replace("_", " ").title())


def snapshot(
    ctx: Context,
    *,
    module: str = "projects",
    entity_id: str | None = None,
    error: str | None = None,
) -> dict[str, Any]:
    """Assemble a read model for runtime HTML rendering."""
    if module not in MODULES:
        raise ProfileError(f"unknown runtime module {module!r} (expected {', '.join(MODULES)})")

    base: dict[str, Any] = {
        "brand": BRAND,
        "surface": "runtime",
        "module": module,
        "modules": list(MODULES),
        "module_labels": dict(MODULE_LABELS),
        "error": error,
        "cancel_supported": False,
        "cancel_reason": CANCEL_REASON,
    }

    if module == "projects":
        listed = projects.list_projects(ctx)
        entries = listed.get("entries") or []
        detail = None
        if entity_id:
            try:
                detail = projects.show(ctx, entity_id)
            except KitError as exc:
                base["error"] = base["error"] or str(exc)
        return {**base, "entries": entries, "detail": detail}

    if module == "start":
        approved = workflows.list_workflows(ctx, approved_only=True)
        project_list = projects.list_projects(ctx)
        return {
            **base,
            "approved_workflows": approved.get("entries") or [],
            "projects": project_list.get("entries") or [],
            "entries": approved.get("entries") or [],
        }

    # monitor
    run_list = runs.list_runs(ctx)
    entries = run_list.get("entries") or []
    detail = None
    events: list[dict] = []
    if entity_id:
        try:
            detail = runs.show(ctx, entity_id)
            events = runs.list_events(ctx, entity_id).get("events") or []
        except KitError as exc:
            base["error"] = base["error"] or str(exc)
    return {
        **base,
        "entries": entries,
        "detail": detail,
        "events": events,
    }


def create_project(
    ctx: Context,
    *,
    project_id: str,
    profile_ref: str,
) -> dict[str, Any]:
    try:
        return projects.create(ctx, project_id=project_id, profile_ref=profile_ref)
    except KitError as exc:
        raise ProfileError(f"{exc.code}: {exc}") from exc


def add_repo(
    ctx: Context,
    *,
    project_id: str,
    repo_id: str,
    forge: str,
    url: str,
    default_branch: str = "main",
    mode: str | None = None,
) -> dict[str, Any]:
    repo: dict[str, Any] = {
        "id": repo_id,
        "forge": forge,
        "url": url,
        "default_branch": default_branch or "main",
    }
    if mode:
        repo["mode"] = mode
    try:
        return projects.add_repo(ctx, project_id=project_id, repo=repo)
    except KitError as exc:
        raise ProfileError(f"{exc.code}: {exc}") from exc


def start_run(
    ctx: Context,
    *,
    project_id: str,
    workflow_id: str,
) -> dict[str, Any]:
    try:
        return runs.start(ctx, project_id=project_id, workflow_id=workflow_id)
    except KitError as exc:
        raise ProfileError(f"{exc.code}: {exc}") from exc


def export_html(
    ctx: Context,
    path,
    *,
    module: str = "projects",
    entity_id: str | None = None,
) -> int:
    from pathlib import Path

    from aisdlc.runtime_html import write_html

    data = snapshot(ctx, module=module, entity_id=entity_id)
    write_html(Path(path), data)
    print(f"runtime html: {path}")
    return 0


def start_server(ctx: Context, *, host: str = "127.0.0.1", port: int = 8765) -> ThreadingHTTPServer:
    """Start a thin loopback HTTP adapter. Caller owns serve_forever/shutdown."""
    from aisdlc.runtime_html import render_shell

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
            module = (qs.get("module") or ["projects"])[0]
            entity_id = (qs.get("id") or [None])[0]
            error = (qs.get("error") or [None])[0]
            try:
                data = snapshot(ctx, module=module, entity_id=entity_id, error=error)
                html = render_shell(data, form_action="/mutate")
                self._send(200, html)
            except ProfileError as exc:
                data = snapshot(ctx, module="projects", error=str(exc))
                self._send(400, render_shell(data, form_action="/mutate"))

        def do_POST(self) -> None:  # noqa: N802
            parsed = urlparse(self.path)
            if parsed.path != "/mutate":
                self._send(404, "not found")
                return
            length = int(self.headers.get("Content-Length") or 0)
            raw = self.rfile.read(length).decode("utf-8") if length else ""
            form = {k: (v[0] if v else "") for k, v in parse_qs(raw, keep_blank_values=True).items()}
            module = form.get("module") or "projects"
            action = form.get("action") or ""
            entity_id = form.get("id") or ""
            redirect_module = module
            redirect_id = entity_id
            try:
                if action in {"approve", "commit", "cancel"}:
                    raise ProfileError(
                        "runtime UI refuses commit/approve/cancel — "
                        "sole-committer and ApprovalPort axioms unchanged; "
                        f"cancel: {CANCEL_REASON}"
                    )
                if module == "projects" and action == "create":
                    if not entity_id:
                        raise ProfileError("id is required")
                    create_project(
                        ctx,
                        project_id=entity_id,
                        profile_ref=form.get("profile_ref") or "aisdlc.toml",
                    )
                elif module == "projects" and action == "add_repo":
                    if not entity_id:
                        raise ProfileError("id is required")
                    repo_id = form.get("repo_id") or ""
                    if not repo_id:
                        raise ProfileError("repo_id is required")
                    mode = form.get("mode") or None
                    add_repo(
                        ctx,
                        project_id=entity_id,
                        repo_id=repo_id,
                        forge=form.get("forge") or "",
                        url=form.get("url") or "",
                        default_branch=form.get("default_branch") or "main",
                        mode=mode if mode else None,
                    )
                elif module == "start" and action == "start":
                    project_id = form.get("project") or ""
                    workflow_id = form.get("workflow") or ""
                    if not project_id or not workflow_id:
                        raise ProfileError("project and workflow are required")
                    run = start_run(ctx, project_id=project_id, workflow_id=workflow_id)
                    redirect_module = "monitor"
                    redirect_id = run["id"]
                else:
                    raise ProfileError(f"unknown action {action!r} for module {module!r}")
                loc = f"/?module={redirect_module}"
                if redirect_id:
                    loc += f"&id={redirect_id}"
                self._redirect(loc)
            except ProfileError as exc:
                accept = self.headers.get("Accept") or ""
                err = str(exc)
                if "application/json" in accept:
                    self._send(400, json.dumps({"error": err}), content_type="application/json")
                    return
                data = snapshot(
                    ctx,
                    module=module if module in MODULES else "projects",
                    entity_id=entity_id or None,
                    error=err,
                )
                self._send(400, render_shell(data, form_action="/mutate"))

    return ThreadingHTTPServer((host, port), Handler)


def serve(ctx: Context, *, host: str = "127.0.0.1", port: int = 8765) -> int:
    server = start_server(ctx, host=host, port=port)
    bound = server.server_address[1]
    print(f"runtime serve: http://{host}:{bound}/ (Ctrl+C to stop)")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nruntime serve: stopped")
    finally:
        server.server_close()
    return 0
