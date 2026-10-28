#!/usr/bin/env python3
"""Enforce Checkstyle / SpotBugs shrink-only ratchets (#566)."""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
BASELINE_PATH = REPO_ROOT / "workspace" / "ci" / "quality-ratchet-baseline.json"
BACKEND = REPO_ROOT / "backend-api"
CHECKSTYLE_XML = BACKEND / "target" / "checkstyle-result.xml"
SPOTBUGS_XML = BACKEND / "target" / "spotbugsXml.xml"


def load_baseline() -> dict:
    return json.loads(BASELINE_PATH.read_text(encoding="utf-8"))


def count_checkstyle_violations() -> int:
    if not CHECKSTYLE_XML.is_file():
        raise FileNotFoundError(f"missing {CHECKSTYLE_XML} — run checkstyle:check first")
    root = ET.parse(CHECKSTYLE_XML).getroot()
    return sum(len(file_node.findall("error")) for file_node in root.findall("file"))


def count_spotbugs_instances() -> int:
    if not SPOTBUGS_XML.is_file():
        raise FileNotFoundError(f"missing {SPOTBUGS_XML} — run spotbugs first")
    root = ET.parse(SPOTBUGS_XML).getroot()
    return len(root.findall(".//BugInstance"))


def run_maven_checkstyle(max_violations: int) -> int:
    command = [
        "mvn",
        "-q",
        "checkstyle:check",
        "-pl",
        "backend-api",
        f"-Dcheckstyle.maxAllowedViolations={max_violations}",
        "-Dcheckstyle.failOnViolation=true",
    ]
    completed = subprocess.run(command, cwd=REPO_ROOT, check=False)
    return completed.returncode


def enforce_checkstyle() -> int:
    baseline = load_baseline()
    max_allowed = int(baseline["checkstyle_max_violations"])
    exit_code = run_maven_checkstyle(max_allowed)
    if exit_code != 0:
        actual = count_checkstyle_violations()
        print(
            f"Checkstyle ratchet failed: {actual} violations (max allowed {max_allowed}). "
            f"Fix new findings or lower debt via #{baseline['zero_debt_issues']['checkstyle']}.",
            file=sys.stderr,
        )
        return 1
    print(f"Checkstyle ratchet passed (<= {max_allowed} violations).")
    return 0


def enforce_spotbugs() -> int:
    baseline = load_baseline()
    max_allowed = int(baseline["spotbugs_max_bug_instances"])
    command = [
        "mvn",
        "-q",
        "compile",
        "com.github.spotbugs:spotbugs-maven-plugin:spotbugs",
        "-pl",
        "backend-api",
        "-am",
        "-DskipSpotBugs=false",
    ]
    subprocess.run(command, cwd=REPO_ROOT, check=True)
    actual = count_spotbugs_instances()
    if actual > max_allowed:
        print(
            f"SpotBugs ratchet failed: {actual} findings (max allowed {max_allowed}). "
            f"Fix new bugs or drive to zero via #{baseline['zero_debt_issues']['spotbugs']}.",
            file=sys.stderr,
        )
        return 1
    print(f"SpotBugs ratchet passed ({actual} <= {max_allowed} findings).")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--tool", choices=("checkstyle", "spotbugs"), required=True)
    args = parser.parse_args()
    if args.tool == "checkstyle":
        return enforce_checkstyle()
    return enforce_spotbugs()


if __name__ == "__main__":
    sys.exit(main())
