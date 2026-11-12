"""Server-derived write actor (channel + identity). Never trust request bodies (#2254)."""
from __future__ import annotations

import os

CHANNELS = ("cli", "ui", "agent")


def format_actor(channel: str, identity: str) -> str:
    channel = (channel or "cli").strip().casefold()
    identity = (identity or "local").strip() or "local"
    if channel == "agent":
        # agent:<id> is the full created_by value when channel is agent.
        if identity.startswith("agent:"):
            return identity
        return f"agent:{identity}"
    if channel not in CHANNELS:
        channel = "cli"
    return f"{channel}:{identity}"


def from_env(*, channel: str = "cli") -> str:
    identity = os.environ.get("AISDLC_ACTOR") or "local"
    return format_actor(channel, identity)


def from_token_identity(identity: str | None, scope: str | None) -> str:
    return format_actor("ui", identity or scope or "anonymous")
