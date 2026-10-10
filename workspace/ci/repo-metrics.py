#!/usr/bin/env python3
"""
Repository metrics baseline for #1197 Phase 0 / #1256 / #1417 (CU76).

Offline-friendly: reads the checkout only (no GitHub API). Optional network
enrichment is deliberately omitted so CI and agents get a stable baseline.

Usage:
  python3 workspace/ci/repo-metrics.py --json /tmp/m.json
  python3 workspace/ci/repo-metrics.py --markdown docs/300-development/REPO-METRICS-BASELINE.md
  python3 workspace/ci/repo-metrics.py --markdown -   # stdout
"""
from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]

ALWAYS_LOADED = [
    "CLAUDE.md",
    "AGENTS.md",
    "CONSTITUTION.md",
    ".claude/rules/general.md",
    ".claude/rules/programming.md",
    ".claude/rules/code-quality.md",
    ".claude/rules/refactoring.md",
    ".claude/rules/ai-agent-workflow.md",
    ".claude/rules/ui-ux-design.md",
    ".claude/rules/database-migrations.md",
    ".claude/rules/hooks.md",
]


def _dir_size_bytes(path: Path) -> int:
    if not path.exists():
        return 0
    total = 0
    for p in path.rglob("*"):
        if p.is_file():
            try:
                total += p.stat().st_size
            except OSError:
                continue
    return total


def _count_files(path: Path, pattern: str) -> int:
    if not path.exists():
        return 0
    return sum(1 for _ in path.rglob(pattern) if _.is_file())


def collect(repo: Path = REPO_ROOT) -> dict:
    modules_path = repo / "workspace" / "modules.yaml"
    modules_doc = yaml.safe_load(modules_path.read_text(encoding="utf-8")) or {}
    modules = modules_doc.get("modules") or {}

    workflows_dir = repo / ".github" / "workflows"
    workflow_files = sorted(workflows_dir.glob("*.yml")) + sorted(workflows_dir.glob("*.yaml"))
    with_paths = 0
    for wf in workflow_files:
        text = wf.read_text(encoding="utf-8", errors="replace")
        # naive but stable: look for paths: under on.push / on.pull_request
        if "\n    paths:" in text or "\n  paths:" in text or "paths:" in text and "paths-ignore" not in text:
            # count only if paths: appears as a filter key (not paths-ignore alone)
            lines = text.splitlines()
            for i, line in enumerate(lines):
                stripped = line.strip()
                if stripped.startswith("paths:") and "paths-ignore" not in stripped:
                    with_paths += 1
                    break
                if stripped == "paths:" and i + 1 < len(lines) and lines[i + 1].strip().startswith("-"):
                    with_paths += 1
                    break

    always_bytes = 0
    always_present = []
    for rel in ALWAYS_LOADED:
        p = repo / rel
        if p.is_file():
            size = p.stat().st_size
            always_bytes += size
            always_present.append({"path": rel, "bytes": size})

    # ~4 chars/token rough estimate for always-loaded Markdown
    always_tokens_est = max(1, always_bytes // 4)

    deprecated = repo / "deprecated"
    docs_dir = repo / "docs"
    puml_count = _count_files(docs_dir / "200-architecture" / "204-diagrams", "*.puml")

    module_rows = []
    for name, meta in modules.items():
        module_rows.append(
            {
                "name": name,
                "path": meta.get("path"),
                "fleet": meta.get("fleet"),
                "responsibility": meta.get("responsibility"),
                "depends_on": list(meta.get("depends_on") or []),
            }
        )

    return {
        "generated_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "generator": "workspace/ci/repo-metrics.py",
        "issue": "#1417 / #1256 / #1197 P0.1",
        "modules": module_rows,
        "modules_count": len(module_rows),
        "workflows_total": len(workflow_files),
        "workflows_with_paths_filter": with_paths,
        "always_loaded_files": always_present,
        "always_loaded_bytes": always_bytes,
        "always_loaded_tokens_estimate": always_tokens_est,
        "deprecated_bytes": _dir_size_bytes(deprecated),
        "deprecated_files": _count_files(deprecated, "*") if deprecated.exists() else 0,
        "docs_bytes": _dir_size_bytes(docs_dir),
        "plantuml_diagram_files": puml_count,
        "notes": [
            "Token estimate is bytes/4 (rough); measure with the agent loader for exact counts.",
            "Workflow path-filter count is heuristic over YAML text; re-check when editing workflows.",
            "Script is offline-only; PR cross-area rates require gh and are recorded in ADR-024.",
        ],
    }


def render_markdown(data: dict) -> str:
    lines = [
        "# Repository Metrics Baseline",
        "",
        f"> Generated: `{data['generated_at']}` by `{data['generator']}` ({data['issue']}, CU76).",
        ">",
        "> Regenerate: `python3 workspace/ci/repo-metrics.py --markdown docs/300-development/REPO-METRICS-BASELINE.md`",
        "",
        "## Summary",
        "",
        "| Metric | Value |",
        "|--------|------:|",
        f"| Modules (`workspace/modules.yaml`) | {data['modules_count']} |",
        f"| Workflows total | {data['workflows_total']} |",
        f"| Workflows with `paths:` filter (heuristic) | {data['workflows_with_paths_filter']} |",
        f"| Always-loaded bytes | {data['always_loaded_bytes']} |",
        f"| Always-loaded tokens (est.) | {data['always_loaded_tokens_estimate']} |",
        f"| `deprecated/` bytes | {data['deprecated_bytes']} |",
        f"| `deprecated/` files | {data['deprecated_files']} |",
        f"| `docs/` bytes | {data['docs_bytes']} |",
        f"| PlantUML `.puml` under `204-diagrams/` | {data['plantuml_diagram_files']} |",
        "",
        "## Modules",
        "",
        "| Module | Path | Fleet | Depends on |",
        "|--------|------|-------|------------|",
    ]
    for m in data["modules"]:
        deps = ", ".join(m["depends_on"]) if m["depends_on"] else "—"
        lines.append(f"| `{m['name']}` | `{m['path']}` | {m['fleet']} | {deps} |")

    lines.extend(
        [
            "",
            "## Always-loaded agent files",
            "",
            "| Path | Bytes |",
            "|------|------:|",
        ]
    )
    for f in data["always_loaded_files"]:
        lines.append(f"| `{f['path']}` | {f['bytes']} |")

    lines.extend(
        [
            "",
            "## Notes",
            "",
        ]
    )
    for n in data["notes"]:
        lines.append(f"- {n}")
    lines.append("")
    lines.append("Related: [MODULE-OWNERSHIP.md](MODULE-OWNERSHIP.md), [REPO-SPLIT-PLAN.md](REPO-SPLIT-PLAN.md), ADR-024, #1197.")
    lines.append("")
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Collect offline repository metrics for #1197 Phase 0.")
    parser.add_argument("--json", metavar="PATH", help="Write JSON metrics to PATH")
    parser.add_argument(
        "--markdown",
        metavar="PATH",
        help="Write Markdown baseline to PATH (use - for stdout)",
    )
    args = parser.parse_args(argv)

    if not args.json and not args.markdown:
        parser.error("provide --json and/or --markdown")

    data = collect()

    if args.json:
        path = Path(args.json)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

    if args.markdown:
        md = render_markdown(data)
        if args.markdown == "-":
            sys.stdout.write(md)
        else:
            path = Path(args.markdown)
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(md, encoding="utf-8")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
