"""Bearer tokens for ``aisdlc serve`` (#2253, #2384). Never log the plaintext secret.

``.aisdlc/serve.token`` holds the owner token (``token=``, ``scope=``, ``identity=`` lines).
``.aisdlc/serve.tokens`` holds further named tokens, one per line
(``identity=alice scope=write token=...``), so several people act through one host, each with their own
scope and recorded as their own actor. Both files are mode 0600; listings never include a secret.
"""
from __future__ import annotations

import os
import re
import secrets
from dataclasses import dataclass
from pathlib import Path

from aisdlc.errors import KitError

TOKEN_REL = Path(".aisdlc") / "serve.token"
TOKENS_REL = Path(".aisdlc") / "serve.tokens"
IDENTITY = re.compile(r"^[a-z0-9][a-z0-9._-]{0,63}$")
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


def tokens_path(root: Path) -> Path:
    return root / TOKENS_REL


def _read_named(root: Path) -> list[ServeToken]:
    path = tokens_path(root)
    if not path.is_file():
        return []
    out: list[ServeToken] = []
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        fields = dict(part.partition("=")[::2] for part in line.split() if "=" in part)
        value, scope, identity = fields.get("token", ""), fields.get("scope", ""), fields.get("identity", "")
        if value and scope in SCOPES and IDENTITY.match(identity):
            out.append(ServeToken(value=value, scope=scope, identity=identity))
    return out


def load_tokens(root: Path) -> list[ServeToken]:
    """Every token the host accepts: the owner token first, then the named ones."""
    owner = load_token(root)
    return ([owner] if owner is not None else []) + _read_named(root)


def _write_named(root: Path, tokens: list[ServeToken]) -> None:
    path = tokens_path(root)
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = ["# aisdlc serve named tokens (#2384): identity scope token. Keep private (mode 0600)."]
    lines += [f"identity={t.identity} scope={t.scope} token={t.value}" for t in tokens]
    tmp = path.with_suffix(".tmp")
    tmp.write_text("\n".join(lines) + "\n", encoding="utf-8")
    os.chmod(tmp, 0o600)
    os.replace(tmp, path)


def add_token(root: Path, *, identity: str, scope: str) -> ServeToken:
    """Create a named token. The returned value is the only time the secret is available."""
    if not IDENTITY.match(identity or ""):
        raise KitError("invalid_id", f"token identity must match {IDENTITY.pattern}")
    if scope not in SCOPES:
        raise KitError("invalid_scope", f"unknown serve token scope {scope!r}")
    owner = load_token(root)
    named = _read_named(root)
    taken = {t.identity for t in named} | ({owner.identity} if owner is not None else set())
    if identity in taken:
        raise KitError("conflict", f"a token for {identity!r} already exists; revoke it first")
    token = ServeToken(value=secrets.token_urlsafe(32), scope=scope, identity=identity)
    _write_named(root, [*named, token])
    return token


def revoke_token(root: Path, identity: str) -> bool:
    named = _read_named(root)
    kept = [t for t in named if t.identity != identity]
    if len(kept) == len(named):
        return False
    _write_named(root, kept)
    return True


def list_tokens(root: Path) -> list[dict]:
    """Identities and scopes only: never the secret."""
    owner = load_token(root)
    out = [{"identity": owner.identity, "scope": owner.scope, "source": "serve.token"}] if owner else []
    return out + [{"identity": t.identity, "scope": t.scope, "source": "serve.tokens"} for t in _read_named(root)]


def authenticate(authorization: str | None, tokens: list[ServeToken], *, need: str = "read") -> ServeToken:
    """The token the request presents, if the host accepts it and its scope allows ``need``."""
    presented = parse_bearer(authorization)
    match = None
    if presented is not None:
        for token in tokens:  # compare every candidate: no early exit that leaks which one matched
            if secrets.compare_digest(presented, token.value) and match is None:
                match = token
    if match is None:
        raise KitError("unauthorized", "missing or invalid bearer token")
    if not match.allows(need):
        raise KitError("forbidden", f"token scope {match.scope!r} cannot {need}; {need} scope required")
    return match

