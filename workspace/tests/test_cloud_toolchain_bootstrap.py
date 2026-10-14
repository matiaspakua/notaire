#!/usr/bin/env python3
"""Guards #1659 (CU76): Claude Code cloud sessions start with the toolchain the gates need."""

from __future__ import annotations

import json
import os
import re
import stat
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
HOOK = REPO_ROOT / ".claude" / "hooks" / "session-start-toolchain.sh"
SETTINGS = REPO_ROOT / ".claude" / "settings.json"
POM = REPO_ROOT / "pom.xml"
CURSOR_INSTALL = REPO_ROOT / ".cursor" / "install.sh"
ENSURE_TOOLS = REPO_ROOT / "security" / "tools" / "ensure_local_tools.sh"


def pin(text: str, name: str) -> str:
    return re.search(rf'^{name}="?([^"\s]+)"?', text, re.MULTILINE).group(1)


def stub(path: Path, body: str = "exit 0") -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(f"#!/bin/sh\n{body}\n", encoding="utf-8")
    path.chmod(path.stat().st_mode | stat.S_IXUSR)


def run_hook(env_extra: dict[str, str], path: str) -> subprocess.CompletedProcess:
    env = {"HOME": env_extra.pop("HOME"), "PATH": path, **env_extra}
    return subprocess.run(["bash", str(HOOK)], capture_output=True, text=True, env=env, timeout=60)


class ToolchainBootstrapTest(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp(prefix="toolchain-"))
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(self.tmp)]))
        self.home = self.tmp / "home"
        self.home.mkdir()
        self.env_file = self.tmp / "claude-env.sh"
        self.tools = self.home / ".cache" / "notaire-tools"

    def test_hook_is_registered_for_session_start(self):
        settings = json.loads(SETTINGS.read_text(encoding="utf-8"))
        commands = [h["command"] for group in settings["hooks"]["SessionStart"] for h in group["hooks"]]
        self.assertTrue(any("session-start-toolchain.sh" in c for c in commands), commands)

    def test_pins_match_their_owners(self):
        hook = HOOK.read_text(encoding="utf-8")
        pom_java = re.search(r"<java.version>(\d+)</java.version>", POM.read_text(encoding="utf-8")).group(1)
        openspec = re.search(r"@fission-ai/openspec@([\d.]+)", CURSOR_INSTALL.read_text(encoding="utf-8")).group(1)
        self.assertEqual(pom_java, pin(hook, "JDK_VERSION"))
        self.assertEqual(openspec, pin(hook, "OPENSPEC_VERSION"))
        self.assertEqual(pin(ENSURE_TOOLS.read_text(encoding="utf-8"), "TRIVY_VERSION"), pin(hook, "TRIVY_VERSION"))

    def test_alert_tool_pins_equal_the_compose_image_tags(self):
        hook = HOOK.read_text(encoding="utf-8")
        compose = (REPO_ROOT / "infra" / "observability" / "docker-compose.yml").read_text(encoding="utf-8")
        prometheus = re.search(r"image: prom/prometheus:v([\d.]+)", compose).group(1)
        alertmanager = re.search(r"image: prom/alertmanager:v([\d.]+)", compose).group(1)
        self.assertEqual(prometheus, pin(hook, "PROMETHEUS_VERSION"))
        self.assertEqual(alertmanager, pin(hook, "ALERTMANAGER_VERSION"))

    def test_hook_installs_the_prometheus_binary_from_the_release_tarball(self):
        hook = HOOK.read_text(encoding="utf-8")
        self.assertIn("linux-amd64/prometheus", hook.replace('"', ""))
        self.assertRegex(hook, r'\[ -x "\$BIN/prometheus" \]')

    def test_outside_cloud_sessions_the_hook_does_nothing(self):
        result = run_hook({"HOME": str(self.home), "CLAUDE_ENV_FILE": str(self.env_file)}, "/usr/bin:/bin")
        self.assertEqual(result.returncode, 0)
        self.assertFalse(self.env_file.exists())
        self.assertFalse(self.tools.exists())

    def test_an_install_failure_does_not_block_the_session(self):
        stubs = self.tmp / "stubs"
        stub(stubs / "curl", "exit 22")
        stub(stubs / "npm", "exit 1")
        result = run_hook(
            {"HOME": str(self.home), "CLAUDE_CODE_REMOTE": "true", "CLAUDE_ENV_FILE": str(self.env_file)},
            f"{stubs}:/usr/bin:/bin",
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("warning", result.stdout.lower())

    def test_environment_is_exported_from_the_cache(self):
        stub(self.tools / "jdk" / "bin" / "java")
        for tool in ("openspec", "kustomize", "trivy", "promtool", "amtool", "prometheus"):
            stub(self.tools / "bin" / tool)
        result = run_hook(
            {"HOME": str(self.home), "CLAUDE_CODE_REMOTE": "true", "CLAUDE_ENV_FILE": str(self.env_file)},
            "/usr/bin:/bin",
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        exported = self.env_file.read_text(encoding="utf-8")
        self.assertIn("LC_ALL=C.UTF-8", exported)
        self.assertIn("OPENSPEC_TELEMETRY=0", exported)
        self.assertIn(f"JAVA_HOME={self.tools}/jdk", exported)
        self.assertIn(f"PATH={self.tools}/bin:{self.tools}/jdk/bin:", exported)
        self.assertIn(f"KUSTOMIZE={self.tools}/bin/kustomize", exported)


if __name__ == "__main__":
    unittest.main()
