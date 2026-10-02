#!/usr/bin/env bash
#
# enable-gh-secure.sh — GitHub Security Lab baseline for this repository.
#
# Installs https://github.com/GitHubSecurityLab/gh-secure and applies the
# settings that live only in GitHub (they are not files in this repo):
#   - private vulnerability reporting
#   - secret-scanning push protection
#   - Dependabot alerts and security updates
#
# Code scanning is .github/workflows/codeql.yml. Do not also pass
# `code-scanning` to gh-secure: default setup plus the advanced workflow
# makes GitHub reject advanced SARIF processing and fails Analyze jobs.
#
# Branch protection is opt-in. gh-secure's default rule requires one
# approving review, which blocks an unattended merge. Pass
# --with-branch-protection only when that policy is intended. If the
# repository already has active rulesets, gh-secure still prompts, even
# with --yes.
#
# Requires admin or maintain on the target repository. A token without
# administration scope fails with HTTP 403.
#
# USAGE
#   bash scripts/enable-gh-secure.sh
#   bash scripts/enable-gh-secure.sh --dry-run
#   bash scripts/enable-gh-secure.sh --apply
#   bash scripts/enable-gh-secure.sh --apply --with-branch-protection
#   bash scripts/enable-gh-secure.sh --repo owner/name --apply
set -euo pipefail

REPO="${GH_SECURE_REPO:-matiaspakua/notaire}"
APPLY=0
DRY=0
WITH_BP=0

usage() {
  sed -n '3,28p' "${BASH_SOURCE[0]}"
}

while [ $# -gt 0 ]; do
  case "$1" in
    --repo)
      REPO="${2:?--repo requires owner/name}"
      shift 2
      ;;
    --apply) APPLY=1; shift ;;
    --dry-run) DRY=1; shift ;;
    --with-branch-protection) WITH_BP=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *)
      echo "unknown option: $1 (try --help)" >&2
      exit 2
      ;;
  esac
done

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required: https://cli.github.com" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "gh is not authenticated. Run: gh auth login" >&2
  exit 1
fi

if ! gh extension list 2>/dev/null | grep -q 'GitHubSecurityLab/gh-secure'; then
  echo "Installing GitHubSecurityLab/gh-secure"
  gh extension install GitHubSecurityLab/gh-secure
fi

echo "=== gh-secure status (${REPO}) ==="
gh secure status --repo "$REPO"

if [ "$APPLY" = 0 ] && [ "$DRY" = 0 ]; then
  echo ""
  echo "Status only. Re-run with --apply to enable vulnerability reporting,"
  echo "secret-scanning push protection, and Dependabot."
  echo "Code scanning is .github/workflows/codeql.yml (do not enable default setup)."
  exit 0
fi

features=(vulnerability-reporting secret-scanning dependabot)
if [ "$WITH_BP" = 1 ]; then
  features+=(branch-protection)
fi

args=(secure --repo "$REPO" --yes)
if [ "$DRY" = 1 ]; then
  args+=(--dry-run)
fi
args+=("${features[@]}")

echo ""
echo "=== disable CodeQL default setup (advanced workflow owns scanning) ==="
if [ "$DRY" = 1 ]; then
  echo "(dry-run) would PATCH code-scanning/default-setup state=not-configured"
else
  # Ignore 404/403: older API or missing administration scope.
  if ! gh api --method PATCH \
    -H "Accept: application/vnd.github+json" \
    "repos/${REPO}/code-scanning/default-setup" \
    -f state=not-configured; then
    echo "warn: could not disable CodeQL default setup (need admin)." >&2
    echo "warn: Settings → Code security → Code scanning → disable default setup." >&2
  fi
fi

echo ""
echo "=== gh ${args[*]} ==="
gh "${args[@]}"
