"""Failing MCP server tests for the intent entry point (#2395).

Spawns 'aisdlc mcp' with subprocess, pipes newline-delimited JSON-RPC lines.
Expected to fail because the mcp command does not exist yet.
"""
from __future__ import annotations

import json
import subprocess
import sys
import unittest

from helpers import BIN, ENV, KIT, Repo

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


def _rpc_id_counter():
    """Simple counter for JSON-RPC request ids."""
    counter = [0]

    def _next():
        counter[0] += 1
        return counter[0]

    return _next


class MCPServerTest(unittest.TestCase):
    """MCP stdio JSON-RPC 2.0 server tests."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self._next_id = _rpc_id_counter()

    def _spawn_mcp(self):
        """Spawn 'aisdlc mcp' process with stdin/stdout pipes."""
        proc = subprocess.Popen(
            [sys.executable, str(BIN), "--root", str(self.repo.root), "mcp"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
            env=ENV,
        )
        self.addCleanup(lambda: proc.terminate() or None)
        return proc

    def _send(self, proc, method: str, params: dict | None = None, is_notification: bool = False):
        """Send a JSON-RPC request/notification and return the response."""
        req = {"jsonrpc": "2.0", "method": method}
        if params is not None:
            req["params"] = params
        if not is_notification:
            req["id"] = self._next_id()
        line = json.dumps(req) + "\n"
        proc.stdin.write(line)
        proc.stdin.flush()

        if is_notification:
            return None

        # Read response line
        response_line = proc.stdout.readline()
        if not response_line:
            raise RuntimeError("MCP process closed without response")
        return json.loads(response_line)

    # --- Scenario: stdio MCP initialize and list tools ---
    def test_mcp_initialize_and_list(self):
        proc = self._spawn_mcp()

        # Initialize
        resp = self._send(proc, "initialize", {"protocolVersion": "2024-11-05", "capabilities": {}})
        self.assertIn("result", resp)
        self.assertIn("serverInfo", resp["result"])

        # Send initialized notification
        self._send(proc, "notifications/initialized", {}, is_notification=True)

        # List tools
        resp = self._send(proc, "tools/list")
        self.assertIn("result", resp)
        tools = resp["result"].get("tools", resp["result"])
        tool_names = [t["name"] for t in tools]
        self.assertIn("intent_submit", tool_names)
        self.assertIn("intent_show", tool_names)
        self.assertIn("factory_status", tool_names)

    # --- Scenario: MCP intent_submit tool ---
    def test_mcp_intent_submit(self):
        proc = self._spawn_mcp()
        self._send(proc, "initialize", {"protocolVersion": "2024-11-05", "capabilities": {}})
        self._send(proc, "notifications/initialized", {}, is_notification=True)

        resp = self._send(proc, "tools/call", {
            "name": "intent_submit",
            "arguments": {"text": "Test intent via MCP"},
        })
        self.assertIn("result", resp)
        self.assertFalse(resp.get("error"))
        content = resp["result"].get("content", [])
        self.assertGreater(len(content), 0)
        intent_json = json.loads(content[0]["text"])
        self.assertEqual("prompt", intent_json["kind"])
        self.assertTrue(intent_json["id"].startswith("int-"))

    # --- Scenario: MCP intent_show tool ---
    def test_mcp_intent_show(self):
        proc = self._spawn_mcp()
        self._send(proc, "initialize", {"protocolVersion": "2024-11-05", "capabilities": {}})
        self._send(proc, "notifications/initialized", {}, is_notification=True)

        # First submit an intent
        submit_resp = self._send(proc, "tools/call", {
            "name": "intent_submit",
            "arguments": {"text": "Show me via MCP"},
        })
        intent_json = json.loads(submit_resp["result"]["content"][0]["text"])
        intent_id = intent_json["id"]

        # Then show it
        resp = self._send(proc, "tools/call", {
            "name": "intent_show",
            "arguments": {"id": intent_id},
        })
        self.assertIn("result", resp)
        content = resp["result"].get("content", [])
        shown = json.loads(content[0]["text"])
        self.assertEqual(intent_id, shown["id"])

    # --- Scenario: MCP factory_status tool ---
    def test_mcp_factory_status(self):
        proc = self._spawn_mcp()
        self._send(proc, "initialize", {"protocolVersion": "2024-11-05", "capabilities": {}})
        self._send(proc, "notifications/initialized", {}, is_notification=True)

        resp = self._send(proc, "tools/call", {
            "name": "factory_status",
            "arguments": {},
        })
        self.assertIn("result", resp)
        content = resp["result"].get("content", [])
        self.assertGreater(len(content), 0)
        status = json.loads(content[0]["text"])
        # Status should have some counts
        self.assertIn("intents", status)

    # --- Scenario: unknown method → -32601 ---
    def test_mcp_unknown_method(self):
        proc = self._spawn_mcp()
        self._send(proc, "initialize", {"protocolVersion": "2024-11-05", "capabilities": {}})

        resp = self._send(proc, "unknown/method")
        self.assertIn("error", resp)
        self.assertEqual(-32601, resp["error"]["code"])

    # --- Scenario: malformed JSON → -32700 ---
    def test_mcp_malformed_json(self):
        proc = self._spawn_mcp()
        # Send valid initialize first
        req = json.dumps({"jsonrpc": "2.0", "method": "initialize", "id": self._next_id(),
                          "params": {"protocolVersion": "2024-11-05", "capabilities": {}}}) + "\n"
        proc.stdin.write(req)
        proc.stdin.flush()
        proc.stdout.readline()  # consume response

        # Send malformed JSON
        proc.stdin.write("not valid json\n")
        proc.stdin.flush()
        response_line = proc.stdout.readline()
        if response_line:
            resp = json.loads(response_line)
            self.assertIn("error", resp)
            self.assertEqual(-32700, resp["error"]["code"])

    # --- Scenario: unknown tool → isError result ---
    def test_mcp_unknown_tool(self):
        proc = self._spawn_mcp()
        self._send(proc, "initialize", {"protocolVersion": "2024-11-05", "capabilities": {}})
        self._send(proc, "notifications/initialized", {}, is_notification=True)

        resp = self._send(proc, "tools/call", {
            "name": "nonexistent_tool",
            "arguments": {},
        })
        self.assertIn("result", resp)
        # Tool not found should return isError
        self.assertTrue(resp["result"].get("isError", False))


if __name__ == "__main__":
    unittest.main()
