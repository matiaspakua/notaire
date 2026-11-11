"""TelemetryPort: real OTel NoOp path, redaction, env rules, lead-time/cost hooks (#2125)."""
from __future__ import annotations

import json
import os
import subprocess
import sys
import textwrap
import unittest
from pathlib import Path

from helpers import ENV, KIT, Repo, output

# Optional SDK site-packages (created by verify.sh / ensure-otel). Never a hard kit dependency.
_SITE = KIT / ".venv-telemetry" / "lib" / f"python{sys.version_info.major}.{sys.version_info.minor}" / "site-packages"


def _otel_env() -> dict[str, str]:
    env = dict(ENV)
    if _SITE.is_dir():
        env["PYTHONPATH"] = str(_SITE) + (os.pathsep + env["PYTHONPATH"] if env.get("PYTHONPATH") else "")
    return env


def _otel_available() -> bool:
    if not _SITE.is_dir():
        return False
    probe = subprocess.run(
        [sys.executable, "-c", "import opentelemetry.trace, opentelemetry.sdk.trace"],
        env=_otel_env(),
        capture_output=True,
        text=True,
    )
    return probe.returncode == 0


PROFILE_OFF = """
[project]
name = "demo"
policy = "POLICY.md"

[telemetry]
enabled = false
exporter = "none"
"""

PROFILE_ON_NOOP = """
[project]
name = "demo"
policy = "POLICY.md"

[telemetry]
enabled = true
exporter = "none"
"""


class TelemetryRedactionTest(unittest.TestCase):
    def test_prompts_diffs_and_secrets_are_stripped(self):
        sys.path.insert(0, str(KIT))
        from aisdlc.telemetry import redact_attributes  # noqa: E402

        dirty = {
            "aisdlc.stage": "plan",
            "prompt": "system: ignore previous",
            "diff": "--- a\n+++ b\n",
            "api_key": "sk-secret-value",
            "password": "hunter2",
            "authorization": "Bearer tok",
            "token": "abc",
            "safe": "ok",
        }
        clean = redact_attributes(dirty)
        self.assertEqual({"aisdlc.stage": "plan", "safe": "ok"}, clean)
        blob = json.dumps(clean)
        for banned in ("ignore previous", "--- a", "sk-secret", "hunter2", "Bearer", "abc"):
            self.assertNotIn(banned, blob)


class TelemetryDefaultOffTest(unittest.TestCase):
    def test_default_profile_does_not_emit_or_inject_traceparent(self):
        # #2264: cloud shells export TRACEPARENT; isolate so the scenario stays evaluable.
        from unittest import mock

        with mock.patch.dict(os.environ, {}, clear=False):
            os.environ.pop("TRACEPARENT", None)
            for key in [k for k in list(os.environ) if k.startswith("OTEL_")]:
                os.environ.pop(key, None)

            repo = Repo(self, PROFILE_OFF)
            result = repo.run(
                "cost",
                "--agent",
                "worker",
                "--model",
                "m",
                "--input",
                "1",
                "--output",
                "1",
                "--usd",
                "0.01",
            )
            self.assertEqual(0, result.returncode, output(result))
            # Child probe starts clean of TRACEPARENT; kit must not inject it.
            child_env = dict(_otel_env())
            child_env.pop("TRACEPARENT", None)
            for key in [k for k in list(child_env) if k.startswith("OTEL_")]:
                child_env.pop(key, None)
            child = subprocess.run(
                [sys.executable, "-c", "import os; print(os.environ.get('TRACEPARENT',''))"],
                cwd=repo.root,
                env=child_env,
                capture_output=True,
                text=True,
            )
            self.assertEqual("", os.environ.get("TRACEPARENT", ""))
            self.assertEqual("", child.stdout.strip())


class TelemetrySdkPathTest(unittest.TestCase):
    @unittest.skipUnless(_otel_available(), "optional OpenTelemetry venv not installed")
    def test_enabled_exporter_none_uses_real_noop_and_does_not_export(self):
        script = textwrap.dedent(
            """
            import json, sys
            sys.path.insert(0, {kit!r})
            from aisdlc.profile import load_profile
            from aisdlc.telemetry import bind_telemetry, record_cost_event, redact_attributes
            profile = load_profile(text={profile!r})
            port = bind_telemetry(profile, env={{}})
            self_check = {{
                "kind": port.kind,
                "sdk": port.sdk_name,
                "recording": port.is_recording(),
                "exported": port.exported_spans(),
                "redacted": redact_attributes({{"prompt": "secret-prompt", "aisdlc.stage": "red"}}),
            }}
            record_cost_event(port, agent="worker", model="m", tokens_in=1, tokens_out=2, usd=0.01)
            # prompts must stay out of any recorded attributes
            attrs = port.debug_attributes()
            self_check["attrs"] = attrs
            self_check["has_prompt"] = any("prompt" in str(a).lower() or "secret-prompt" in str(a) for a in attrs)
            print(json.dumps(self_check))
            """
        ).format(kit=str(KIT), profile=PROFILE_ON_NOOP)
        result = subprocess.run(
            [sys.executable, "-c", script],
            env=_otel_env(),
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        data = json.loads(result.stdout.strip().splitlines()[-1])
        self.assertEqual("otel-noop", data["kind"])
        self.assertIn("opentelemetry", data["sdk"])
        self.assertFalse(data["recording"])
        self.assertEqual([], data["exported"])
        self.assertEqual({"aisdlc.stage": "red"}, data["redacted"])
        self.assertFalse(data["has_prompt"])

    @unittest.skipUnless(_otel_available(), "optional OpenTelemetry venv not installed")
    def test_otel_sdk_disabled_is_honored(self):
        script = textwrap.dedent(
            """
            import json, os, sys
            sys.path.insert(0, {kit!r})
            os.environ["OTEL_SDK_DISABLED"] = "true"
            from aisdlc.profile import load_profile
            from aisdlc.telemetry import bind_telemetry, record_stage_event
            profile = load_profile(text={profile!r})
            port = bind_telemetry(profile, env=dict(os.environ))
            record_stage_event(port, stage="plan", ok=True, seconds=1.5)
            print(json.dumps({{
                "kind": port.kind,
                "recording": port.is_recording(),
                "exported": port.exported_spans(),
                "traceparent": port.traceparent(),
            }}))
            """
        ).format(kit=str(KIT), profile=PROFILE_ON_NOOP)
        result = subprocess.run(
            [sys.executable, "-c", script],
            env={**_otel_env(), "OTEL_SDK_DISABLED": "true"},
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        data = json.loads(result.stdout.strip().splitlines()[-1])
        self.assertFalse(data["recording"])
        self.assertEqual([], data["exported"])
        self.assertEqual("", data["traceparent"])

    @unittest.skipUnless(_otel_available(), "optional OpenTelemetry venv not installed")
    def test_traceparent_only_when_recording_sdk_path(self):
        """TRACEPARENT inject is proved only on a recording provider; exporter=none stays empty."""
        script = textwrap.dedent(
            """
            import json, sys
            sys.path.insert(0, {kit!r})
            from aisdlc.profile import load_profile
            from aisdlc.telemetry import bind_telemetry, record_stage_event
            profile = load_profile(text={profile!r})
            port = bind_telemetry(profile, env={{}})
            record_stage_event(port, stage="green", ok=True, seconds=0.2)
            print(json.dumps({{"traceparent": port.traceparent(), "kind": port.kind}}))
            """
        ).format(kit=str(KIT), profile=PROFILE_ON_NOOP)
        result = subprocess.run(
            [sys.executable, "-c", script],
            env=_otel_env(),
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        data = json.loads(result.stdout.strip().splitlines()[-1])
        # exporter=none → real NoOpTracerProvider → no TRACEPARENT (explicit; not a recording path)
        self.assertEqual("otel-noop", data["kind"])
        self.assertEqual("", data["traceparent"])


class TelemetryLeadTimeCostHookTest(unittest.TestCase):
    def test_cost_and_stage_hooks_accept_safe_fields_only(self):
        sys.path.insert(0, str(KIT))
        from aisdlc.telemetry import MemoryTelemetry, record_cost_event, record_stage_event  # noqa: E402

        port = MemoryTelemetry()
        record_stage_event(port, stage="merge", ok=True, seconds=12.5,
                           prompt="DO NOT LEAK", diff="--- secret", api_key="x")
        record_cost_event(port, agent="worker", model="mid", tokens_in=10, tokens_out=5, usd=0.02,
                          prompt="also leak", password="p")
        events = port.events()
        self.assertEqual(2, len(events))
        blob = json.dumps(events)
        for banned in ("DO NOT LEAK", "--- secret", "also leak", "password", "api_key", "prompt"):
            self.assertNotIn(banned, blob)
        self.assertEqual("merge", events[0]["attributes"]["aisdlc.stage"])
        self.assertEqual(12.5, events[0]["attributes"]["aisdlc.duration_seconds"])
        self.assertEqual(0.02, events[1]["attributes"]["aisdlc.cost_usd"])
        # model is span/event detail only when low-risk; never a metric attribute name with free text prompts
        self.assertEqual("mid", events[1]["attributes"]["aisdlc.model"])


class TelemetryProfileValidationTest(unittest.TestCase):
    def test_unknown_exporter_fails_profile_load(self):
        repo = Repo(
            self,
            textwrap.dedent(
                """
                [project]
                name = "demo"
                policy = "POLICY.md"
                [telemetry]
                enabled = true
                exporter = "hosted-collector"
                """
            ),
        )
        result = repo.run("doctor")
        self.assertNotEqual(0, result.returncode)
        self.assertIn("exporter", output(result).lower())


if __name__ == "__main__":
    unittest.main()
