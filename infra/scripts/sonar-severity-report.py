#!/usr/bin/env python3
"""Print SonarQube BLOCKER/CRITICAL/MAJOR issue counts for Notaire projects (#1737).

Usage (Sonar up locally, e.g. after bash infra/scripts/start-infra.sh):

    python3 infra/scripts/sonar-severity-report.py
    python3 infra/scripts/sonar-severity-report.py --url http://localhost:9000

Credentials: SONAR_ADMIN_USER / SONAR_ADMIN_PASSWORD (defaults admin/admin),
or SONAR_TOKEN as a Bearer-style basic user with empty password.

Exit 0 always when Sonar answers; exit 2 if unreachable. Does not fail on
non-zero counts — use run-sonar.sh quality gate for new-code enforcement.
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

PROJECTS = ("notaire-backend", "notaire-frontend")
SEVERITIES = ("BLOCKER", "CRITICAL", "MAJOR")


def auth_header(user: str, password: str, token: str | None) -> str:
    if token:
        raw = f"{token}:".encode()
    else:
        raw = f"{user}:{password}".encode()
    return "Basic " + base64.b64encode(raw).decode()


def fetch_counts(base_url: str, project: str, headers: dict[str, str]) -> dict[str, int]:
    params = urllib.parse.urlencode(
        {
            "componentKeys": project,
            "resolved": "false",
            "facets": "severities",
            "ps": 1,
        }
    )
    url = f"{base_url.rstrip('/')}/api/issues/search?{params}"
    request = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(request, timeout=20) as response:
        body = json.load(response)
    counts = {severity: 0 for severity in SEVERITIES}
    for facet in body.get("facets", []):
        if facet.get("property") != "severities":
            continue
        for value in facet.get("values", []):
            name = value.get("val")
            if name in counts:
                counts[name] = int(value.get("count", 0))
    return counts


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default=os.environ.get("SONAR_URL", "http://localhost:9000"))
    parser.add_argument("--user", default=os.environ.get("SONAR_ADMIN_USER", "admin"))
    parser.add_argument("--password", default=os.environ.get("SONAR_ADMIN_PASSWORD", "admin"))
    parser.add_argument("--token", default=os.environ.get("SONAR_TOKEN") or None)
    args = parser.parse_args(argv)
    headers = {"Authorization": auth_header(args.user, args.password, args.token)}
    print(f"Sonar severity report @ {args.url}")
    try:
        for project in PROJECTS:
            counts = fetch_counts(args.url, project, headers)
            parts = " ".join(f"{severity}={counts[severity]}" for severity in SEVERITIES)
            print(f"  {project}: {parts}")
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, OSError) as error:
        print(f"unreachable: {error}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
