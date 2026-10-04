#!/usr/bin/env python3
"""Cheap review gates on the branch's test changes (AUDIT.md E5).

The red gate proves a test fails; it cannot tell a real test from a cheap one.
These checks catch what foreman review caught by hand in #1063:

- an absolute home path in an added test line (breaks on every other machine),
- a new test file whose name already exists at another path (a duplicate
  instead of extending the class the design names),
- test changes that add no assertion.

    static_checks.py <repo> <base>   print one '- problem' line each; exit 1 if any
"""
import os
import re
import subprocess
import sys

TEST_FILE = re.compile(r"(src/test/|\.test\.tsx?$|tests/e2e/|testing/e2e/)")
HOME_PATH = re.compile(r"/Users/|/home/")
ASSERTION = re.compile(r"\b(assert\w*|expect|verify|fail)\s*\(|\bassert\s")


def git(repo, *args):
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True, text=True).stdout


def changed_tests(repo, base, diff_filter="d"):
    names = git(repo, "diff", "--name-only", "--diff-filter=" + diff_filter, base + "..HEAD").splitlines()
    return [n for n in names if TEST_FILE.search(n)]


def added_lines(repo, base, path):
    diff = git(repo, "diff", "-U0", base + "..HEAD", "--", path).splitlines()
    return [line[1:] for line in diff if line.startswith("+") and not line.startswith("+++")]


def duplicates(repo, base, path):
    name = os.path.basename(path)
    return [p for p in git(repo, "ls-tree", "-r", "--name-only", base).splitlines()
            if os.path.basename(p) == name and p != path]


def find_problems(repo, base):
    problems = []
    tests = changed_tests(repo, base)
    has_assertion = False
    for path in tests:
        lines = added_lines(repo, base, path)
        if any(HOME_PATH.search(line) for line in lines):
            problems.append(f"{path}: absolute home path (/Users/ or /home/) — resolve paths from the project root")
        has_assertion = has_assertion or any(ASSERTION.search(line) for line in lines)
    for path in changed_tests(repo, base, "A"):
        for other in duplicates(repo, base, path):
            problems.append(f"{path}: a test class with this name already exists at {other} — extend it instead")
    if tests and not has_assertion:
        problems.append("test changes add no assertion — each new test must assert the new behaviour")
    return problems


if __name__ == "__main__":
    found = find_problems(sys.argv[1], sys.argv[2])
    for p in found:
        print("- " + p)
    sys.exit(1 if found else 0)
