"""Bearer token file for ``aisdlc serve`` (#2253). Never log the plaintext secret."""
from __future__ import annotations

import os
import secrets
from dataclasses import dataclass
from pathlib import Path

from aisdlc.errors import KitError

TOKEN_REL = Path(".aisdlc") / "serve.token"
SCOPES = {"read": 1, "write": 2, "owner": 3}
DEFAULT_SCOPE = "owner"


@dataclass(frozen=True)
class ServeToken:
    value: str
    scope: str
    identity: str = ""

    def allows(self, need: str) -> bool:
        return SCOPES.get(self.scope, 0) >= SCOPES.get(need, 99)


def token_path(root: Path) -> Path:
    return root / TOKEN_REL


def load_token(root: Path) -> ServeToken | None:
    path = token_path(root)
    if not path.is_file():
        return None
    value = ""
    scope = DEFAULT_SCOPE
    identity = ""
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, _, rest = line.partition("=")
        key = key.strip().casefold()
        rest = rest.strip()
        if key == "token":
            value = rest
        elif key == "scope" and rest in SCOPES:
            scope = rest
        elif key == "identity":
            identity = rest
    if not value:
        return None
    return ServeToken(value=value, scope=scope, identity=identity or scope)


def ensure_token(root: Path, *, scope: str = DEFAULT_SCOPE) -> ServeToken:
    existing = load_token(root)
    if existing is not None:
        return existing
    if scope not in SCOPES:
        raise KitError("invalid_scope", f"unknown serve token scope {scope!r}")
    path = token_path(root)
    path.parent.mkdir(parents=True, exist_ok=True)
    value = secrets.token_urlsafe(32)
    path.write_text(f"token={value}\nscope={scope}\n", encoding="utf-8")
    os.chmod(path, 0o600)
    return ServeToken(value=value, scope=scope)


def parse_bearer(authorization: str | None) -> str | None:
    if not authorization:
        return None
    parts = authorization.split(None, 1)
    if len(parts) != 2 or parts[0].casefold() != "bearer" or not parts[1].strip():
        return None
    return parts[1].strip()


def require_token(
    authorization: str | None,
    expected: ServeToken,
    *,
    need: str = "read",
) -> None:
    presented = parse_bearer(authorization)
    if presented is None or not secrets.compare_digest(presented, expected.value):
        raise KitError("unauthorized", "missing or invalid bearer token")
    if not expected.allows(need):
        raise KitError("forbidden", f"token scope {expected.scope!r} cannot {need}")
