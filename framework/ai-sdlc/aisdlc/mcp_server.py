"""MCP stdio JSON-RPC 2.0 server for aisdlc (#2395).

Stdlib only — newline-delimited JSON on stdin/stdout.
Tools: intent_submit, intent_show, factory_status.
"""
from __future__ import annotations

import json
import sys
from typing import Any

# ---------------------------------------------------------------------------
# Tool definitions
# ---------------------------------------------------------------------------

TOOLS = [
    {
        "name": "intent_submit",
        "description": "Submit a new factory intent (prompt, reference, or spec).",
        "inputSchema": {
            "type": "object",
            "properties": {
                "text": {"type": "string", "description": "Prompt text for the intent"},
                "ref": {"type": "string", "description": "Reference in system:id format (e.g. github:#123)"},
                "spec": {"type": "string", "description": "Markdown spec content"},
                "title": {"type": "string", "description": "Optional title override"},
            },
        },
    },
    {
        "name": "intent_show",
        "description": "Show an intent by its ID.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "id": {"type": "string", "description": "Intent ID (e.g. int-abc123)"},
            },
            "required": ["id"],
        },
    },
    {
        "name": "factory_status",
        "description": "Show factory status summary (intent counts, etc.).",
        "inputSchema": {
            "type": "object",
            "properties": {},
        },
    },
]


# ---------------------------------------------------------------------------
# JSON-RPC helpers
# ---------------------------------------------------------------------------

def _jsonrpc_error(code: int, message: str, request_id: Any) -> dict:
    return {
        "jsonrpc": "2.0",
        "error": {"code": code, "message": message},
        "id": request_id,
    }


def _jsonrpc_result(result: Any, request_id: Any) -> dict:
    return {
        "jsonrpc": "2.0",
        "result": result,
        "id": request_id,
    }


# ---------------------------------------------------------------------------
# Tool handlers
# ---------------------------------------------------------------------------

def _handle_intent_submit(ctx, arguments: dict) -> dict:
    from aisdlc.intent_model import submit as submit_intent

    intent = submit_intent(
        ctx,
        text=arguments.get("text"),
        ref=arguments.get("ref"),
        spec=arguments.get("spec"),
        title=arguments.get("title"),
        actor="mcp",
    )
    return {
        "content": [{"type": "text", "text": json.dumps(intent)}],
    }


def _handle_intent_show(ctx, arguments: dict) -> dict:
    from aisdlc.intent_model import show_intent

    intent_id = arguments.get("id", "")
    if not intent_id:
        return {
            "content": [{"type": "text", "text": json.dumps({"error": "id is required"})}],
            "isError": True,
        }
    intent = show_intent(ctx, intent_id)
    return {
        "content": [{"type": "text", "text": json.dumps(intent)}],
    }


def _handle_factory_status(ctx, arguments: dict) -> dict:
    from aisdlc.intent_model import list_intents

    intents_data = list_intents(ctx)
    entries = intents_data.get("entries", [])
    status = {
        "intents": len(entries),
        "entries": entries,
    }
    return {
        "content": [{"type": "text", "text": json.dumps(status)}],
    }


TOOL_HANDLERS = {
    "intent_submit": _handle_intent_submit,
    "intent_show": _handle_intent_show,
    "factory_status": _handle_factory_status,
}


# ---------------------------------------------------------------------------
# MCP server main loop
# ---------------------------------------------------------------------------

def run(ctx) -> None:
    """Run the MCP stdio server. Blocks until stdin is closed."""
    for raw_line in sys.stdin:
        raw_line = raw_line.strip()
        if not raw_line:
            continue

        # Parse JSON-RPC request
        try:
            request = json.loads(raw_line)
        except json.JSONDecodeError:
            resp = _jsonrpc_error(-32700, "Parse error", None)
            sys.stdout.write(json.dumps(resp) + "\n")
            sys.stdout.flush()
            continue

        request_id = request.get("id")
        method = request.get("method", "")

        # --- initialize ---
        if method == "initialize":
            resp = _jsonrpc_result(
                {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {"tools": {"listChanged": False}},
                    "serverInfo": {"name": "aisdlc-mcp", "version": "0.1.0"},
                },
                request_id,
            )
            sys.stdout.write(json.dumps(resp) + "\n")
            sys.stdout.flush()
            continue

        # --- notifications/initialized ---
        if method == "notifications/initialized":
            continue

        # --- tools/list ---
        if method == "tools/list":
            resp = _jsonrpc_result({"tools": TOOLS}, request_id)
            sys.stdout.write(json.dumps(resp) + "\n")
            sys.stdout.flush()
            continue

        # --- tools/call ---
        if method == "tools/call":
            params = request.get("params", {})
            tool_name = params.get("name", "")
            arguments = params.get("arguments", {})

            handler = TOOL_HANDLERS.get(tool_name)
            if handler is None:
                resp = _jsonrpc_result(
                    {
                        "content": [
                            {
                                "type": "text",
                                "text": json.dumps({"error": f"unknown tool: {tool_name}"}),
                            }
                        ],
                        "isError": True,
                    },
                    request_id,
                )
                sys.stdout.write(json.dumps(resp) + "\n")
                sys.stdout.flush()
                continue

            try:
                result = handler(ctx, arguments)
                resp = _jsonrpc_result(result, request_id)
            except (ValueError, KeyError) as exc:
                resp = _jsonrpc_result(
                    {
                        "content": [{"type": "text", "text": json.dumps({"error": str(exc)})}],
                        "isError": True,
                    },
                    request_id,
                )
            sys.stdout.write(json.dumps(resp) + "\n")
            sys.stdout.flush()
            continue

        # Unknown method
        resp = _jsonrpc_error(-32601, f"Method not found: {method}", request_id)
        sys.stdout.write(json.dumps(resp) + "\n")
        sys.stdout.flush()
