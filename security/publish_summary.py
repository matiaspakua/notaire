#!/usr/bin/env python3
"""Render the security case report as Markdown for the CI job summary.

Issue #1506, CU78. The HTML report stays an artifact. This text is what the
GitHub Actions run shows. It never prints secret values; the scanner already
redacts those before the report is written.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path


def render(report: dict) -> str:
    summary = report.get("summary") or {}
    lines = [
        "## Security cases",
        "",
        f"Profile `{report.get('profile', '')}` · "
        f"pass {summary.get('pass', 0)} · "
        f"fail {summary.get('fail', 0)} · "
        f"skip {summary.get('skip', 0)}",
        "",
        "| Case | Status | Family | Detail |",
        "|------|--------|--------|--------|",
    ]
    for case in report.get("cases") or []:
        detail = str(case.get("detail", "")).replace("|", "/").replace("\n", " ")
        lines.append(
            f"| `{case.get('id', '')}` | {case.get('status', '')} | "
            f"{case.get('family', '')} | {detail} |"
        )
    lines.append("")
    lines.append("Full HTML is the `security-case-report` artifact.")
    lines.append("A hosted remote copy is a later step; this summary is the CI view.")
    return "\n".join(lines) + "\n"


def main(argv: list[str] | None = None) -> int:
    args = list(sys.argv[1:] if argv is None else argv)
    if len(args) != 1:
        print("usage: python3 security/publish_summary.py <report.json>", file=sys.stderr)
        return 2
    path = Path(args[0])
    if not path.is_file():
        print("Security case report was not produced.")
        return 0
    report = json.loads(path.read_text(encoding="utf-8"))
    sys.stdout.write(render(report))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
