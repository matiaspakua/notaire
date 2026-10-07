#!/usr/bin/env python3
"""Generate coverage-snapshot.json (backend numbers only) from JaCoCo XML report.

Deliberately fails loudly (nonzero exit, no output file) rather than writing
fabricated numbers when the JaCoCo XML is missing -- a prior version wrote
hardcoded 0% on failure, which silently deployed a misleading "0% coverage"
to the live GitHub Pages site while the actual coverage was 83%+ (issue #758).
The caller (CI) is responsible for falling back to the last known-good
deployed values when this script fails, instead of trusting a fabricated
default.
"""

import xml.etree.ElementTree as ET
import json
import datetime
import sys
import os

JACOCO_XML = "backend-api/target/site/jacoco/jacoco.xml"
OUTPUT_DIR = "coverage-snapshot"
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "coverage-snapshot.json")


def main():
    if not os.path.isfile(JACOCO_XML):
        print(f"JaCoCo XML not found: {JACOCO_XML} -- not writing a snapshot", file=sys.stderr)
        return 1

    tree = ET.parse(JACOCO_XML)
    root = tree.getroot()
    counters = {}
    for c in root.findall(".//counter"):
        ct = c.get("type")
        covered = int(c.get("covered", 0))
        missed = int(c.get("missed", 0))
        total = covered + missed
        pct = round(covered * 100 / total) if total > 0 else 0
        counters[ct] = pct

    if "INSTRUCTION" not in counters or "BRANCH" not in counters:
        print("JaCoCo XML present but missing INSTRUCTION/BRANCH counters -- not writing a snapshot", file=sys.stderr)
        return 1

    snapshot = {
        "backendInstruction": counters["INSTRUCTION"],
        "backendBranch": counters["BRANCH"],
        "lastUpdated": datetime.date.today().isoformat(),
    }

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    with open(OUTPUT_FILE, "w") as f:
        json.dump(snapshot, f, indent=2)

    print("Snapshot:", json.dumps(snapshot))
    return 0


if __name__ == "__main__":
    sys.exit(main())
