"""IsolatorPort (#2173/#2198/#2200): OS containment + sole-committer custody.

YAML, harness adapters, and profile flags are **not** isolation (ADR-002 / ADR-004).
`sandbox=true` only after a real containment probe succeeds (prefer Linux `bwrap`).
`enforces_committer=true` only after a proven interception probe (git commit without
grant fails closed). Commit custody uses `core.hooksPath` and works **without**
bwrap; agent sessions never receive `AISDLC_COMMIT_GRANT`.

Overrides:
- `AISDLC_BWRAP` — empty = force none; path = that binary
- `AISDLC_ISOLATOR_COMMITTER` — `0`/`false`/`off` forces enforces_committer=false
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import tempfile
import textwrap
from pathlib import Path
from typing import Any, Protocol, Sequence

from aisdlc.context import Context
from aisdlc.grants import active_grants, is_expired
from aisdlc.profile import Grant

ENV_BWRAP = "AISDLC_BWRAP"
ENV_COMMIT_GRANT = "AISDLC_COMMIT_GRANT"
ENV_COMMITTER = "AISDLC_ISOLATOR_COMMITTER"
HOOKS_REL = Path(".aisdlc") / "isolator" / "hooks"
PRE_COMMIT = "pre-commit"


class IsolatorPort(Protocol):
    """Boundary for OS sole-committer / containment runtime."""

    def status(self) -> dict[str, Any]: ...


class UnevaluableIsolator:
    """Default Isolator when no OS backend is available."""

    kind: str = "unevaluable"

    def status(self) -> dict[str, Any]:
        return isolator_status()


def bwrap_path() -> str | None:
    """Resolve bubblewrap binary, or None when unavailable / forced off."""
    override = os.environ.get(ENV_BWRAP)
    if override is not None:
        text = override.strip()
        if not text:
            return None
        return text if Path(text).is_file() or shutil.which(text) else None
    return shutil.which("bwrap")


def _committer_forced_off() -> bool:
    raw = (os.environ.get(ENV_COMMITTER) or "").strip().casefold()
    return raw in {"0", "false", "off", "no"}


def _run_bwrap_probe(binary: str) -> bool:
    """Return True when a trivial command runs under bwrap containment."""
    cmd = [
        binary,
        "--die-with-parent",
        "--ro-bind",
        "/",
        "/",
        "--dev",
        "/dev",
        "--proc",
        "/proc",
        "--tmpfs",
        "/tmp",
        "--",
        "true",
    ]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=15, check=False)
    except (OSError, subprocess.TimeoutExpired):
        return False
    return result.returncode == 0


def install_commit_custody(root: Path) -> Path:
    """Install Isolator pre-commit hook and point core.hooksPath at it.

    Returns the hooks directory. Does not claim OS sandbox.
    """
    root = root.resolve()
    hooks = root / HOOKS_REL
    hooks.mkdir(parents=True, exist_ok=True)
    hook = hooks / PRE_COMMIT
    hook.write_text(
        textwrap.dedent(
            f"""\
            #!/bin/sh
            # aisdlc Isolator sole-committer custody (ADR-004 / axiom 2).
            # Not OS isolation. Agent must not hold {ENV_COMMIT_GRANT}.
            if [ -z "${{{ENV_COMMIT_GRANT}:-}}" ]; then
              echo "aisdlc isolator: git commit blocked — set {ENV_COMMIT_GRANT} for Owner/runtime only" >&2
              exit 1
            fi
            exit 0
            """
        ),
        encoding="utf-8",
    )
    hook.chmod(0o755)
    subprocess.run(
        ["git", "config", "core.hooksPath", str(HOOKS_REL.as_posix())],
        cwd=root,
        check=True,
        capture_output=True,
        text=True,
    )
    return hooks


def probe_committer_enforcement() -> bool:
    """Prove git-commit interception: without grant fails; with grant may pass.

    Returns True only when a without-grant commit fails closed under custody hooks.
    Forced off via AISDLC_ISOLATOR_COMMITTER=0/false/off.
    """
    if _committer_forced_off():
        return False
    tmp = Path(tempfile.mkdtemp(prefix="aisdlc-isolator-commit-"))
    try:
        env = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}
        env.pop(ENV_COMMIT_GRANT, None)
        subprocess.run(
            ["git", "init", "-q", "-b", "main"],
            cwd=tmp,
            check=True,
            capture_output=True,
            text=True,
            env=env,
        )
        for key, value in (
            ("user.email", "isolator-probe@example.com"),
            ("user.name", "Isolator Probe"),
            ("commit.gpgsign", "false"),
        ):
            subprocess.run(
                ["git", "config", key, value],
                cwd=tmp,
                check=True,
                capture_output=True,
                text=True,
                env=env,
            )
        install_commit_custody(tmp)
        (tmp / "probe.txt").write_text("probe\n", encoding="utf-8")
        subprocess.run(
            ["git", "add", "-A"],
            cwd=tmp,
            check=True,
            capture_output=True,
            text=True,
            env=env,
        )
        denied = subprocess.run(
            ["git", "commit", "-q", "-m", "agent without grant"],
            cwd=tmp,
            capture_output=True,
            text=True,
            env=env,
        )
        if denied.returncode == 0:
            return False
        # Prove Owner/runtime path still works with the grant.
        allowed_env = {**env, ENV_COMMIT_GRANT: "1"}
        allowed = subprocess.run(
            ["git", "commit", "-q", "--allow-empty", "-m", "owner with grant"],
            cwd=tmp,
            capture_output=True,
            text=True,
            env=allowed_env,
        )
        return allowed.returncode == 0
    except (OSError, subprocess.SubprocessError):
        return False
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


def probe() -> dict[str, Any]:
    """Detect real containment + committer custody. Never fake honesty fields."""
    enforces = probe_committer_enforcement()
    binary = bwrap_path()
    if not binary:
        detail_parts = [
            "no OS Isolator backend available (bwrap missing or AISDLC_BWRAP empty) — "
            "YAML/harness flags are not isolation"
        ]
        if enforces:
            detail_parts.append(
                "sole-committer custody probe succeeded (git hooks; enforces_committer=true) "
                "without claiming sandbox"
            )
        else:
            detail_parts.append(
                "enforces_committer=false — commit interception not proven "
                "(or AISDLC_ISOLATOR_COMMITTER forced off)"
            )
        return {
            "status": "no-evaluable",
            "sandbox": False,
            "backend": "none",
            "probe_ok": False,
            "enforces_committer": enforces,
            "detail": "; ".join(detail_parts),
        }
    ok = _run_bwrap_probe(binary)
    if not ok:
        return {
            "status": "no-evaluable",
            "sandbox": False,
            "backend": "bwrap",
            "probe_ok": False,
            "enforces_committer": enforces,
            "detail": (
                "bwrap present but containment probe failed — "
                f"sandbox remains false; enforces_committer={str(enforces).lower()} (ADR-004 honesty)"
            ),
        }
    detail = (
        "bwrap containment probe succeeded — Isolator OS started for probe/run"
    )
    if enforces:
        detail += "; sole-committer custody proven (enforces_committer=true)"
    else:
        detail += "; still does not enforce sole-committer (enforces_committer=false)"
    return {
        "status": "ready",
        "sandbox": True,
        "backend": "bwrap",
        "probe_ok": True,
        "enforces_committer": enforces,
        "detail": detail,
    }


def isolator_status() -> dict[str, Any]:
    """Report Isolator readiness from a live probe (never fake sandbox/committer)."""
    payload = probe()
    return {
        "status": payload["status"],
        "sandbox": payload["sandbox"],
        "backend": payload["backend"],
        "enforces_committer": payload["enforces_committer"],
        "detail": payload["detail"],
    }


def doctor_note() -> str:
    """One doctor line: informational, never an error by itself."""
    payload = isolator_status()
    return (
        f"isolator: status={payload['status']} sandbox={payload['sandbox']} "
        f"backend={payload['backend']} enforces_committer={payload['enforces_committer']} "
        f"— {payload['detail']}"
    )


def _write_bind_targets(root: Path, grants: Sequence[Grant]) -> list[Path]:
    """Resolve Grant write_set globs to concrete directories/files under root for RW binds."""
    targets: list[Path] = []
    seen: set[Path] = set()
    for grant in grants:
        for pattern in grant.write_set:
            cleaned = pattern.strip().lstrip("./")
            if not cleaned:
                continue
            # Strip glob tails: docs/** → docs ; framework/ai-sdlc/*.py → framework/ai-sdlc
            for sep in ("/**", "/**/", "/*", "*"):
                if cleaned.endswith(sep):
                    cleaned = cleaned[: -len(sep)]
                    break
            cleaned = cleaned.rstrip("/")
            if not cleaned or any(ch in cleaned for ch in "*?["):
                # Unresolved glob — bind the first path segment when safe
                head = cleaned.split("/", 1)[0].rstrip("*?[")
                if not head:
                    continue
                cleaned = head
            path = (root / cleaned).resolve()
            try:
                path.relative_to(root.resolve())
            except ValueError:
                continue
            if path in seen:
                continue
            seen.add(path)
            targets.append(path)
    return targets


def _bwrap_run_command(
    binary: str,
    root: Path,
    argv: Sequence[str],
    write_paths: Sequence[Path],
) -> list[str]:
    cmd: list[str] = [
        binary,
        "--die-with-parent",
        "--ro-bind",
        "/",
        "/",
        "--dev",
        "/dev",
        "--proc",
        "/proc",
        "--tmpfs",
        "/tmp",
    ]
    for path in write_paths:
        if path.exists():
            cmd.extend(["--bind", str(path), str(path)])
        else:
            # Ensure parent exists in the sandbox view; create host dir for bind target
            path.mkdir(parents=True, exist_ok=True)
            cmd.extend(["--bind", str(path), str(path)])
    cmd.extend(["--chdir", str(root), "--", *argv])
    return cmd


def run_command(
    ctx: Context,
    argv: Sequence[str],
    *,
    as_json: bool = False,
) -> int:
    """Run argv under bwrap constrained by active Grant write_set, or refuse honestly."""
    if not argv:
        print("error: isolator run requires a command after --")
        return 2

    expired = [g.id for g in ctx.profile.grants if is_expired(g)]
    live = active_grants(ctx.profile.grants)
    if ctx.profile.grants and not live:
        payload = {
            "status": "fail",
            "sandbox": False,
            "backend": bwrap_path() and "bwrap" or "none",
            "enforces_committer": False,
            "expired_grants": expired,
            "detail": "all grants expired — fail closed (not a sandbox success)",
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"isolator run: FAILED — {payload['detail']}")
        return 1

    probed = probe()
    if not probed.get("sandbox"):
        payload = {
            "status": "fail",
            "sandbox": False,
            "backend": probed.get("backend", "none"),
            "enforces_committer": probed.get("enforces_committer", False),
            "detail": (
                "Isolator run refused — no working OS containment backend "
                f"({probed.get('detail')})"
            ),
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"isolator run: FAILED — {payload['detail']}")
        return 1

    binary = bwrap_path()
    if not binary:
        # Defensive: probe said sandbox but binary vanished
        print("isolator run: FAILED — bwrap unavailable after probe")
        return 1

    root = ctx.root.resolve()
    # Install commit custody in the worktree before contained run (agent never gets grant).
    try:
        install_commit_custody(root)
    except (OSError, subprocess.SubprocessError) as exc:
        payload = {
            "status": "fail",
            "sandbox": True,
            "backend": "bwrap",
            "enforces_committer": False,
            "detail": f"failed to install commit custody hooks: {exc}",
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"isolator run: FAILED — {payload['detail']}")
        return 1

    write_paths = _write_bind_targets(root, live) if live else []
    cmd = _bwrap_run_command(binary, root, argv, write_paths)
    child_env = {k: v for k, v in os.environ.items()}
    child_env.pop(ENV_COMMIT_GRANT, None)
    try:
        result = subprocess.run(cmd, cwd=root, check=False, env=child_env)
    except OSError as exc:
        payload = {
            "status": "fail",
            "sandbox": False,
            "backend": "bwrap",
            "enforces_committer": False,
            "detail": f"failed to execute bwrap: {exc}",
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"isolator run: FAILED — {payload['detail']}")
        return 1

    enforces = bool(probed.get("enforces_committer"))
    if as_json:
        print(
            json.dumps(
                {
                    "status": "pass" if result.returncode == 0 else "fail",
                    "sandbox": True,
                    "backend": "bwrap",
                    "enforces_committer": enforces,
                    "exit_code": result.returncode,
                    "write_binds": [str(p) for p in write_paths],
                    "detail": (
                        "command executed under bwrap containment constrained by Grant "
                        "write_set; commit grant unset for agent"
                    ),
                },
                indent=2,
                sort_keys=True,
            )
        )
    return result.returncode


def wrap_command(
    ctx: Context,
    argv: Sequence[str],
    *,
    as_json: bool = False,
) -> int:
    """Run argv with commit custody active; no commit grant. Does not require bwrap."""
    if not argv:
        print("error: isolator wrap requires a command after --")
        return 2

    root = ctx.root.resolve()
    try:
        install_commit_custody(root)
    except (OSError, subprocess.SubprocessError) as exc:
        payload = {
            "status": "fail",
            "sandbox": False,
            "backend": bwrap_path() and "bwrap" or "none",
            "enforces_committer": False,
            "detail": f"failed to install commit custody: {exc}",
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"isolator wrap: FAILED — {payload['detail']}")
        return 1

    child_env = {k: v for k, v in os.environ.items()}
    # Agent never receives the commit grant in wrap mode.
    child_env.pop(ENV_COMMIT_GRANT, None)
    # Empty string also blocks the hook.
    child_env[ENV_COMMIT_GRANT] = ""

    try:
        result = subprocess.run(list(argv), cwd=root, check=False, env=child_env)
    except OSError as exc:
        payload = {
            "status": "fail",
            "sandbox": False,
            "backend": "none",
            "enforces_committer": False,
            "detail": f"failed to execute wrap command: {exc}",
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"isolator wrap: FAILED — {payload['detail']}")
        return 1

    enforces = probe_committer_enforcement()
    if as_json:
        print(
            json.dumps(
                {
                    "status": "pass" if result.returncode == 0 else "fail",
                    "sandbox": False,
                    "backend": bwrap_path() and "bwrap" or "none",
                    "enforces_committer": enforces,
                    "exit_code": result.returncode,
                    "detail": (
                        "command ran under Isolator commit custody (hooks); "
                        "not OS sandbox unless isolator run/bwrap used"
                    ),
                },
                indent=2,
                sort_keys=True,
            )
        )
    return result.returncode


def status_cmd(*, as_json: bool) -> int:
    payload = isolator_status()
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"isolator status: status={payload['status']} sandbox={payload['sandbox']} "
            f"backend={payload['backend']} enforces_committer={payload['enforces_committer']}"
        )
        print(f"  detail: {payload['detail']}")
    return 0


def probe_cmd(*, as_json: bool) -> int:
    payload = probe()
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"isolator probe: status={payload['status']} sandbox={payload['sandbox']} "
            f"backend={payload['backend']} probe_ok={payload.get('probe_ok')} "
            f"enforces_committer={payload['enforces_committer']}"
        )
        print(f"  detail: {payload['detail']}")
    # Probe reporting is always evaluable as a report; exit 0 even when sandbox=false
    # so adopters can read honesty without treating missing bwrap as a hard tool error.
    # (run still refuses.)
    return 0


def dispatch(ctx: Context, action: str, *, as_json: bool, argv: Sequence[str]) -> int:
    if action == "status":
        return status_cmd(as_json=as_json)
    if action == "probe":
        return probe_cmd(as_json=as_json)
    if action == "run":
        return run_command(ctx, argv, as_json=as_json)
    if action == "wrap":
        return wrap_command(ctx, argv, as_json=as_json)
    print(f"error: unknown isolator action {action!r}")
    return 2
