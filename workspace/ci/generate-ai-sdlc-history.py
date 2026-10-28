#!/usr/bin/env python3
"""Generate github-page/lib/ai-sdlc-history.json: the data behind the Pages AI SDLC case study (#1958).

WHY
---
The case study shows how the process evolved: commits, merged PRs, PRs about the process itself and
bug issues per month, plus the date each process mechanism landed. Those numbers must come from the
repository and GitHub, not from prose, and say when they were taken. The snapshot is committed (the
Pages build has no GitHub token for issue history); re-run this script to refresh it.

USAGE
  python3 workspace/ci/generate-ai-sdlc-history.py            # needs git and an authenticated gh
  python3 workspace/ci/generate-ai-sdlc-history.py --check    # exit 1 when the snapshot is missing

Fails without writing when gh cannot list PRs or issues: a partial snapshot would mislead.
"""
from __future__ import annotations

import argparse
import datetime as dt
import json
import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "github-page" / "lib" / "ai-sdlc-history.json"
SINCE = "2025-12"
PROCESS = re.compile(r"\b(sdlc|openspec|agents?|fleet|foreman|harness|constitution|preflight|local-ai|"
                     r"ai-sdlc|claude|skills?|steward|archive)\b", re.I)
BOTS = {"app/dependabot"}

# Process mechanisms whose first commit dates the case-study timeline: (path, label, issue).
MECHANISMS = (
    ("AGENTS.md", "First agent entry file", None),
    ("CLAUDE.md", "Agent configuration reorganized", None),
    ("workspace/sdlc/preflight.sh", "Local preflight mirrors CI", 720),
    ("CONSTITUTION.md", "Engineering Constitution", 781),
    ("workspace/sdlc/validate-sdlc-plan.sh", "Constitution wired into OpenSpec (Gate 1)", 783),
    ("workspace/sdlc/run_pipeline.sh", "Dashboarded pre-PR pipeline", 856),
    (".claude/hooks/block-push-to-main.sh", "Harness hooks enforce the workflow", 1027),
    (".github/workflows/sdlc-process.yml", "Process checks in CI", None),
    ("local-ai", "Local-model foreman/worker harness", 1074),
    ("docs/300-development/304-ai-sdlc-cloud", "Cloud agent fleet", 1114),
    ("workspace/sdlc/check-heavy-ci.sh", "Heavy-CI merge gate", 1133),
    ("CONSTITUTION-AGENT-CARD.md", "Context budget: 16k → 8k tokens", 1259),
    ("security/cases/catalog.yml", "Security case catalog before every PR", 1500),
    ("workspace/sdlc/check-openspec-archive-hygiene.py", "Archive-in-the-PR hygiene", 1252),
    ("workspace/sdlc/claim-issue.py", "Atomic issue claim and lanes", 1623),
    ("workspace/sdlc/publish-progress.sh", "Continuous publication", 1681),
    ("backend-api/src/test/resources/architecture/hexagon-baseline.txt", "Shrink-only ArchUnit baseline", 1695),
    ("frontend/eslint-suppressions.json", "Shrink-only frontend boundary baseline", 1696),
    ("framework/ai-sdlc", "Stack-agnostic framework extracted", 1958),
)


def month(timestamp: str) -> str:
    return timestamp[:7]


def pr_stats(prs: list[dict]) -> dict[str, dict[str, int]]:
    stats: dict[str, dict[str, int]] = defaultdict(lambda: {"merged_prs": 0, "process_prs": 0})
    for item in prs:
        if item["author"]["login"] in BOTS:
            continue
        bucket = stats[month(item["mergedAt"])]
        bucket["merged_prs"] += 1
        bucket["process_prs"] += 1 if PROCESS.search(item["title"]) else 0
    return dict(stats)


def bug_stats(issues: list[dict]) -> dict[str, int]:
    bugs: dict[str, int] = defaultdict(int)
    for item in issues:
        if any(label["name"] == "type:bug" for label in item["labels"]):
            bugs[month(item["createdAt"])] += 1
    return dict(bugs)


def merge_months(commits: dict[str, int], prs: dict[str, dict[str, int]], bugs: dict[str, int],
                 since: str) -> list[dict]:
    keys = sorted(k for k in set(commits) | set(prs) | set(bugs) if k >= since)
    return [{"month": k, "commits": commits.get(k, 0), "merged_prs": prs.get(k, {}).get("merged_prs", 0),
             "process_prs": prs.get(k, {}).get("process_prs", 0), "bug_issues": bugs.get(k, 0)} for k in keys]


def snapshot(months: list[dict], mechanisms: list[dict], legacy_commits: int, totals: dict) -> dict:
    return {
        "generated_at": dt.datetime.now(dt.timezone.utc).isoformat(timespec="seconds"),
        "generated_by": "python3 workspace/ci/generate-ai-sdlc-history.py",
        "notes": ("process_prs: merged PR titles naming the SDLC itself (sdlc, openspec, agents, fleet, harness, "
                  "constitution, preflight, skills, archive…); bug_issues: issues labelled type:bug by creation "
                  "month; Dependabot PRs excluded."),
        "legacy_commits": legacy_commits,
        "totals": totals,
        "months": months,
        "mechanisms": mechanisms,
    }


def _run(*args: str) -> str:
    return subprocess.run(args, cwd=ROOT, check=True, capture_output=True, text=True).stdout


def commit_months() -> dict[str, int]:
    counts: dict[str, int] = defaultdict(int)
    for line in _run("git", "log", "--format=%ad", "--date=format:%Y-%m").splitlines():
        counts[line.strip()] += 1
    return dict(counts)


def mechanism_dates() -> list[dict]:
    found = []
    for path, label, issue in MECHANISMS:
        follow = ["--follow"] if (ROOT / path).is_file() else []  # date renamed files by their origin
        lines = _run("git", "log", *follow, "--diff-filter=A", "--format=%ad", "--date=short", "--", path).split()
        if lines:
            found.append({"date": lines[-1], "label": label, "issue": issue, "path": path})
    return sorted(found, key=lambda m: m["date"])


def gh_json(*args: str) -> list[dict]:
    return json.loads(_run("gh", *args))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.check:
        return 0 if OUTPUT.is_file() else 1
    try:
        prs = gh_json("pr", "list", "--state", "merged", "--limit", "5000", "--json", "title,mergedAt,author")
        issues = gh_json("issue", "list", "--state", "all", "--limit", "5000", "--json", "createdAt,labels")
    except (subprocess.CalledProcessError, FileNotFoundError, json.JSONDecodeError) as exc:
        print(f"error: gh could not list PRs or issues ({exc}); snapshot not written", file=sys.stderr)
        return 1
    commits = commit_months()
    totals = {"commits": sum(commits.values()), "merged_prs": len(prs),
              "merged_prs_without_bots": sum(1 for p in prs if p["author"]["login"] not in BOTS),
              "issues": len(issues), "bug_issues": sum(bug_stats(issues).values())}
    data = snapshot(
        months=merge_months(commits, pr_stats(prs), bug_stats(issues), SINCE),
        mechanisms=mechanism_dates(),
        legacy_commits=sum(n for k, n in commits.items() if k < SINCE),
        totals=totals,
    )
    OUTPUT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {OUTPUT.relative_to(ROOT)}: {len(data['months'])} months, {len(data['mechanisms'])} mechanisms")
    return 0


if __name__ == "__main__":
    sys.exit(main())
