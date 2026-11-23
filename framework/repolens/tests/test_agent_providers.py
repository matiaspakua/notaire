"""Benchmark runner selects Cursor when Anthropic cannot authenticate."""
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "bench"))

from providers import build_argv, load_settings, select_provider, usage_from_stream  # noqa: E402


def which_of(found: dict[str, str]):
    def which(name: str):
        return found.get(name)

    return which


def test_cursor_fallback_when_claude_is_missing():
    settings = load_settings(None, {"CURSOR_API_KEY": "present"})
    selection = select_provider(settings, {"CURSOR_API_KEY": "present"}, which=which_of({"agent": "/bin/agent"}))
    assert selection.selected == "cursor"
    assert selection.via == "fallback"
    assert "ANTHROPIC_API_KEY" in selection.statuses[0].reason
    assert "claude" in selection.statuses[0].reason


def test_forced_anthropic_does_not_silently_switch_to_cursor():
    settings = load_settings(None, {"CURSOR_API_KEY": "present"}, provider="anthropic")
    selection = select_provider(
        settings,
        {"CURSOR_API_KEY": "present"},
        which=which_of({"agent": "/bin/agent"}),
        run_probe=lambda _argv: 0,
    )
    assert selection.selected is None
    assert selection.via == "forced"


def test_order_env_is_repolens_not_the_factory_prefix(monkeypatch):
    monkeypatch.setenv("REPOLENS_AGENT_ORDER", "cursor,anthropic")
    monkeypatch.delenv("AISDLC_AGENT_ORDER", raising=False)
    settings = load_settings(None)
    assert settings.order == ("cursor", "anthropic")


def test_cursor_graph_arm_passes_mcp_config_and_keeps_the_prompt():
    argv = build_argv("cursor", "agent", "find payment", model="composer", arm="graph", mcp=Path("mcp.json"))
    assert argv[-1] == "find payment"
    assert "--mcp-config" in argv
    assert "ask" in argv


def test_stream_usage_is_labeled_separately_from_a_missing_session():
    stream = "\n".join([
        json.dumps({"type": "assistant", "message": {"content": [{"type": "tool_use", "name": "Grep"}]}}),
        json.dumps({"type": "result", "usage": {"input_tokens": 10, "output_tokens": 1}, "result": "FILES: a.py"}),
    ])
    assert usage_from_stream(stream)["input_tokens"] == 10


def test_the_report_skips_an_agent_run_that_recorded_no_session(tmp_path):
    """A provider probe that ran no session leaves an empty summary; the report must not divide by zero on it."""
    import report

    assert report.agent_section({"model": "m", "rows": [], "summary": {}}, tmp_path) == ("", "", [])
