#!/usr/bin/env python3
"""Unauthenticated DAST baseline for the local API (issue #1500, CU78).

Checks that the health endpoint answers and that a protected API path rejects
an anonymous caller without returning a stack trace. This is not a penetration
test. The weekly OWASP ZAP workflow remains the deeper CI DAST.
"""

from __future__ import annotations

import argparse
import re
import sys
import urllib.error
import urllib.request

HEALTH_PATH = "/actuator/health"
PROTECTED_PATH = "/api/v1/personas"
STACK_TRACE = re.compile(r"java\.lang\.\w*Exception|\bat com\.licensis\.notaire\.")


def fetch(url: str, timeout: float) -> tuple[int, str]:
    request = urllib.request.Request(url, method="GET")
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            body = response.read(65536).decode("utf-8", "replace")
            return response.status, body
    except urllib.error.HTTPError as error:
        body = error.read(65536).decode("utf-8", "replace")
        return error.code, body


def check_base(base_url: str, timeout: float = 5.0) -> list[str]:
    """Return failure messages. An empty list means the baseline passed."""
    base = base_url.rstrip("/")
    failures: list[str] = []
    try:
        health_status, health_body = fetch(base + HEALTH_PATH, timeout)
    except Exception as error:  # noqa: BLE001 — the gate reports the cause
        return [f"health request failed: {error.__class__.__name__}"]
    if health_status != 200:
        failures.append(f"{HEALTH_PATH} returned {health_status}")
    if STACK_TRACE.search(health_body):
        failures.append(f"{HEALTH_PATH} body contains a stack trace")

    try:
        status, body = fetch(base + PROTECTED_PATH, timeout)
    except Exception as error:  # noqa: BLE001
        failures.append(f"protected request failed: {error.__class__.__name__}")
        return failures
    if status not in (401, 403):
        failures.append(f"{PROTECTED_PATH} returned {status}, expected 401 or 403")
    if STACK_TRACE.search(body):
        failures.append(f"{PROTECTED_PATH} body contains a stack trace")
    return failures


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Run the unauthenticated DAST baseline")
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--timeout", type=float, default=5.0)
    args = parser.parse_args(argv)
    failures = check_base(args.base_url, args.timeout)
    if failures:
        for failure in failures:
            print(failure)
        return 1
    print(f"dast baseline passed for {args.base_url}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
