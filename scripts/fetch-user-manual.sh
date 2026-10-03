#!/usr/bin/env bash
# Fetch the historical Manual de Usuario PDF from the docs-manuals GitHub Release.
# The PDF is not stored as an ordinary git blob (issue #1050 / CU76 / ADR-022).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEST_DIR="${REPO_ROOT}/docs/100-business/105-manuals/C_Manual de Usuario"
DEST_FILE="${DEST_DIR}/Manual de Usuario Notaire.doc.pdf"
RELEASE_TAG="${NOTAIRE_MANUAL_RELEASE_TAG:-docs-manuals}"
ASSET_NAME="${NOTAIRE_MANUAL_ASSET_NAME:-Manual-de-Usuario-Notaire.doc.pdf}"

mkdir -p "${DEST_DIR}"

if [[ -f "${DEST_FILE}" ]]; then
  echo "Already present: ${DEST_FILE}"
  exit 0
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "gh CLI is required to download release assets." >&2
  echo "Install: https://cli.github.com/ — then re-run this script." >&2
  exit 1
fi

echo "Downloading ${ASSET_NAME} from release ${RELEASE_TAG}…"
gh release download "${RELEASE_TAG}" \
  --repo matiaspakua/notaire \
  --pattern "${ASSET_NAME}" \
  --dir "${DEST_DIR}" \
  --clobber

# Normalize filename if the release asset used the hyphenated public name.
if [[ ! -f "${DEST_FILE}" && -f "${DEST_DIR}/${ASSET_NAME}" ]]; then
  mv "${DEST_DIR}/${ASSET_NAME}" "${DEST_FILE}"
fi

if [[ ! -f "${DEST_FILE}" ]]; then
  echo "Download finished but ${DEST_FILE} was not found." >&2
  echo "Check the release tag (${RELEASE_TAG}) and asset name (${ASSET_NAME})." >&2
  exit 1
fi

echo "Saved: ${DEST_FILE}"
