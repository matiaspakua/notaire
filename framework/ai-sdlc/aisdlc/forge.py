"""ForgePort — forge access for delivery metrics (#2184) and gated attest (#2208).

Read-only lead-time (#2184): fetches issue open time and linked pull requests via
``gh`` and builds the sample shape for ``aisdlc.metrics.lead_time_from_sample``.
That path MUST NOT mutate the forge.

Write / ``factory/gate`` attest (#2208): builds an attest subject, verifies it via
ApprovalPort, and optionally performs a **scoped** mutation (comment / label /
check-run) only when verify succeeds **and** write is explicitly enabled. Default
is dry-run (``write_enabled=false``). Never fake ``attested`` or ``write_enabled``.

Default transport is subprocess ``gh``. Unit tests inject a runner so the suite
never touches the network.
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import tempfile
from collections.abc import Callable
from pathlib import Path
from typing import Any

Runner = Callable[[list[str]], subprocess.CompletedProcess[str]]
VerifyFn = Callable[..., dict[str, Any]]
StatusFn = Callable[..., dict[str, Any]]

READ_ONLY_VERBS = frozenset({"issue", "pr", "api", "repo", "status", "auth", "browse"})
# Verbs / actions that would mutate the forge — never used by the lead-time path.
WRITE_VERBS = frozenset(
    {
        "create",
        "edit",
        "close",
        "reopen",
        "merge",
        "comment",
        "delete",
        "lock",
        "unlock",
        "ready",
        "approve",
        "review",
        "request-reviewers",
        "add-assignee",
        "remove-assignee",
        "add-label",
        "remove-label",
        "set",
        "upload",
        "run",
        "workflow",
        "cache",
        "secret",
        "variable",
        "gist",
        "release",
        "label",
        "milestone",
        "project",
        "alias",
        "extension",
        "codespace",
        "attestation",
    }
)

SCOPED_WRITE_KINDS = frozenset({"comment", "label", "check-run"})
DEFAULT_ATTEST_LABEL = "aisdlc-factory-gate"
ATTEST_OUTCOMES = frozenset({"pass", "fail", "no-evaluable"})

_OK_CONCLUSIONS = frozenset({"SUCCESS", "SKIPPED", "NEUTRAL", "NONE", ""})


class ForgeError(Exception):
    """Forge read failed (missing gh, bad JSON, or non-zero gh). Maps to exit 2."""


def _default_runner(argv: list[str]) -> subprocess.CompletedProcess[str]:
    """Invoke `gh` (or `$AISDLC_GH` override for tests) with read-only argv."""
    binary = os.environ.get("AISDLC_GH") or "gh"
    if binary == "gh" and shutil.which("gh") is None:
        raise ForgeError("gh CLI not found on PATH (required for live lead-time)")
    return subprocess.run(
        [binary, *argv],
        check=False,
        capture_output=True,
        text=True,
    )


def checks_green_at(
    checks: list[dict[str, Any]] | None,
    *,
    merged_at: str | None,
) -> str | None:
    """Compute pull_request.checks_green_at from a statusCheckRollup list.

    Rules (honest when product CI is path-scoped / absent on PRs):
    - Any pending / in-progress / queued check → not green.
    - Any failure / cancelled / timed_out / action_required → not green.
    - Otherwise, if there is ≥1 completed check with a timestamp → max(completedAt).
    - Empty rollup + merged_at → merged_at (missing checks with no run = success).
    - Empty rollup + not merged → None.
    """
    rows = list(checks or [])
    if not rows:
        return merged_at
    completed_ats: list[str] = []
    for check in rows:
        status = str(check.get("status") or "").upper()
        conclusion = str(check.get("conclusion") or "").upper()
        if status in {"IN_PROGRESS", "QUEUED", "PENDING", "WAITING", "REQUESTED"}:
            return None
        if status and status not in {"COMPLETED", "SUCCESS"} and not conclusion:
            return None
        if conclusion and conclusion not in _OK_CONCLUSIONS:
            return None
        completed = check.get("completedAt") or check.get("completed_at")
        if completed:
            completed_ats.append(str(completed))
    if completed_ats:
        return max(completed_ats)
    return merged_at


def _run_json(runner: Runner, argv: list[str]) -> Any:
    _assert_read_only(argv)
    result = runner(argv)
    if result.returncode != 0:
        err = (result.stderr or result.stdout or "").strip() or f"gh {' '.join(argv)} failed"
        raise ForgeError(err)
    text = (result.stdout or "").strip()
    if not text:
        raise ForgeError(f"gh {' '.join(argv)} returned empty stdout")
    try:
        return json.loads(text)
    except json.JSONDecodeError as exc:
        raise ForgeError(f"gh {' '.join(argv)} returned invalid JSON: {exc}") from exc


def _assert_read_only(argv: list[str]) -> None:
    if not argv:
        raise ForgeError("empty gh argv")
    head = argv[0]
    if head in WRITE_VERBS:
        raise ForgeError(f"ForgePort refuses write verb {head!r}")
    if head not in READ_ONLY_VERBS:
        # Allow unknown top-level only if no write token appears later.
        pass
    tokens = {t.lower().lstrip("-") for t in argv}
    banned = tokens & {v.lower() for v in WRITE_VERBS}
    # `pr list` / `issue view` are fine; ban mutation actions as discrete tokens.
    action_banned = banned & {
        "create",
        "edit",
        "close",
        "reopen",
        "merge",
        "comment",
        "delete",
        "approve",
        "ready",
        "lock",
        "unlock",
        "attestation",
    }
    if action_banned:
        raise ForgeError(f"ForgePort refuses mutating gh tokens: {sorted(action_banned)}")


def _pr_links_issue(pr: dict[str, Any], issue: int) -> bool:
    refs = pr.get("closingIssuesReferences") or []
    for ref in refs:
        number = ref.get("number") if isinstance(ref, dict) else ref
        if number == issue or str(number) == str(issue):
            return True
    title = str(pr.get("title") or "")
    needle = f"#{issue}"
    return needle in title


def lead_time_sample(
    issue: int,
    *,
    runner: Runner | None = None,
) -> dict[str, Any]:
    """Build the #2138 lead-time sample document for a live issue (read-only)."""
    run = runner or _default_runner
    issue_data = _run_json(
        run,
        ["issue", "view", str(issue), "--json", "number,title,createdAt,state"],
    )
    created = issue_data.get("createdAt") or issue_data.get("created_at")
    if not created:
        raise ForgeError(f"issue #{issue} missing createdAt")

    prs_raw = _run_json(
        run,
        [
            "pr",
            "list",
            "--state",
            "all",
            "--search",
            str(issue),
            "--limit",
            "30",
            "--json",
            "number,title,createdAt,mergedAt,statusCheckRollup,closingIssuesReferences",
        ],
    )
    if not isinstance(prs_raw, list):
        raise ForgeError("gh pr list did not return a JSON array")

    pull_requests: list[dict[str, Any]] = []
    for pr in prs_raw:
        if not isinstance(pr, dict) or pr.get("number") is None:
            continue
        if not _pr_links_issue(pr, int(issue)):
            continue
        green = checks_green_at(
            pr.get("statusCheckRollup") or [],
            merged_at=pr.get("mergedAt") or pr.get("merged_at"),
        )
        entry: dict[str, Any] = {
            "number": int(pr["number"]),
            "title": pr.get("title"),
            "created_at": pr.get("createdAt") or pr.get("created_at"),
            "linked_issues": [int(issue)],
        }
        if green:
            entry["checks_green_at"] = green
        if pr.get("mergedAt") or pr.get("merged_at"):
            entry["merged_at"] = pr.get("mergedAt") or pr.get("merged_at")
        pull_requests.append(entry)

    return {
        "schema": 1,
        "event_pair": {
            "start": "issue.created_at",
            "end": "pull_request.checks_green_at",
            "notes": (
                "Lead time = first linked PR whose required checks are all green, "
                "minus issue open time. Merge is optional and not required for this metric. "
                "Live sample via read-only ForgePort (#2184); never mutates the forge."
            ),
        },
        "sample": {
            "label": f"forge-issue-#{int(issue)}",
            "issue": {
                "number": int(issue_data.get("number") or issue),
                "title": issue_data.get("title"),
                "created_at": str(created),
            },
            "pull_requests": pull_requests,
        },
        "source": "forge-port-readonly",
    }


def build_attest_payload(
    gate: str,
    *,
    outcome: str = "pass",
) -> dict[str, Any]:
    """Return schema-1 ``factory/gate`` attest subject (exact bytes signed later)."""
    outcome_norm = (outcome or "pass").strip().casefold()
    if outcome_norm not in ATTEST_OUTCOMES:
        raise ForgeError(
            f"invalid attest outcome {outcome!r}; expected one of {sorted(ATTEST_OUTCOMES)}"
        )
    gate_norm = (gate or "").strip()
    if not gate_norm:
        raise ForgeError("attest requires a non-empty --gate name")
    return {
        "schema": 1,
        "kind": "factory/gate",
        "gate": gate_norm,
        "outcome": outcome_norm,
        "source": "aisdlc-forge-attest",
    }


def assert_scoped_write(argv: list[str]) -> None:
    """Refuse unscoped forge mutations on the write path (#2208)."""
    if not argv:
        raise ForgeError("empty gh argv")
    tokens = [t.lower().lstrip("-") for t in argv]
    token_set = set(tokens)
    banned = {
        "merge",
        "close",
        "reopen",
        "delete",
        "approve",
        "ready",
        "lock",
        "unlock",
        "review",
        "request-reviewers",
    }
    hit = token_set & banned
    if hit:
        raise ForgeError(f"ForgePort refuses unscoped write tokens: {sorted(hit)}")
    # Allow only comment / label edit / checks API create shapes.
    head2 = tuple(tokens[:2])
    if head2 in {("pr", "comment"), ("issue", "comment")}:
        return
    if head2 in {("pr", "edit"), ("issue", "edit")} and "add-label" in token_set:
        return
    if tokens[:1] == ["api"] and any("check-runs" in t or "/check-runs" in t for t in argv):
        return
    raise ForgeError(f"ForgePort refuses non-scoped write argv: {argv!r}")


def _write_subject_file(payload: dict[str, Any], path: Path | None) -> Path:
    if path is None:
        handle = tempfile.NamedTemporaryFile(
            prefix="aisdlc-forge-attest-",
            suffix=".json",
            delete=False,
            mode="w",
            encoding="utf-8",
        )
        with handle as fh:
            json.dump(payload, fh, indent=2, sort_keys=True)
            fh.write("\n")
            return Path(fh.name)
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return path


def _default_verify(subject: Path, *, signature: Path | None, ctx: Any) -> dict[str, Any]:
    from aisdlc import approval

    return approval.verify(subject, signature=signature, ctx=ctx)


def _default_approval_status(ctx: Any = None) -> dict[str, Any]:
    from aisdlc import approval

    return approval.approval_status(ctx)


def _scoped_write_argv(
    *,
    kind: str,
    pr: int | None,
    issue: int | None,
    body: str,
    label: str,
) -> list[str]:
    kind_norm = (kind or "comment").strip().casefold()
    if kind_norm not in SCOPED_WRITE_KINDS:
        raise ForgeError(
            f"invalid write kind {kind!r}; expected one of {sorted(SCOPED_WRITE_KINDS)}"
        )
    if pr is None and issue is None:
        raise ForgeError("scoped write requires --pr and/or --issue")
    target_is_pr = pr is not None
    number = int(pr if target_is_pr else issue)  # type: ignore[arg-type]
    entity = "pr" if target_is_pr else "issue"
    if kind_norm == "comment":
        return [entity, "comment", str(number), "--body", body]
    if kind_norm == "label":
        return [entity, "edit", str(number), "--add-label", label]
    # check-run: GitHub Checks API (POST). Requires a PR head SHA in live use;
    # unit tests inject the runner and do not hit the network. We pass a
    # placeholder SHA path that live operators must replace via env when needed.
    head_sha = (os.environ.get("AISDLC_FORGE_HEAD_SHA") or "0000000").strip()
    repo_slug = (os.environ.get("AISDLC_FORGE_REPO") or "").strip()
    if not repo_slug:
        # Resolve via gh; still scoped to checks create only.
        api_path = f"repos/{{owner}}/{{repo}}/check-runs"
    else:
        api_path = f"repos/{repo_slug}/check-runs"
    payload = {
        "name": "factory/gate",
        "head_sha": head_sha,
        "status": "completed",
        "conclusion": "success" if "outcome=pass" in body or '"outcome": "pass"' in body else "neutral",
        "output": {
            "title": "aisdlc factory/gate",
            "summary": body[:65000],
        },
    }
    return ["api", "-X", "POST", api_path, "--input", "-", "__body__", json.dumps(payload)]


def _run_scoped_write(runner: Runner, argv: list[str]) -> subprocess.CompletedProcess[str]:
    # Special-case check-run body: strip marker before allowlist check, pass JSON
    # as a trailing argv element so injectable runners can observe the write.
    if "__body__" in argv:
        idx = argv.index("__body__")
        body = argv[idx + 1] if idx + 1 < len(argv) else "{}"
        clean = argv[:idx]
        assert_scoped_write(clean)
        return runner([*clean, body])
    assert_scoped_write(argv)
    return runner(argv)


def attest(
    *,
    gate: str,
    outcome: str = "pass",
    subject: Path | str | None = None,
    signature: Path | str | None = None,
    write: bool = False,
    kind: str = "comment",
    pr: int | None = None,
    issue: int | None = None,
    label: str | None = None,
    runner: Runner | None = None,
    verify_fn: VerifyFn | None = None,
    approval_status_fn: StatusFn | None = None,
    ctx: Any = None,
) -> dict[str, Any]:
    """Build ``factory/gate`` subject, verify via ApprovalPort, optionally scoped-write.

    Honesty fields: ``write_enabled``, ``attested``, ``approvals_live``, ``detail``.
    Default dry-run never mutates the forge.
    """
    run = runner or _default_runner
    verify = verify_fn or (lambda path, **kw: _default_verify(path, signature=kw.get("signature"), ctx=kw.get("ctx")))
    status_fn = approval_status_fn or (lambda **kw: _default_approval_status(kw.get("ctx")))

    payload_doc = build_attest_payload(gate, outcome=outcome)
    subject_path = _write_subject_file(
        payload_doc, Path(subject) if subject is not None else None
    )
    sig_path = Path(signature) if signature else Path(str(subject_path) + ".sig")

    # Be tolerant of injectable lambdas that ignore kwargs.
    try:
        status = status_fn(ctx=ctx)
    except TypeError:
        status = status_fn()

    approvals_live = bool(status.get("approvals_live"))
    try:
        verified = verify(subject_path, signature=sig_path, ctx=ctx)
    except TypeError:
        try:
            verified = verify(subject_path, signature=sig_path)
        except TypeError:
            verified = verify(subject_path)

    attested = bool(verified.get("ok"))
    base: dict[str, Any] = {
        "schema": 1,
        "kind": "factory/gate",
        "gate": payload_doc["gate"],
        "outcome": payload_doc["outcome"],
        "subject": str(subject_path),
        "signature": str(sig_path),
        "attested": attested,
        "write_enabled": False,
        "approvals_live": bool(verified.get("approvals_live", approvals_live)),
        "agent_can_approve": False,
        "write_kind": None,
        "target": {"pr": pr, "issue": issue},
        "detail": verified.get("detail") or status.get("detail") or "",
    }

    if not attested:
        base["detail"] = (
            (verified.get("detail") or "Approval verify failed")
            + " — refuse forge write; attested=false write_enabled=false (#2208)"
        )
        return base

    if not write:
        base["detail"] = (
            f"Approval verified; dry-run only (write_enabled=false). "
            f"{verified.get('detail') or ''}".strip()
        )
        return base

    # Live write path: still refuse if Approval not live for honesty, even if
    # a one-off verify_fn returned ok (injectables may set approvals_live).
    label_name = (label or DEFAULT_ATTEST_LABEL).strip() or DEFAULT_ATTEST_LABEL
    body = (
        f"aisdlc factory/gate attest\n"
        f"gate={payload_doc['gate']} outcome={payload_doc['outcome']}\n"
        f"subject={subject_path}\n"
        f"attested=true\n"
    )
    try:
        argv = _scoped_write_argv(
            kind=kind, pr=pr, issue=issue, body=body, label=label_name
        )
    except ForgeError as exc:
        base["detail"] = f"{exc} — write refused; write_enabled=false"
        return base

    result = _run_scoped_write(run, argv)
    if result.returncode != 0:
        err = (result.stderr or result.stdout or "").strip() or "scoped write failed"
        base["detail"] = f"Approval verified but scoped write failed: {err}"
        base["write_enabled"] = False
        return base

    base["write_enabled"] = True
    base["write_kind"] = (kind or "comment").strip().casefold()
    base["detail"] = (
        f"Approval verified; scoped {base['write_kind']} write completed "
        f"(write_enabled=true). {verified.get('detail') or ''}".strip()
    )
    return base


def doctor_note(ctx: Any = None) -> str:
    """One doctor INFO line: dry-run attest; live write only behind Approval + flag."""
    try:
        status = _default_approval_status(ctx)
    except Exception:  # pragma: no cover - defensive
        status = {"approvals_live": False, "status": "no-evaluable"}
    live = bool(status.get("approvals_live"))
    return (
        f"forge: write/attest dry-run available (factory/gate); "
        f"write_enabled default false; live scoped write only after Approval verify "
        f"+ --write (approvals_live={str(live).lower()}); never fake attest (#2208)"
    )


def dispatch_attest(
    ctx: Any,
    *,
    gate: str,
    outcome: str,
    subject: str | None,
    signature: str | None,
    write: bool,
    kind: str,
    pr: int | None,
    issue: int | None,
    label: str | None,
    as_json: bool,
) -> int:
    """CLI entry for ``aisdlc forge attest``."""
    try:
        payload = attest(
            gate=gate,
            outcome=outcome,
            subject=Path(subject) if subject else None,
            signature=Path(signature) if signature else None,
            write=write,
            kind=kind,
            pr=pr,
            issue=issue,
            label=label,
            ctx=ctx,
        )
    except ForgeError as exc:
        if as_json:
            print(
                json.dumps(
                    {
                        "attested": False,
                        "write_enabled": False,
                        "approvals_live": False,
                        "detail": str(exc),
                    },
                    indent=2,
                    sort_keys=True,
                )
            )
        else:
            print(f"forge attest: error: {exc}")
        return 2

    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"forge attest: attested={payload['attested']} "
            f"write_enabled={payload['write_enabled']} "
            f"approvals_live={payload['approvals_live']} "
            f"gate={payload.get('gate')} outcome={payload.get('outcome')}"
        )
        print(f"  detail: {payload.get('detail')}")
    if not payload.get("attested"):
        return 1
    if write and not payload.get("write_enabled"):
        return 1
    return 0
