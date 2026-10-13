#!/usr/bin/env bash
# SessionStart hook: give a Claude Code cloud session the toolchain the Constitution
# gates need (#1659). A fresh cloud image has JDK 21, no openspec/kustomize/trivy and a
# POSIX locale, so backend builds, Gate 1, Spotless and the SCA gate fail before any work.
#
# Cloud sessions only, idempotent, user-space (no sudo/apt), and it never blocks session
# start: a failed download prints one warning and the rest still installs.
# Pins mirror pom.xml, .cursor/install.sh and security/tools/ensure_local_tools.sh;
# workspace/tests/test_cloud_toolchain_bootstrap.py fails when they drift.
set -uo pipefail

JDK_VERSION="26"
OPENSPEC_VERSION="1.14.0"
TRIVY_VERSION="0.75.0"
KUSTOMIZE_VERSION="5.5.0"
PROMETHEUS_VERSION="3.15.0"
ALERTMANAGER_VERSION="0.34.1"

[ "${CLAUDE_CODE_REMOTE:-}" = "true" ] || exit 0

TOOLS="${NOTAIRE_TOOLS_DIR:-$HOME/.cache/notaire-tools}"
BIN="$TOOLS/bin"
mkdir -p "$BIN"

warn() { echo "warning: toolchain: $1 not installed ($2)"; }

# fetch <url> <dest-dir> [tar flags...] — download and unpack into a clean directory.
fetch() {
    local url="$1" dest="$2"; shift 2
    local archive
    archive="$(mktemp)"
    if curl -fsSL --max-time 300 -o "$archive" "$url" && mkdir -p "$dest" && tar -xzf "$archive" -C "$dest" "$@"; then
        rm -f "$archive"
        return 0
    fi
    rm -f "$archive"
    return 1
}

install_jdk() {
    [ -x "$TOOLS/jdk/bin/java" ] && return 0
    java -version 2>&1 | grep -q "\"$JDK_VERSION" && return 0
    fetch "https://api.adoptium.net/v3/binary/latest/${JDK_VERSION}/ga/linux/x64/jdk/hotspot/normal/eclipse" \
        "$TOOLS/jdk" --strip-components=1 || { rm -rf "$TOOLS/jdk"; warn "JDK $JDK_VERSION" "download failed"; }
}

install_openspec() {
    [ -x "$BIN/openspec" ] || command -v openspec >/dev/null 2>&1 && return 0
    if npm install -g --silent --prefix "$TOOLS/npm" "@fission-ai/openspec@$OPENSPEC_VERSION" >/dev/null 2>&1; then
        ln -sfn "$TOOLS/npm/bin/openspec" "$BIN/openspec"
    else
        warn "openspec $OPENSPEC_VERSION" "npm install failed"
    fi
}

install_kustomize() {
    [ -x "$BIN/kustomize" ] || command -v kustomize >/dev/null 2>&1 && return 0
    fetch "https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize%2Fv${KUSTOMIZE_VERSION}/kustomize_v${KUSTOMIZE_VERSION}_linux_amd64.tar.gz" \
        "$BIN" kustomize || warn "kustomize $KUSTOMIZE_VERSION" "download failed"
}

install_trivy() {
    [ -x "$BIN/trivy" ] || command -v trivy >/dev/null 2>&1 && return 0
    fetch "https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz" \
        "$BIN" trivy || warn "trivy $TRIVY_VERSION" "download failed"
}

install_alert_tools() {
    if ! [ -x "$BIN/promtool" ] && ! command -v promtool >/dev/null 2>&1; then
        fetch "https://github.com/prometheus/prometheus/releases/download/v${PROMETHEUS_VERSION}/prometheus-${PROMETHEUS_VERSION}.linux-amd64.tar.gz" \
            "$BIN" --strip-components=1 "prometheus-${PROMETHEUS_VERSION}.linux-amd64/promtool" \
            || warn "promtool $PROMETHEUS_VERSION" "download failed"
    fi
    if ! [ -x "$BIN/amtool" ] && ! command -v amtool >/dev/null 2>&1; then
        fetch "https://github.com/prometheus/alertmanager/releases/download/v${ALERTMANAGER_VERSION}/alertmanager-${ALERTMANAGER_VERSION}.linux-amd64.tar.gz" \
            "$BIN" --strip-components=1 "alertmanager-${ALERTMANAGER_VERSION}.linux-amd64/amtool" \
            || warn "amtool $ALERTMANAGER_VERSION" "download failed"
    fi
}

export_environment() {
    [ -n "${CLAUDE_ENV_FILE:-}" ] || return 0
    {
        echo "LC_ALL=C.UTF-8"
        echo "OPENSPEC_TELEMETRY=0"
        [ -x "$BIN/kustomize" ] && echo "KUSTOMIZE=$BIN/kustomize"
        if [ -x "$TOOLS/jdk/bin/java" ]; then
            echo "JAVA_HOME=$TOOLS/jdk"
            echo "PATH=$BIN:$TOOLS/jdk/bin:$PATH"
        else
            echo "PATH=$BIN:$PATH"
        fi
    } >> "$CLAUDE_ENV_FILE"
}

install_jdk
install_openspec
install_kustomize
install_trivy
install_alert_tools
export_environment
echo "toolchain ready: $TOOLS"
exit 0
