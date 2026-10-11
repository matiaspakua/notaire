#!/usr/bin/env python3
"""Run the security case catalog locally or in CI and write a report.

Issue #1500, CU78. The catalog is the security test schema. The local profile
is the pre-PR gate (`workspace/sdlc/run_pipeline.sh` and `preflight.sh`). The
CI profile runs the same cases and checks the deeper CodeQL and ZAP workflows.
"""

from __future__ import annotations

import argparse
import html
import importlib.util
import json
import os
import shutil
import subprocess
import sys
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[1]
CATALOG_PATH = REPO_ROOT / "security" / "cases" / "catalog.yml"
POLICY_PATH = REPO_ROOT / "security" / "policy" / "security-policy.yml"
SCANNER_PATH = REPO_ROOT / "security" / "scan_secrets.py"
DEFAULT_REPORT = REPO_ROOT / "reports" / "security" / "latest"
SEMGREP_ENV = {"SEMGREP_SEND_METRICS": "off"}


@dataclass
class CaseResult:
    id: str
    family: str
    status: str
    blocking: bool
    tool: str
    ci_tool: str
    detail: str


def load_catalog(path: Path = CATALOG_PATH) -> dict:
    return yaml.safe_load(path.read_text(encoding="utf-8"))


def load_scanner():
    spec = importlib.util.spec_from_file_location("scan_secrets", SCANNER_PATH)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {SCANNER_PATH}")
    module = importlib.util.module_from_spec(spec)
    sys.modules["scan_secrets"] = module
    spec.loader.exec_module(module)
    return module


def _result(case: dict, status: str, detail: str) -> CaseResult:
    return CaseResult(
        id=case["id"],
        family=case["family"],
        status=status,
        blocking=bool(case["blocking"]),
        tool=str(case["local_tool"]),
        ci_tool=str(case["ci_tool"]),
        detail=detail,
    )


def _case(case_id: str, family: str, blocking: bool = True) -> dict:
    return {
        "id": case_id,
        "family": family,
        "blocking": blocking,
        "local_tool": "",
        "ci_tool": "",
    }


def assess_evidence(repo: Path, case: dict) -> CaseResult:
    problems: list[str] = []
    for item in case.get("evidence", []):
        path = repo / item["path"]
        if not path.is_file():
            problems.append(f"missing {item['path']}")
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        for needle in item.get("must_contain", []):
            if needle not in text:
                problems.append(f"{item['path']} missing {needle}")
    if problems:
        return _result(case, "FAIL", "; ".join(problems))
    return _result(case, "PASS", "evidence present")


def assess_secrets(scan_root: Path) -> CaseResult:
    case = _case("SEC-SEC-01", "secrets")
    case["local_tool"] = "security/scan_secrets.py"
    case["ci_tool"] = "security/scan_secrets.py"
    scanner = load_scanner()
    policy = scanner.load_policy(POLICY_PATH)
    findings = scanner.scan_tree(scan_root, policy)
    if findings:
        return _result(case, "FAIL", scanner.format_findings(findings) or "secret findings")
    return _result(case, "PASS", "no high-confidence secret")


def resolve_semgrep(repo: Path) -> str | None:
    vendored = repo / "security" / ".tools" / "venv" / "bin" / "semgrep"
    if vendored.is_file() and os_access(vendored):
        return str(vendored)
    return shutil.which("semgrep")


def resolve_trivy(repo: Path) -> str | None:
    vendored = repo / "security" / ".tools" / "bin" / "trivy"
    if vendored.is_file() and os_access(vendored):
        return str(vendored)
    return shutil.which("trivy")


def os_access(path: Path) -> bool:
    return path.stat().st_mode & 0o111 != 0


def _run(command: list[str], cwd: Path, timeout: int) -> subprocess.CompletedProcess[str]:
    env = dict(os.environ)
    env.update(SEMGREP_ENV)
    return subprocess.run(
        command,
        cwd=cwd,
        capture_output=True,
        text=True,
        timeout=timeout,
        env=env,
        check=False,
    )


def assess_sast(repo: Path, semgrep_bin: str | None) -> CaseResult:
    case = _case("SEC-SAST-01", "sast")
    case["local_tool"] = "semgrep"
    case["ci_tool"] = "semgrep + codeql"
    if not semgrep_bin:
        return _result(case, "FAIL", "semgrep is not installed; run security/tools/ensure_local_tools.sh")
    rules = repo / "security" / "semgrep" / "notaire.yml"
    fixture = repo / "security" / "semgrep" / "fixtures" / "bad"
    codeql = repo / ".github" / "workflows" / "codeql.yml"
    if not codeql.is_file() or "github/codeql-action" not in codeql.read_text(encoding="utf-8"):
        return _result(case, "FAIL", "CodeQL workflow is missing")
    try:
        fixture_run = _run(
            [semgrep_bin, "scan", "--config", str(rules), "--metrics", "off", "--json", str(fixture)],
            repo,
            300,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        return _result(case, "FAIL", f"fixture scan failed: {error.__class__.__name__}")
    try:
        payload = json.loads(fixture_run.stdout or "{}")
    except json.JSONDecodeError:
        return _result(case, "FAIL", "fixture scan did not return JSON")
    if not payload.get("results"):
        return _result(case, "FAIL", "fixture scan found nothing; the rules are not proving a finding")
    try:
        clean = _run(
            [
                semgrep_bin,
                "scan",
                "--config",
                str(rules),
                "--metrics",
                "off",
                "--error",
                "--quiet",
                "--exclude",
                "security/semgrep/fixtures",
                "--exclude",
                "security/.tools",
                ".",
            ],
            repo,
            600,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        return _result(case, "FAIL", f"tree scan failed: {error.__class__.__name__}")
    if clean.returncode != 0:
        tail = (clean.stdout or clean.stderr or "semgrep reported a finding").strip()
        return _result(case, "FAIL", tail[-500:])
    return _result(case, "PASS", "semgrep clean; CodeQL workflow present")


def assess_sca(repo: Path, trivy_bin: str | None, profile: str = "local") -> CaseResult:
    case = _case("SEC-SCA-01", "sca")
    case["local_tool"] = "trivy"
    case["ci_tool"] = "trivy-action"
    workflow = repo / ".github" / "workflows" / "ci.yml"
    text = workflow.read_text(encoding="utf-8") if workflow.is_file() else ""
    if "aquasecurity/trivy-action" not in text:
        return _result(case, "FAIL", "ci.yml does not run trivy-action")
    if profile == "ci":
        return _result(case, "PASS", "CI runs aquasecurity/trivy-action (report-only, SR-13)")
    if not trivy_bin:
        return _result(case, "FAIL", "trivy is not installed; run security/tools/ensure_local_tools.sh")
    try:
        completed = _run(
            [
                trivy_bin,
                "fs",
                ".",
                "--scanners",
                "vuln",
                "--ignore-unfixed",
                "--exit-code",
                "0",
                "--quiet",
                "--skip-dirs",
                "security/.tools",
                "--skip-dirs",
                "reports",
            ],
            repo,
            900,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        return _result(case, "FAIL", f"trivy failed to start: {error.__class__.__name__}")
    if completed.returncode != 0:
        return _result(case, "FAIL", (completed.stderr or completed.stdout or "trivy failed").strip()[-500:])
    return _result(case, "PASS", "trivy exited 0 (findings stay report-only until #282)")


def assess_dast(repo: Path, base_url: str | None, skip: bool, profile: str) -> CaseResult:
    case = _case("SEC-DAST-01", "dast")
    case["local_tool"] = "security/dast_baseline.py"
    case["ci_tool"] = "zap"
    workflow = repo / ".github" / "workflows" / "dast-zap.yml"
    zap_text = workflow.read_text(encoding="utf-8") if workflow.is_file() else ""
    if "zaproxy/action-baseline" not in zap_text:
        return _result(case, "FAIL", "dast-zap.yml does not run zaproxy/action-baseline")
    if profile == "ci":
        return _result(case, "PASS", "CI DAST is the weekly ZAP baseline (warn-first, #281)")
    if skip or not base_url:
        return _result(
            case,
            "SKIP",
            "live baseline runs in run_pipeline.sh after the stack is up",
        )
    spec = importlib.util.spec_from_file_location(
        "dast_baseline", repo / "security" / "dast_baseline.py"
    )
    if spec is None or spec.loader is None:
        return _result(case, "FAIL", "dast_baseline.py is missing")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    failures = module.check_base(base_url)
    if failures:
        return _result(case, "FAIL", "; ".join(failures))
    return _result(case, "PASS", f"baseline passed for {base_url}")


def run_cases(
    repo: Path,
    *,
    profile: str,
    base_url: str | None,
    skip_dast: bool,
    semgrep_bin: str | None,
    trivy_bin: str | None,
    only: str | None = None,
) -> list[CaseResult]:
    catalog = load_catalog(repo / "security" / "cases" / "catalog.yml")
    results: list[CaseResult] = []
    for case in catalog["cases"]:
        if only and case["family"] != only and case["id"] != only:
            continue
        family = case["family"]
        if family in {"threat-model", "policy"}:
            results.append(assess_evidence(repo, case))
        elif family == "secrets":
            results.append(assess_secrets(repo))
        elif family == "sast":
            results.append(assess_sast(repo, semgrep_bin))
        elif family == "sca":
            results.append(assess_sca(repo, trivy_bin, profile=profile))
        elif family == "dast":
            results.append(assess_dast(repo, base_url, skip_dast, profile))
        else:
            results.append(_result(case, "FAIL", f"unknown family {family}"))
    return results


def write_report(results: list[CaseResult], dest: Path, profile: str) -> None:
    dest.mkdir(parents=True, exist_ok=True)
    payload = {
        "profile": profile,
        "use_case": "CU78",
        "generated_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "cases": [asdict(result) for result in results],
        "summary": {
            "pass": sum(1 for result in results if result.status == "PASS"),
            "fail": sum(1 for result in results if result.status == "FAIL"),
            "skip": sum(1 for result in results if result.status == "SKIP"),
        },
    }
    (dest / "report.json").write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    rows = []
    for result in results:
        rows.append(
            "<tr>"
            f"<td>{html.escape(result.id)}</td>"
            f"<td>{html.escape(result.family)}</td>"
            f"<td class=\"{html.escape(result.status)}\">{html.escape(result.status)}</td>"
            f"<td>{html.escape(result.tool)}</td>"
            f"<td>{html.escape(result.ci_tool)}</td>"
            f"<td>{html.escape(result.detail)}</td>"
            "</tr>"
        )
    document = (
        "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">"
        "<title>Security cases</title>"
        "<style>body{font-family:-apple-system,sans-serif;max-width:1100px;margin:2rem auto;padding:0 1rem}"
        "table{width:100%;border-collapse:collapse}td,th{padding:8px;border-bottom:1px solid #ddd;text-align:left;vertical-align:top}"
        ".PASS{color:#1a7f37;font-weight:600}.FAIL{color:#cf222e;font-weight:600}.SKIP{color:#9a6700;font-weight:600}"
        "</style></head><body>"
        f"<h1>Security cases — {html.escape(profile)}</h1>"
        f"<p>CU78 · {html.escape(payload['generated_at'])} · "
        f"pass {payload['summary']['pass']} · fail {payload['summary']['fail']} · "
        f"skip {payload['summary']['skip']}</p>"
        "<table><tr><th>Case</th><th>Family</th><th>Status</th><th>Local tool</th><th>CI tool</th><th>Detail</th></tr>"
        + "".join(rows)
        + "</table></body></html>\n"
    )
    (dest / "index.html").write_text(document, encoding="utf-8")


def exit_code(results: list[CaseResult]) -> int:
    return 1 if any(result.blocking and result.status == "FAIL" for result in results) else 0


def install_tools(repo: Path) -> None:
    script = repo / "security" / "tools" / "ensure_local_tools.sh"
    completed = subprocess.run(["bash", str(script)], cwd=repo, check=False)
    if completed.returncode != 0:
        raise SystemExit(completed.returncode)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Run the Notaire security case catalog")
    parser.add_argument("--profile", choices=("local", "ci"), default="local")
    parser.add_argument("--base-url", default=None)
    parser.add_argument("--skip-dast", action="store_true")
    parser.add_argument("--only", default=None, help="family or case id, for example dast")
    parser.add_argument("--report-dir", type=Path, default=DEFAULT_REPORT)
    parser.add_argument("--no-install", action="store_true")
    parser.add_argument("--repo", type=Path, default=REPO_ROOT)
    args = parser.parse_args(argv)
    repo = args.repo.resolve()
    if args.profile == "local" and not args.no_install and args.only != "dast":
        install_tools(repo)
    semgrep_bin = resolve_semgrep(repo)
    trivy_bin = resolve_trivy(repo)
    results = run_cases(
        repo,
        profile=args.profile,
        base_url=args.base_url,
        skip_dast=args.skip_dast,
        semgrep_bin=semgrep_bin,
        trivy_bin=trivy_bin,
        only=args.only,
    )
    write_report(results, args.report_dir, args.profile)
    for result in results:
        print(f"{result.status:4} {result.id} {result.detail}")
    print(f"report: {args.report_dir / 'index.html'}")
    return exit_code(results)


if __name__ == "__main__":
    raise SystemExit(main())
