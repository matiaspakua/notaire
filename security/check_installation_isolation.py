#!/usr/bin/env python3
"""Verifiable per-installation isolation checklist (ADR-029 W3 / #1684, CU-78).

Fails when JWT signing secret, application admin password, or actuator credentials
are missing or match documented repository defaults (.env.example, JwtTokenService,
ProductionCredentialsGuard).

Usage:
  python3 security/check_installation_isolation.py [--env-file PATH]

Reads the process environment plus optional ``KEY=VALUE`` lines from ``--env-file``.
Exit 0 when every check passes; exit 1 on violations.
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

# Documented defaults — keep aligned with .env.example and backend guards.
INSECURE_JWT_SECRETS = frozenset(
    {
        "notaire-default-secret-key-change-in-production-!!",
        "change-this-to-a-random-secret-of-at-least-32-bytes",
    }
)
MIN_JWT_SECRET_BYTES = 32

INSECURE_ADMIN_PASSWORDS = frozenset({"admin"})

WEAK_ACTUATOR_USERS = frozenset({"admin", "root"})
WEAK_ACTUATOR_PASSWORDS = frozenset(
    {"admin", "password", "changeme", "secret", "root", "change-me-actuator"}
)
MIN_ACTUATOR_PASSWORD_LENGTH = 16
PLACEHOLDER_PREFIXES = ("change-me", "changeme")


def load_env_file(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip().strip('"').strip("'")
    return values


def is_blank(value: str | None) -> bool:
    return value is None or not str(value).strip()


def has_placeholder_prefix(value: str) -> bool:
    lowered = value.lower()
    return any(lowered.startswith(prefix) for prefix in PLACEHOLDER_PREFIXES)


def collect_violations(env: dict[str, str]) -> list[str]:
    violations: list[str] = []

    jwt_secret = env.get("JWT_SECRET", "")
    if is_blank(jwt_secret):
        violations.append("JWT_SECRET is missing or blank")
    elif jwt_secret in INSECURE_JWT_SECRETS:
        violations.append("JWT_SECRET matches a documented default placeholder")
    elif len(jwt_secret.encode("utf-8")) < MIN_JWT_SECRET_BYTES:
        violations.append(
            f"JWT_SECRET is shorter than {MIN_JWT_SECRET_BYTES} bytes (HS256 minimum)"
        )

    admin_password = env.get("APP_ADMIN_PASSWORD", "")
    if is_blank(admin_password):
        violations.append("APP_ADMIN_PASSWORD is missing or blank")
    elif admin_password in INSECURE_ADMIN_PASSWORDS:
        violations.append("APP_ADMIN_PASSWORD uses the documented default 'admin'")

    actuator_user = env.get("ACTUATOR_USER", "")
    if is_blank(actuator_user) or actuator_user.lower() in WEAK_ACTUATOR_USERS:
        violations.append("ACTUATOR_USER is missing or uses a documented weak default")

    actuator_password = env.get("ACTUATOR_PASSWORD", "")
    if is_blank(actuator_password):
        violations.append("ACTUATOR_PASSWORD is missing or blank")
    elif (
        actuator_password.lower() in WEAK_ACTUATOR_PASSWORDS
        or len(actuator_password) < MIN_ACTUATOR_PASSWORD_LENGTH
        or has_placeholder_prefix(actuator_password)
    ):
        violations.append(
            "ACTUATOR_PASSWORD is a documented default, placeholder, or shorter than "
            f"{MIN_ACTUATOR_PASSWORD_LENGTH} characters"
        )

    return violations


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="ADR-029 W3 installation isolation checklist")
    parser.add_argument(
        "--env-file",
        type=Path,
        help="Optional KEY=VALUE file (for example .env) merged over the process environment",
    )
    args = parser.parse_args(argv)

    env = dict(os.environ)
    if args.env_file is not None:
        if not args.env_file.is_file():
            print(f"env file not found: {args.env_file}", file=sys.stderr)
            return 2
        env.update(load_env_file(args.env_file))

    violations = collect_violations(env)
    if violations:
        print("Installation isolation checklist FAILED (ADR-029 W3 / #1684):", file=sys.stderr)
        for item in violations:
            print(f"  - {item}", file=sys.stderr)
        return 1

    print("Installation isolation checklist passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
