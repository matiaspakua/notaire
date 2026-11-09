"""Sole-committer dry-run (#2167): axiom 2 as a policy check, not an OS Isolator.

Reads committed history only. Never rewrites git, never writes a forge, never claims
sandbox isolation. Exit 1 only when the profile sets `[committer] required = true`.
"""
from __future__ import annotations

import fnmatch
import json
from typing import Any

from aisdlc import repo
from aisdlc.context import Context
from aisdlc.profile import CommitterPolicy


def _match(value: str, patterns: tuple[str, ...]) -> bool:
    folded = value.casefold()
    for pattern in patterns:
        if fnmatch.fnmatchcase(folded, pattern.casefold()):
            return True
    return False


def _is_runtime(commit: dict[str, str], policy: CommitterPolicy) -> bool:
    if not policy.runtime_names and not policy.runtime_emails:
        return False
    name_ok = (not policy.runtime_names) or _match(commit["author_name"], policy.runtime_names)
    email_ok = (not policy.runtime_emails) or _match(commit["author_email"], policy.runtime_emails)
    return name_ok and email_ok


def _agent_reasons(commit: dict[str, str], policy: CommitterPolicy) -> list[str]:
    reasons: list[str] = []
    if policy.agent_names and _match(commit["author_name"], policy.agent_names):
        reasons.append(f"author name matches agent pattern ({commit['author_name']})")
    if policy.agent_emails and _match(commit["author_email"], policy.agent_emails):
        reasons.append(f"author email matches agent pattern ({commit['author_email']})")
    body = commit.get("message") or ""
    for trailer in policy.agent_trailers:
        if trailer.casefold() in body.casefold():
            reasons.append(f"message contains agent trailer ({trailer})")
    return reasons


def evaluate(commits: list[dict[str, str]], policy: CommitterPolicy) -> dict[str, Any]:
    """Return status payload: pass | fail | no-evaluable."""
    base = {
        "required": policy.required,
        "commits_scanned": len(commits),
        "violations": [],
        "note": "dry-run policy check only — does not claim OS isolation",
    }
    if not policy.evaluable():
        return {**base, "status": "no-evaluable", "detail": "no [committer] patterns configured"}

    runtime_enforced = bool(policy.runtime_names or policy.runtime_emails)
    violations: list[dict[str, Any]] = []
    for commit in commits:
        reasons = _agent_reasons(commit, policy)
        if runtime_enforced and not _is_runtime(commit, policy) and not reasons:
            reasons.append(
                f"author not in runtime allowlist ({commit['author_name']} <{commit['author_email']}>)"
            )
        if reasons:
            violations.append(
                {
                    "sha": commit["sha"][:12],
                    "author": f"{commit['author_name']} <{commit['author_email']}>",
                    "reasons": reasons,
                }
            )
    if violations:
        return {**base, "status": "fail", "violations": violations}
    return {**base, "status": "pass", "violations": []}


def check(ctx: Context, base: str | None, as_json: bool) -> int:
    policy = ctx.profile.committer
    base = base or ctx.profile.base
    try:
        commits = repo.commits_since(ctx.root, base)
    except repo.GitError as exc:
        print(f"committer: FAILED — {exc}")
        return 2
    result = evaluate(commits, policy)
    result["base"] = base
    if as_json:
        print(json.dumps(result, indent=2, sort_keys=True))
    else:
        status = result["status"]
        print(f"committer: {status} ({result['commits_scanned']} commit(s) since {base})")
        print(f"  note: {result['note']}")
        for hit in result.get("violations") or []:
            print(f"  FAIL  {hit['sha']} {hit['author']}")
            for reason in hit["reasons"]:
                print(f"        {reason}")
        if status == "no-evaluable":
            print(f"  detail: {result.get('detail')}")
        elif status == "pass":
            print("committer: PASSED — no agent-authored commits matched policy")
        elif status == "fail" and not policy.required:
            print("committer: FAIL (advisory — set committer.required = true to fail the gate)")
        elif status == "fail":
            print("committer: FAILED — agent-authored commits found; runtime must own git commit")
    if result["status"] == "fail" and policy.required:
        return 1
    return 0
