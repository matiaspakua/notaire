#!/usr/bin/env python3
"""Run the CHECK/EXPECTED lines of foreman review notes (AUDIT.md H1).

A note pairs each 'CHECK: <shell command>' with the next 'EXPECTED: <text>'.
When EXPECTED is one literal token (0, PASS, a number), the output must equal
it. Otherwise the output is shown for the foreman to judge.

    review_check.py <workdir> <note.md>...   exit 1 if any literal check fails
"""
import subprocess
import sys
from dataclasses import dataclass

TIMEOUT = 900


@dataclass
class Result:
    cmd: str
    expected: str
    output: str
    verdict: str  # PASS, FAIL or JUDGE


def parse_checks(text):
    pairs, cmd = [], None
    for line in text.splitlines():
        if line.startswith("CHECK:"):
            cmd = line[len("CHECK:"):].strip()
        elif line.startswith("EXPECTED:") and cmd is not None:
            pairs.append((cmd, line[len("EXPECTED:"):].strip()))
            cmd = None
    return pairs


def run_one(cmd, cwd):
    try:
        done = subprocess.run(["bash", "-c", cmd], cwd=cwd, capture_output=True, text=True, timeout=TIMEOUT)
        return (done.stdout + done.stderr).strip()
    except subprocess.TimeoutExpired:
        return f"(timed out after {TIMEOUT}s)"


def verdict(expected, output):
    if len(expected.split()) != 1:
        return "JUDGE"
    return "PASS" if output == expected else "FAIL"


def run_checks(pairs, cwd):
    results = []
    for cmd, expected in pairs:
        output = run_one(cmd, cwd)
        results.append(Result(cmd, expected, output, verdict(expected, output)))
    return results


def exit_code(results):
    return 1 if any(r.verdict == "FAIL" for r in results) else 0


def main(workdir, notes):
    results = []
    for note in notes:
        results += run_checks(parse_checks(open(note).read()), workdir)
    for r in results:
        print(f"[{r.verdict}] {r.cmd}\n  expected: {r.expected}\n  output:   {r.output[-2000:]}")
    return exit_code(results)


if __name__ == "__main__":
    sys.exit(main(sys.argv[1], sys.argv[2:]))
