#!/usr/bin/env bash
#
# fleet-token.sh — mint a GitHub App installation token for the agent fleet (#1648).
#
# Fleet agents need to write issue labels and Project Status; personal or cloud
# agent tokens got 403 there (#1551), which forced human handoffs and board
# drift. The `notaire-fleet` App holds exactly the permissions in
# docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md.
#
# USAGE
#   eval "$(bash workspace/sdlc/fleet-token.sh)"   # exports GH_TOKEN for gh / scripts
#   bash workspace/sdlc/fleet-token.sh --jwt-only  # print the App JWT (tests)
#
# ENV  FLEET_APP_ID, FLEET_APP_PRIVATE_KEY (PEM text), optional FLEET_REPO.
# Without them it prints nothing on stdout, warns on stderr, and the caller keeps
# its ambient `gh` auth — never a silent identity switch.

set -euo pipefail

REPO="${FLEET_REPO:-matiaspakua/notaire}"

if [ -z "${FLEET_APP_ID:-}" ] || [ -z "${FLEET_APP_PRIVATE_KEY:-}" ]; then
  echo "fleet-token: fleet App not configured (FLEET_APP_ID / FLEET_APP_PRIVATE_KEY); using ambient gh auth" >&2
  exit 0
fi

b64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }

now=$(date +%s)
header=$(printf '{"alg":"RS256","typ":"JWT"}' | b64url)
payload=$(printf '{"iat":%d,"exp":%d,"iss":"%s"}' "$((now - 60))" "$((now + 540))" "$FLEET_APP_ID" | b64url)
key_file=$(mktemp)
trap 'rm -f "$key_file"' EXIT
printf '%s\n' "$FLEET_APP_PRIVATE_KEY" > "$key_file"
signature=$(printf '%s.%s' "$header" "$payload" | openssl dgst -sha256 -sign "$key_file" | b64url)
jwt="$header.$payload.$signature"

if [ "${1:-}" = "--jwt-only" ]; then
  printf '%s\n' "$jwt"
  exit 0
fi

api() {
  curl -fsS -X "$1" "https://api.github.com$2" \
    -H "Authorization: Bearer $jwt" -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28"
}

installation=$(api GET "/repos/$REPO/installation" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
token=$(api POST "/app/installations/$installation/access_tokens" | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
printf 'export GH_TOKEN=%q\n' "$token"
echo "fleet-token: GH_TOKEN set to the notaire-fleet installation (expires in 1 h)" >&2
