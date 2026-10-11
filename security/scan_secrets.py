#!/usr/bin/env python3
"""High-confidence secret scan for the Notaire working tree.

Issue #307, CU78. Patterns follow the Gitleaks families for private keys,
GitHub personal access tokens, AWS access key IDs, and Slack tokens. The
scanner prints only the path, line, and rule id. It never prints the secret.

Exit status is 1 when any finding remains after the policy allowlist.
"""

from __future__ import annotations

import re
import sys
from dataclasses import dataclass
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_POLICY = Path(__file__).resolve().parent / "policy" / "security-policy.yml"

SKIP_DIRS = {
    ".git",
    ".next",
    ".venv",
    "node_modules",
    "target",
    "dist",
    "coverage",
    "playwright-report",
    "test-results",
    ".pytest_cache",
}
SKIP_SUFFIXES = {
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".webp",
    ".ico",
    ".pdf",
    ".zip",
    ".jar",
    ".woff",
    ".woff2",
    ".ttf",
}
MAX_BYTES = 1_000_000

PRIVATE_KEY = re.compile(
    r"-----BEGIN (?:RSA |OPENSSH |EC |DSA )?PRIVATE KEY-----"
    r"[A-Za-z0-9+/=\r\n]{40,}?"
    r"-----END (?:RSA |OPENSSH |EC |DSA )?PRIVATE KEY-----"
)
GITHUB_PAT = re.compile(r"\bghp_[A-Za-z0-9]{36}\b")
AWS_ACCESS_KEY = re.compile(r"\bAKIA[0-9A-Z]{16}\b")
SLACK_TOKEN = re.compile(r"\bxox[baprs]-[0-9A-Za-z-]{10,48}\b")

RULES = (
    ("private-key", PRIVATE_KEY),
    ("github-pat", GITHUB_PAT),
    ("aws-access-key", AWS_ACCESS_KEY),
    ("slack-token", SLACK_TOKEN),
)


@dataclass(frozen=True)
class Finding:
    path: str
    line: int
    kind: str


def load_policy(path: Path | None = None) -> dict:
    policy_path = path or DEFAULT_POLICY
    return yaml.safe_load(policy_path.read_text(encoding="utf-8"))


def _ignore_literals(policy: dict) -> tuple[str, ...]:
    return tuple(policy.get("secret_scan", {}).get("ignore_literals") or ())


def _ignore_prefixes(policy: dict) -> tuple[str, ...]:
    return tuple(policy.get("secret_scan", {}).get("ignore_path_prefixes") or ())


def _redact(text: str, literals: tuple[str, ...]) -> str:
    redacted = text
    for literal in literals:
        if literal:
            redacted = redacted.replace(literal, "")
    return redacted


def _line_of(text: str, index: int) -> int:
    return text.count("\n", 0, index) + 1


def scan_text(text: str, relpath: str, policy: dict) -> list[Finding]:
    prefixes = _ignore_prefixes(policy)
    if any(relpath == prefix or relpath.startswith(prefix) for prefix in prefixes):
        return []
    redacted = _redact(text, _ignore_literals(policy))
    findings: list[Finding] = []
    for kind, pattern in RULES:
        for match in pattern.finditer(redacted):
            findings.append(Finding(relpath, _line_of(redacted, match.start()), kind))
    return findings


def _skipped(path: Path) -> bool:
    if any(part in SKIP_DIRS for part in path.parts):
        return True
    return path.suffix.lower() in SKIP_SUFFIXES


def scan_tree(root: Path, policy: dict) -> list[Finding]:
    findings: list[Finding] = []
    for path in root.rglob("*"):
        if not path.is_file() or _skipped(path):
            continue
        if path.stat().st_size > MAX_BYTES:
            continue
        data = path.read_bytes()
        if b"\0" in data[:1024]:
            continue
        relpath = path.relative_to(root).as_posix()
        findings.extend(scan_text(data.decode("utf-8", errors="ignore"), relpath, policy))
    return findings


def format_findings(findings: list[Finding]) -> str:
    if not findings:
        return "secret scan: no high-confidence findings"
    lines = [f"{item.path}:{item.line}: {item.kind}" for item in findings]
    return "secret scan findings (values redacted):\n" + "\n".join(lines)


def main() -> int:
    policy = load_policy()
    findings = scan_tree(REPO_ROOT, policy)
    print(format_findings(findings))
    return 1 if findings else 0


if __name__ == "__main__":
    sys.exit(main())
