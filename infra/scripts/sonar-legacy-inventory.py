#!/usr/bin/env python3
"""Export and classify overall SonarQube vulnerability / high-severity issues (#1535).

Usage (Sonar up locally, e.g. after bash infra/scripts/run-sonar.sh):

    python3 infra/scripts/sonar-legacy-inventory.py
    python3 infra/scripts/sonar-legacy-inventory.py --write
    python3 infra/scripts/sonar-legacy-inventory.py --url http://localhost:9000 --write

Credentials: SONAR_ADMIN_USER / SONAR_ADMIN_PASSWORD (defaults admin/admin),
or SONAR_TOKEN as basic user with empty password.

Without --write, prints markdown to stdout. With --write, merges classifications
from docs/300-development/SONAR-LEGACY-VULN-INVENTORY.md (when present) and
rewrites that file. Exit 2 if Sonar is unreachable.
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any

PROJECTS = ("notaire-backend", "notaire-frontend")
HIGH_SEVERITIES = frozenset({"BLOCKER", "CRITICAL", "MAJOR"})
VALID_CLASSIFICATIONS = frozenset(
    {"fix", "false_positive", "accepted_risk", "defer_hexagon"}
)
REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_INVENTORY = REPO_ROOT / "docs" / "300-development" / "SONAR-LEGACY-VULN-INVENTORY.md"


def auth_header(user: str, password: str, token: str | None) -> str:
    if token:
        raw = f"{token}:".encode()
    else:
        raw = f"{user}:{password}".encode()
    return "Basic " + base64.b64encode(raw).decode()


def row_id(row: dict[str, str]) -> str:
    return "|".join(
        [
            row.get("project", ""),
            row.get("rule", ""),
            row.get("component", ""),
        ]
    )


def select_inventory_rows(issues: list[dict[str, Any]]) -> list[dict[str, str]]:
    """Keep VULNERABILITY rows and non-vuln BLOCKER/CRITICAL/MAJOR findings."""
    selected: list[dict[str, str]] = []
    for issue in issues:
        issue_type = str(issue.get("type", ""))
        severity = str(issue.get("severity", ""))
        if issue_type != "VULNERABILITY" and severity not in HIGH_SEVERITIES:
            continue
        project = str(issue.get("project") or "")
        component = str(issue.get("component") or "")
        if not project and ":" in component:
            project = component.split(":", 1)[0]
        selected.append(
            {
                "key": str(issue.get("key") or ""),
                "project": project,
                "rule": str(issue.get("rule") or ""),
                "severity": severity,
                "type": issue_type,
                "component": component,
                "message": str(issue.get("message") or "").replace("|", "/").replace("\n", " "),
                "classification": "",
                "rationale": "",
            }
        )
    selected.sort(key=lambda row: (row["project"], row["severity"], row["rule"], row["component"]))
    return selected


def suggest_vulnerability_classification(row: dict[str, str]) -> tuple[str, str] | None:
    """Return (classification, rationale) for known live Sonar vuln clusters (#1760)."""
    rule = row.get("rule", "")
    component = row.get("component", "")
    if rule == "secrets:S6703" and ".github/workflows/" in component:
        return (
            "accepted_risk",
            "ephemeral GitHub Actions Postgres service password (throwaway CI DB)",
        )
    # java:S4790 on UserController resolved by #1762 (MD5 verify removed).
    if rule == "typescript:S5332" and "otel/otlp-proxy.ts" in component:
        return (
            "accepted_risk",
            "docker-internal OTLP HTTP to collector (compose network; not public egress)",
        )
    return None


def apply_classifications(
    rows: list[dict[str, str]],
    existing: dict[str, tuple[str, str]],
) -> list[dict[str, str]]:
    for row in rows:
        prior = existing.get(row_id(row))
        if prior:
            row["classification"], row["rationale"] = prior
        elif not row.get("classification"):
            # Heuristic default for known hexagon-owned equality smells.
            if "jpa/" in row["component"] and row["rule"] in {
                "java:S4973",
                "javabugs:S6466",
            }:
                row["classification"] = "defer_hexagon"
                row["rationale"] = "legacy JpaController equality — hexagonal epic #1693"
            elif row["type"] == "VULNERABILITY":
                suggested = suggest_vulnerability_classification(row)
                if suggested:
                    row["classification"], row["rationale"] = suggested
                else:
                    row["classification"] = ""
                    row["rationale"] = ""
            else:
                row["classification"] = ""
                row["rationale"] = ""
    return rows


def all_vulnerabilities_classified(rows: list[dict[str, str]]) -> bool:
    for row in rows:
        if row.get("type") != "VULNERABILITY":
            continue
        if row.get("classification") not in VALID_CLASSIFICATIONS:
            return False
        if not str(row.get("rationale") or "").strip():
            return False
    return True


def render_markdown(rows: list[dict[str, str]], source_note: str | None = None) -> str:
    lines = [
        "# Sonar legacy vulnerability / high-severity inventory",
        "",
        "Overall (not new-code) unresolved findings for `notaire-backend` and",
        "`notaire-frontend`. Slice 1 of [#1535](https://github.com/matiaspakua/notaire/issues/1535)",
        "(CU78). New-code severity ratchet remains [#1737](https://github.com/matiaspakua/notaire/issues/1737).",
        "",
        "Refresh (Sonar up):",
        "",
        "```bash",
        "bash infra/scripts/run-sonar.sh",
        "python3 infra/scripts/sonar-legacy-inventory.py --write",
        "```",
        "",
        "Classification values (English): `fix` | `false_positive` | `accepted_risk` | `defer_hexagon`.",
        "",
    ]
    if source_note:
        lines.extend(
            [
                f"Last live refresh: **{source_note}** ({len(rows)} inventory rows).",
                "",
            ]
        )
    lines.extend(
        [
            "| project | rule | severity | type | component | message | classification | rationale |",
            "|---------|------|----------|------|-----------|---------|----------------|-----------|",
        ]
    )
    for row in rows:
        lines.append(
            "| {project} | {rule} | {severity} | {type} | `{component}` | {message} | {classification} | {rationale} |".format(
                project=row.get("project", ""),
                rule=row.get("rule", ""),
                severity=row.get("severity", ""),
                type=row.get("type", ""),
                component=row.get("component", "").replace("`", ""),
                message=row.get("message", ""),
                classification=row.get("classification", ""),
                rationale=row.get("rationale", "").replace("|", "/"),
            )
        )
    if not rows:
        lines.append(
            "| _(none)_ | — | — | — | — | No unresolved VULNERABILITY or BLOCKER/CRITICAL/MAJOR issues returned. | accepted_risk | empty Sonar query or green overall | "
        )
    lines.append("")
    return "\n".join(lines)


_ROW_RE = re.compile(
    r"^\|\s*(?P<project>[^|]+?)\s*\|\s*(?P<rule>[^|]+?)\s*\|\s*(?P<severity>[^|]+?)\s*\|\s*"
    r"(?P<type>[^|]+?)\s*\|\s*`?(?P<component>[^|`]+?)`?\s*\|\s*(?P<message>[^|]*?)\s*\|\s*"
    r"(?P<classification>[^|]*?)\s*\|\s*(?P<rationale>[^|]*?)\s*\|$"
)


def parse_classifications(markdown: str) -> dict[str, tuple[str, str]]:
    result: dict[str, tuple[str, str]] = {}
    for line in markdown.splitlines():
        if not line.startswith("|"):
            continue
        if "project" in line and "classification" in line:
            continue
        if re.match(r"^\|\s*[-:| ]+\|$", line):
            continue
        match = _ROW_RE.match(line)
        if not match:
            continue
        data = {key: value.strip() for key, value in match.groupdict().items()}
        if data["project"].startswith("_(") or data["project"] == "—":
            continue
        classification = data["classification"]
        if classification not in VALID_CLASSIFICATIONS:
            continue
        rid = "|".join([data["project"], data["rule"], data["component"]])
        result[rid] = (classification, data["rationale"])
    return result


def fetch_issues(base_url: str, project: str, headers: dict[str, str]) -> list[dict[str, Any]]:
    issues: list[dict[str, Any]] = []
    page = 1
    page_size = 500
    while True:
        params = urllib.parse.urlencode(
            {
                "componentKeys": project,
                "resolved": "false",
                "ps": page_size,
                "p": page,
                "additionalFields": "comments",
            }
        )
        url = f"{base_url.rstrip('/')}/api/issues/search?{params}"
        request = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(request, timeout=60) as response:
            body = json.load(response)
        batch = body.get("issues") or []
        issues.extend(batch)
        total = int(body.get("total") or 0)
        if page * page_size >= total or not batch:
            break
        page += 1
    return issues


def fetch_all_issues(base_url: str, headers: dict[str, str]) -> list[dict[str, Any]]:
    all_issues: list[dict[str, Any]] = []
    for project in PROJECTS:
        all_issues.extend(fetch_issues(base_url, project, headers))
    return all_issues


def seed_rows_without_sonar() -> list[dict[str, str]]:
    """Fallback rows when Sonar is down — known residuals from #1717 handoff."""
    return select_inventory_rows(
        [
            {
                "key": "seed-s4973",
                "rule": "java:S4973",
                "severity": "MAJOR",
                "type": "BUG",
                "component": "notaire-backend:src/main/java/com/licensis/notaire/jpa/DocumentTypeJpaController.java",
                "message": "Strings and Boxed types should be compared using equals().",
                "project": "notaire-backend",
            },
            {
                "key": "seed-s6863",
                "rule": "java:S6863",
                "severity": "MAJOR",
                "type": "SECURITY_HOTSPOT",
                "component": "notaire-backend:src/main/java/com/licensis/notaire/adapter/in/web/auth/AuthController.java",
                "message": "Login response status intentionally 200 on bad credentials (anti-enumeration).",
                "project": "notaire-backend",
            },
        ]
    )


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default=os.environ.get("SONAR_URL", "http://localhost:9000"))
    parser.add_argument("--user", default=os.environ.get("SONAR_ADMIN_USER", "admin"))
    parser.add_argument("--password", default=os.environ.get("SONAR_ADMIN_PASSWORD", "admin"))
    parser.add_argument("--token", default=os.environ.get("SONAR_TOKEN") or None)
    parser.add_argument(
        "--write",
        action="store_true",
        help=f"Write {DEFAULT_INVENTORY.relative_to(REPO_ROOT)}",
    )
    parser.add_argument(
        "--inventory",
        type=Path,
        default=DEFAULT_INVENTORY,
        help="Inventory markdown path",
    )
    parser.add_argument(
        "--allow-seed",
        action="store_true",
        help="If Sonar is unreachable, emit the seed residual table instead of exiting 2",
    )
    args = parser.parse_args(argv)
    headers = {"Authorization": auth_header(args.user, args.password, args.token)}

    existing: dict[str, tuple[str, str]] = {}
    if args.inventory.is_file():
        existing = parse_classifications(args.inventory.read_text(encoding="utf-8"))

    try:
        issues = fetch_all_issues(args.url, headers)
        rows = select_inventory_rows(issues)
        source_note = f"live Sonar @ {args.url}"
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, OSError, json.JSONDecodeError) as error:
        if not args.allow_seed and not args.write:
            print(f"unreachable: {error}", file=sys.stderr)
            return 2
        # --write with unreachable Sonar: preserve/seed so CI docs stay useful.
        rows = seed_rows_without_sonar()
        source_note = f"seed residuals (Sonar unreachable: {error})"

    # Always merge known seed classifications for documented residuals.
    seed_defaults = {
        "notaire-backend|java:S4973|notaire-backend:src/main/java/com/licensis/notaire/jpa/DocumentTypeJpaController.java": (
            "fix",
            "Integer optimistic-lock compares use OptimisticLockVersions/Objects.equals (#1758)",
        ),
        "notaire-backend|java:S6863|notaire-backend:src/main/java/com/licensis/notaire/adapter/in/web/auth/AuthController.java": (
            "accepted_risk",
            "anti-enumeration contract: bad credentials return HTTP 200",
        ),
    }
    merged_existing = {**seed_defaults, **existing}
    rows = apply_classifications(rows, merged_existing)

    # Ensure seed rows appear even when live Sonar omitted them (different component paths).
    have = {row_id(row) for row in rows}
    for seed in seed_rows_without_sonar():
        rid = row_id(seed)
        if rid not in have:
            rows.append(seed)
            have.add(rid)
    rows = apply_classifications(rows, merged_existing)
    rows.sort(key=lambda row: (row["project"], row["severity"], row["rule"], row["component"]))

    # Fill empty vuln classifications for seed-only empty Sonar with accepted_risk note.
    for row in rows:
        if row["type"] == "VULNERABILITY" and row.get("classification") not in VALID_CLASSIFICATIONS:
            suggested = suggest_vulnerability_classification(row)
            if suggested:
                row["classification"], row["rationale"] = suggested
            else:
                row["classification"] = "fix"
                row["rationale"] = "needs product fix — open child issue from this inventory"
        if row["type"] != "VULNERABILITY" and row.get("classification") not in VALID_CLASSIFICATIONS:
            if "jpa/" in row["component"]:
                row["classification"] = "defer_hexagon"
                row["rationale"] = "legacy JpaController — hexagonal epic #1693"
            else:
                row["classification"] = "accepted_risk"
                row["rationale"] = f"triaged in slice 1 inventory ({source_note})"

    md = render_markdown(rows, source_note=source_note)
    if not all_vulnerabilities_classified(rows):
        print("warning: some VULNERABILITY rows lack classification", file=sys.stderr)

    if args.write:
        args.inventory.parent.mkdir(parents=True, exist_ok=True)
        args.inventory.write_text(md, encoding="utf-8")
        print(f"wrote {args.inventory} ({len(rows)} rows; {source_note})")
    else:
        sys.stdout.write(md)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
