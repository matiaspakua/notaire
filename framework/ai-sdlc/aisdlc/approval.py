"""ApprovalPort (#2190/#2205): cryptographic approval custody (mint + verify).

Authority comes from token + channel (configured identity + private key), never
via request-body role fields alone (V1 axioms 2 + 4 / F-02 / F-03 / ADR-005).
Custody uses OpenSSH SSHSIG via ``ssh-keygen -Y sign`` / ``-Y verify``.

``approvals_live`` is true **only** after a proven mint→verify round-trip with
resolved keys. Missing keys / missing ``ssh-keygen -Y`` / ``AISDLC_APPROVAL=0``
degrade to ``no-evaluable`` with ``approvals_live=false``. Agents never
self-approve: ``agent_can_approve`` is always false; mint refuses role=agent.

Local/dev keys via env or optional profile ``[approval]`` — never a production
HSM claim by themselves. ForgePort write/attest (#2208) consumes verify; this
module does not mutate the forge.
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Protocol

from aisdlc.context import Context

ENV_FORCE = "AISDLC_APPROVAL"
ENV_KEY = "AISDLC_APPROVAL_KEY"
ENV_ALLOWED = "AISDLC_APPROVAL_ALLOWED_SIGNERS"
ENV_IDENTITY = "AISDLC_APPROVAL_IDENTITY"
ENV_NAMESPACE = "AISDLC_APPROVAL_NAMESPACE"

DEFAULT_NAMESPACE = "file"
PERSON_ROLES = frozenset({"person", "admin"})
AGENT_ROLES = frozenset({"agent"})


class ApprovalPort(Protocol):
    """Boundary for cryptographic approval / custody runtime."""

    def status(self) -> dict[str, Any]: ...


@dataclass(frozen=True)
class ApprovalConfig:
    """Resolved local/dev custody paths (not production HSM)."""

    key: Path
    allowed_signers: Path
    identity: str
    namespace: str = DEFAULT_NAMESPACE


class UnevaluableApproval:
    """Default ApprovalPort when custody keys / tooling are unavailable."""

    kind: str = "unevaluable"

    def status(self) -> dict[str, Any]:
        return approval_status()


def _forced_off() -> bool:
    raw = (os.environ.get(ENV_FORCE) or "").strip().casefold()
    return raw in {"0", "false", "off", "no"}


def ssh_keygen_path() -> str | None:
    """Return ssh-keygen path when ``-Y`` signing is available."""
    binary = shutil.which("ssh-keygen")
    if not binary:
        return None
    # Probe -Y support without signing: unsupported builds exit non-zero on bad op.
    probe = subprocess.run(
        [binary, "-Y", "check-novalidate", "-n", "file", "-s", "/dev/null"],
        capture_output=True,
        text=True,
    )
    # OpenSSH with -Y parses the subcommand; older builds say "unknown option".
    combined = (probe.stderr or "") + (probe.stdout or "")
    if "unknown option" in combined.casefold() or "illegal option" in combined.casefold():
        return None
    return binary


def resolve_config(ctx: Context | None = None) -> ApprovalConfig | None:
    """Resolve key paths from env, then optional profile ``[approval]``."""
    key = (os.environ.get(ENV_KEY) or "").strip()
    allowed = (os.environ.get(ENV_ALLOWED) or "").strip()
    identity = (os.environ.get(ENV_IDENTITY) or "").strip()
    namespace = (os.environ.get(ENV_NAMESPACE) or "").strip() or DEFAULT_NAMESPACE

    if ctx is not None and hasattr(ctx, "profile"):
        settings = getattr(ctx.profile, "approval", None)
        if settings is not None:
            root = ctx.root
            if not key and settings.signing_key:
                key = str((root / settings.signing_key).resolve())
            if not allowed and settings.allowed_signers:
                allowed = str((root / settings.allowed_signers).resolve())
            if not identity and settings.identity:
                identity = settings.identity
            if settings.namespace:
                namespace = settings.namespace

    if not key or not allowed or not identity:
        return None
    key_path, allowed_path = Path(key), Path(allowed)
    if not key_path.is_file() or not allowed_path.is_file():
        return None
    return ApprovalConfig(
        key=key_path,
        allowed_signers=allowed_path,
        identity=identity,
        namespace=namespace or DEFAULT_NAMESPACE,
    )


def _key_algorithm(key_path: Path) -> str:
    binary = ssh_keygen_path()
    if not binary:
        return "ssh"
    result = subprocess.run(
        [binary, "-l", "-f", str(key_path)],
        capture_output=True,
        text=True,
    )
    text = (result.stdout or "").strip()
    # Example: "256 SHA256:... comment (ED25519)"
    if "(" in text and text.endswith(")"):
        algo = text[text.rfind("(") + 1 : -1].strip().casefold()
        if algo:
            return f"ssh-{algo}"
    return "ssh"


def _run_sign(binary: str, key: Path, namespace: str, subject: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [binary, "-Y", "sign", "-f", str(key), "-n", namespace, str(subject)],
        capture_output=True,
        text=True,
    )


def _run_verify(
    binary: str,
    allowed_signers: Path,
    identity: str,
    namespace: str,
    subject: Path,
    signature: Path,
) -> subprocess.CompletedProcess[str]:
    data = subject.read_bytes()
    return subprocess.run(
        [
            binary,
            "-Y",
            "verify",
            "-f",
            str(allowed_signers),
            "-I",
            identity,
            "-n",
            namespace,
            "-s",
            str(signature),
        ],
        input=data.decode("utf-8", errors="surrogateescape"),
        capture_output=True,
        text=True,
    )


def probe_live(cfg: ApprovalConfig | None = None) -> bool:
    """Return True only when mint then verify succeed on a temporary subject."""
    if _forced_off():
        return False
    binary = ssh_keygen_path()
    if not binary:
        return False
    cfg = cfg or resolve_config()
    if cfg is None:
        return False
    with tempfile.TemporaryDirectory(prefix="aisdlc-approval-probe-") as tmp:
        subject = Path(tmp) / "probe-subject.txt"
        subject.write_text(
            '{"kind":"aisdlc-approval-probe","channel":"cli"}\n',
            encoding="utf-8",
        )
        signed = _run_sign(binary, cfg.key, cfg.namespace, subject)
        if signed.returncode != 0:
            return False
        sig = Path(str(subject) + ".sig")
        if not sig.is_file():
            return False
        checked = _run_verify(
            binary, cfg.allowed_signers, cfg.identity, cfg.namespace, subject, sig
        )
        return checked.returncode == 0


def approval_status(ctx: Context | None = None) -> dict[str, Any]:
    """Report ApprovalPort readiness. Live only after a proven mint→verify probe."""
    base_detail_stub = (
        "no ApprovalPort custody runtime configured — SSH/DSSE (or equivalent) "
        "mint/verify and token+channel authority are not live; agents never "
        "self-approve; aisdlc improve validate is the policy tip ApprovalPort "
        "must eventually enforce (see ADR-005)"
    )
    if _forced_off():
        return {
            "status": "no-evaluable",
            "approvals_live": False,
            "agent_can_approve": False,
            "algorithm": None,
            "identity": None,
            "subject": None,
            "detail": (
                f"{ENV_FORCE} forced off — approvals_live=false; agents never "
                "self-approve (see ADR-005)"
            ),
        }

    cfg = resolve_config(ctx)
    binary = ssh_keygen_path()
    if cfg is None or binary is None:
        detail = base_detail_stub
        if cfg is None and binary is not None:
            detail = (
                "ApprovalPort keys/identity missing (set AISDLC_APPROVAL_* or "
                "[approval] local/dev paths) — not production HSM; agents never "
                "self-approve (see ADR-005)"
            )
        elif binary is None:
            detail = (
                "ssh-keygen -Y unavailable — ApprovalPort cannot mint/verify; "
                "approvals_live=false; agents never self-approve (see ADR-005)"
            )
        return {
            "status": "no-evaluable",
            "approvals_live": False,
            "agent_can_approve": False,
            "algorithm": None,
            "identity": None,
            "subject": None,
            "detail": detail,
        }

    live = probe_live(cfg)
    algorithm = _key_algorithm(cfg.key)
    if live:
        return {
            "status": "live",
            "approvals_live": True,
            "agent_can_approve": False,
            "algorithm": algorithm,
            "identity": cfg.identity,
            "subject": None,
            "detail": (
                "ApprovalPort mint/verify round-trip proven via ssh-keygen -Y "
                f"(algorithm={algorithm}, identity={cfg.identity}); local/dev "
                "keys only — not a production HSM claim; agents never self-approve"
            ),
        }
    return {
        "status": "no-evaluable",
        "approvals_live": False,
        "agent_can_approve": False,
        "algorithm": algorithm,
        "identity": cfg.identity,
        "subject": None,
        "detail": (
            "ApprovalPort keys present but mint/verify probe failed — "
            "approvals_live=false; agents never self-approve (see ADR-005)"
        ),
    }


def mint(
    subject: Path | str,
    *,
    role: str = "person",
    signature: Path | str | None = None,
    ctx: Context | None = None,
) -> dict[str, Any]:
    """Sign subject bytes. Refuses agent role. Returns ok + signature path."""
    role_norm = (role or "").strip().casefold()
    if role_norm in AGENT_ROLES or role_norm not in PERSON_ROLES:
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "error": "agent",
            "detail": (
                f"mint refuses role={role!r} — agents never self-approve; "
                "use role=person or role=admin with Owner/runtime key custody"
            ),
        }

    if _forced_off():
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": f"{ENV_FORCE} forced off — cannot mint",
        }

    binary = ssh_keygen_path()
    cfg = resolve_config(ctx)
    if binary is None or cfg is None:
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": "ApprovalPort keys or ssh-keygen -Y unavailable — cannot mint",
        }

    subject_path = Path(subject)
    if not subject_path.is_file():
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": f"subject not found: {subject_path}",
        }

    result = _run_sign(binary, cfg.key, cfg.namespace, subject_path)
    sig_path = Path(signature) if signature else Path(str(subject_path) + ".sig")
    default_sig = Path(str(subject_path) + ".sig")
    if result.returncode != 0 or not default_sig.is_file():
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": (result.stderr or result.stdout or "ssh-keygen -Y sign failed").strip(),
        }
    if sig_path != default_sig:
        sig_path.write_bytes(default_sig.read_bytes())

    algorithm = _key_algorithm(cfg.key)
    return {
        "ok": True,
        "approvals_live": True,
        "agent_can_approve": False,
        "algorithm": algorithm,
        "identity": cfg.identity,
        "subject": str(subject_path),
        "signature": str(sig_path),
        "role": role_norm,
        "detail": f"signed with {algorithm} as {cfg.identity} (role={role_norm})",
    }


def verify(
    subject: Path | str,
    *,
    signature: Path | str | None = None,
    ctx: Context | None = None,
) -> dict[str, Any]:
    """Verify an SSHSIG over subject bytes against allowed_signers."""
    if _forced_off():
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": f"{ENV_FORCE} forced off — cannot verify",
        }

    binary = ssh_keygen_path()
    cfg = resolve_config(ctx)
    if binary is None or cfg is None:
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": "ApprovalPort keys or ssh-keygen -Y unavailable — cannot verify",
        }

    subject_path = Path(subject)
    sig_path = Path(signature) if signature else Path(str(subject_path) + ".sig")
    if not subject_path.is_file() or not sig_path.is_file():
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "detail": f"subject or signature missing ({subject_path}, {sig_path})",
        }

    result = _run_verify(
        binary, cfg.allowed_signers, cfg.identity, cfg.namespace, subject_path, sig_path
    )
    algorithm = _key_algorithm(cfg.key)
    if result.returncode != 0:
        return {
            "ok": False,
            "approvals_live": False,
            "agent_can_approve": False,
            "algorithm": algorithm,
            "identity": cfg.identity,
            "subject": str(subject_path),
            "signature": str(sig_path),
            "detail": (result.stderr or result.stdout or "ssh-keygen -Y verify failed").strip(),
        }
    return {
        "ok": True,
        "approvals_live": True,
        "agent_can_approve": False,
        "algorithm": algorithm,
        "identity": cfg.identity,
        "subject": str(subject_path),
        "signature": str(sig_path),
        "detail": f"good {algorithm} signature for {cfg.identity}",
    }


def doctor_note(ctx: Context | None = None) -> str:
    """One doctor line: informational, never an error by itself."""
    payload = approval_status(ctx)
    return (
        f"approval: status={payload['status']} approvals_live={payload['approvals_live']} "
        f"agent_can_approve={payload['agent_can_approve']} — {payload['detail']}"
    )


def status_cmd(ctx: Context, *, as_json: bool) -> int:
    payload = approval_status(ctx)
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"approval status: status={payload['status']} "
            f"approvals_live={payload['approvals_live']} "
            f"agent_can_approve={payload['agent_can_approve']} "
            f"algorithm={payload.get('algorithm')} identity={payload.get('identity')}"
        )
        print(f"  detail: {payload['detail']}")
    return 0


def mint_cmd(
    ctx: Context,
    *,
    subject: str,
    role: str,
    signature: str | None,
    as_json: bool,
) -> int:
    payload = mint(Path(subject), role=role, signature=signature, ctx=ctx)
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"approval mint: ok={payload.get('ok')} algorithm={payload.get('algorithm')} "
            f"identity={payload.get('identity')} signature={payload.get('signature')}"
        )
        print(f"  detail: {payload.get('detail')}")
    return 0 if payload.get("ok") else 1


def verify_cmd(
    ctx: Context,
    *,
    subject: str,
    signature: str | None,
    as_json: bool,
) -> int:
    payload = verify(Path(subject), signature=signature, ctx=ctx)
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"approval verify: ok={payload.get('ok')} algorithm={payload.get('algorithm')} "
            f"identity={payload.get('identity')} subject={payload.get('subject')}"
        )
        print(f"  detail: {payload.get('detail')}")
    return 0 if payload.get("ok") else 1


def dispatch(
    ctx: Context,
    action: str,
    *,
    as_json: bool,
    subject: str | None = None,
    role: str = "person",
    signature: str | None = None,
) -> int:
    if action == "status":
        return status_cmd(ctx, as_json=as_json)
    if action == "mint":
        if not subject:
            print("error: approval mint requires --subject")
            return 2
        return mint_cmd(ctx, subject=subject, role=role, signature=signature, as_json=as_json)
    if action == "verify":
        if not subject:
            print("error: approval verify requires --subject")
            return 2
        return verify_cmd(ctx, subject=subject, signature=signature, as_json=as_json)
    print(f"error: unknown approval action {action!r}")
    return 2
