#!/usr/bin/env python3
"""Generate github-page/lib/delivery-metrics.json: delivery outcome numbers for the Pages site (#1985).

WHY
---
The portfolio review (#1977) asked for outcomes, not just activity: how long a change takes to reach
main, how often it breaks main and how fast main recovers. Every number here is computed from the
GitHub API for a fixed window, and the snapshot records the window and the method so the site can
print them next to the values. Nothing is estimated or typed by hand.

The snapshot is committed so a local `npm run build` works offline. deploy-github-page.yml re-runs
this script with the default GITHUB_TOKEN before every Pages build, so the published site is never
older than the last deploy (no commit to main is needed for that).

USAGE
  python3 workspace/ci/generate-delivery-metrics.py              # needs an authenticated gh
  python3 workspace/ci/generate-delivery-metrics.py --days 90    # window length (default 90)
  python3 workspace/ci/generate-delivery-metrics.py --check      # exit 1 when the snapshot is invalid

Fails without writing when gh cannot read PRs, runs, issues or releases: a partial snapshot would mislead.
"""
from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
import statistics
import subprocess
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "github-page" / "lib" / "delivery-metrics.json"
CI_WORKFLOW = "ci.yml"  # "CI - Build, Test & Security": the merge gate on every PR and on main
FAILURE_TITLE = re.compile(r"^\s*revert\b|\bhotfix\b", re.I)
REQUIRED = ("generated_at", "window", "method", "lead_time_hours", "throughput", "change_failure",
            "restore_main_hours", "first_push_ci", "issues_closed", "releases")


def gh_json(*args: str):
    out = subprocess.run(["gh", *args], check=True, capture_output=True, text=True).stdout
    return json.loads(out) if out.strip() else []


def repo() -> str:
    return os.environ.get("GH_REPO") or "matiaspakua/notaire"


def parse(ts: str) -> dt.datetime:
    return dt.datetime.fromisoformat(ts.replace("Z", "+00:00"))


def hours(a: str, b: str) -> float:
    return (parse(b) - parse(a)).total_seconds() / 3600


def percentile(values: list[float], p: float) -> float | None:
    """Nearest-rank percentile; None for an empty sample (the site prints "n/a", never 0)."""
    if not values:
        return None
    ordered = sorted(values)
    rank = max(1, -(-len(ordered) * p // 100))  # ceil
    return round(ordered[int(rank) - 1], 1)


def summary(values: list[float]) -> dict:
    return {"n": len(values),
            "median": round(statistics.median(values), 1) if values else None,
            "p90": percentile(values, 90)}


def is_bot(pr: dict) -> bool:
    author = pr.get("author") or {}
    return bool(author.get("is_bot")) or "[bot]" in (author.get("login") or "") \
        or (author.get("login") or "").startswith("app/")


def lead_time_and_throughput(prs: list[dict], start: dt.date, end: dt.date) -> tuple[dict, dict, dict]:
    humans = [p for p in prs if not is_bot(p)]
    lead = summary([hours(p["createdAt"], p["mergedAt"]) for p in humans])
    weeks: Counter[str] = Counter()
    day = start - dt.timedelta(days=start.weekday())
    while day <= end:  # every ISO week in the window, so quiet weeks count as 0
        iso = day.isocalendar()
        weeks[f"{iso[0]}-W{iso[1]:02d}"] = 0
        day += dt.timedelta(days=7)
    for p in humans:
        iso = parse(p["mergedAt"]).isocalendar()
        weeks[f"{iso[0]}-W{iso[1]:02d}"] += 1
    series = [{"week": w, "merged": n} for w, n in sorted(weeks.items())]
    counts = [s["merged"] for s in series]
    throughput = {"merged_prs": len(humans), "bot_prs_excluded": len(prs) - len(humans),
                  "weeks": len(series), "median_per_week": statistics.median(counts) if counts else None,
                  "series": series}
    failures = [p for p in humans if FAILURE_TITLE.search(p["title"])]
    change_failure = {"failure_prs": len(failures), "merged_prs": len(humans),
                      "rate_pct": round(100 * len(failures) / len(humans), 1) if humans else None,
                      "examples": [f"#{p['number']}" for p in failures[:10]]}
    return lead, throughput, change_failure


def workflow_runs(event: str, since: str, branch: str | None = None) -> list[dict]:
    query = f"event={event}&created=%3E%3D{since}&per_page=100"
    if branch:
        query += f"&branch={branch}"
    pages = gh_json("api", "--paginate", "--slurp",
                    f"repos/{repo()}/actions/workflows/{CI_WORKFLOW}/runs?{query}")
    return [r for page in pages for r in page.get("workflow_runs", [])]


def restore_main(runs: list[dict]) -> dict:
    """Red-to-green on main: from the first failing CI run's end to the next successful run's end."""
    done = sorted((r for r in runs if r["status"] == "completed" and r["conclusion"] in ("success", "failure")),
                  key=lambda r: r["created_at"])
    episodes: list[float] = []
    red_since: str | None = None
    for r in done:
        if r["conclusion"] == "failure" and red_since is None:
            red_since = r["updated_at"]
        elif r["conclusion"] == "success" and red_since is not None:
            episodes.append(hours(red_since, r["updated_at"]))
            red_since = None
    out = summary(episodes)
    out.update({"red_episodes": len(episodes), "main_runs": len(done), "still_red": red_since is not None})
    return out


def first_push_ci(runs: list[dict], merged: set[int]) -> dict:
    """For each merged PR, did the first CI run of its first pushed commit succeed?"""
    first: dict[int, dict] = {}
    for r in sorted(runs, key=lambda r: (r["created_at"], r.get("run_attempt", 1))):
        for pr in r.get("pull_requests") or []:
            if pr["number"] in merged and pr["number"] not in first:
                first[pr["number"]] = r
    judged = [r for r in first.values() if r["conclusion"] in ("success", "failure")]
    passed = sum(1 for r in judged if r["conclusion"] == "success")
    return {"prs_with_ci": len(first), "judged": len(judged), "passed": passed,
            "rate_pct": round(100 * passed / len(judged), 1) if judged else None}


def history_gap(runs: list[dict], since: str) -> str | None:
    """Why run-based metrics cannot cover the window, or None when they can.

    leanup-actions.yml deletes every completed run but the latest per workflow each night, and the
    check runs go with them, so older CI outcomes are unrecoverable. A metric computed from the few
    surviving runs would look like a 90-day figure while describing a day or two, so it is withheld.
    """
    earliest = min((r["created_at"][:10] for r in runs), default=None)
    limit = (dt.date.fromisoformat(since) + dt.timedelta(days=7)).isoformat()
    if earliest is None or earliest > limit:
        return (f"Not derivable: .github/workflows/leanup-actions.yml deletes all but the latest {CI_WORKFLOW} "
                f"run every night, so only runs since {earliest or 'none'} remain for a window that starts {since}.")
    return None


def withheld(computed: dict, reason: str | None) -> dict:
    if reason is None:
        return {"available": True, **computed}
    return {"available": False, "reason": reason}


def build(days: int) -> dict:
    now = dt.datetime.now(dt.timezone.utc).replace(microsecond=0)
    start = (now - dt.timedelta(days=days)).date()
    end = now.date()
    since = start.isoformat()
    prs = gh_json("pr", "list", "-R", repo(), "--state", "merged", "--limit", "2000",
                  "--search", f"merged:>={since}", "--json", "number,title,createdAt,mergedAt,author")
    lead, throughput, change_failure = lead_time_and_throughput(prs, start, end)
    main_runs = workflow_runs("push", since, "main")
    pr_runs = workflow_runs("pull_request", since)
    issues = gh_json("api", "search/issues", "-X", "GET", "-f",
                     f"q=repo:{repo()} is:issue is:closed reason:completed closed:>={since}",
                     "-f", "per_page=1")
    releases = gh_json("api", "--paginate", "--slurp", f"repos/{repo()}/releases?per_page=100")
    in_window = [r for page in releases for r in page
                 if not r["draft"] and r.get("published_at") and r["published_at"][:10] >= since]
    in_window.sort(key=lambda r: r["published_at"])
    return {
        "generated_at": now.isoformat(),
        "generated_by": "python3 workspace/ci/generate-delivery-metrics.py",
        "repository": repo(),
        "window": {"from": since, "to": end.isoformat(), "days": days},
        "method": {
            "lead_time": "Merged PRs in the window, opened to merged, in hours. Bot PRs (Dependabot) excluded. "
                         "Median and nearest-rank p90.",
            "throughput": "Merged non-bot PRs per ISO week; weeks with no merge count as 0.",
            "change_failure": "Proxy: merged PRs whose title starts with 'Revert' or contains 'hotfix', "
                              "as a share of merged non-bot PRs.",
            "restore_main": f"Runs of {CI_WORKFLOW} on pushes to main. A red episode starts when a run fails "
                            "while main is green and ends when the next run succeeds; time between the two run "
                            "ends, in hours. Cancelled runs are ignored.",
            "first_push_ci": f"For each merged PR, the earliest {CI_WORKFLOW} run triggered by the PR; it passes "
                             "if that run succeeded. Cancelled or skipped first runs are not judged.",
            "issues_closed": "Issues closed as completed in the window (GitHub search).",
            "releases": "Published, non-draft GitHub releases in the window.",
        },
        "lead_time_hours": lead,
        "throughput": throughput,
        "change_failure": change_failure,
        "restore_main_hours": withheld(restore_main(main_runs), history_gap(main_runs, since)),
        "first_push_ci": withheld(first_push_ci(pr_runs, {p["number"] for p in prs}), history_gap(pr_runs, since)),
        "issues_closed": issues["total_count"],
        "releases": {"count": len(in_window),
                     "tags": [{"tag": r["tag_name"], "published": r["published_at"][:10]} for r in in_window]},
    }


def check() -> int:
    try:
        data = json.loads(OUTPUT.read_text())
    except (OSError, ValueError) as exc:
        print(f"delivery-metrics: unreadable {OUTPUT}: {exc}", file=sys.stderr)
        return 1
    missing = [k for k in REQUIRED if k not in data]
    if missing:
        print(f"delivery-metrics: missing keys {missing}", file=sys.stderr)
        return 1
    print(f"delivery-metrics: OK ({data['window']['from']} → {data['window']['to']})")
    return 0


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--days", type=int, default=90)
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()
    if args.check:
        return check()
    try:
        data = build(args.days)
    except (subprocess.CalledProcessError, KeyError, ValueError) as exc:
        detail = getattr(exc, "stderr", "") or exc
        print(f"delivery-metrics: GitHub read failed, snapshot not written: {detail}", file=sys.stderr)
        return 1
    OUTPUT.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    print(f"delivery-metrics: wrote {OUTPUT.relative_to(ROOT)} ({data['window']['from']} → {data['window']['to']})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
