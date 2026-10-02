#!/usr/bin/env bash
# Cursor Cloud Agent — idempotent install for Notaire AI SDLC.
# Installs toolchains + project deps. Must terminate. No long-running servers.
set -euo pipefail
cd /workspace

export DEBIAN_FRONTEND=noninteractive

# --- Node on default PATH (login shells + sudo) ---
NODE_BIN=""
if [ -x /home/ubuntu/.nvm/versions/node/v22.22.2/bin/node ]; then
  NODE_BIN=/home/ubuntu/.nvm/versions/node/v22.22.2/bin
elif command -v node >/dev/null 2>&1; then
  NODE_BIN="$(dirname "$(command -v node)")"
fi
if [ -n "$NODE_BIN" ]; then
  sudo ln -sfn "$NODE_BIN/node" /usr/local/bin/node
  sudo ln -sfn "$NODE_BIN/npm" /usr/local/bin/npm
  sudo ln -sfn "$NODE_BIN/npx" /usr/local/bin/npx
fi

# --- Maven 3.9+ ---
if ! command -v mvn >/dev/null 2>&1; then
  curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -o /tmp/maven.tgz
  sudo tar -xzf /tmp/maven.tgz -C /opt
  sudo ln -sfn /opt/apache-maven-3.9.9/bin/mvn /usr/local/bin/mvn
fi

# --- Docker + compose (nested VM: fuse-overlayfs + iptables-legacy) ---
sudo apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq \
  ca-certificates curl gnupg fuse3 fuse-overlayfs iptables \
  docker.io docker-compose-v2 postgresql-client bc \
  -o Dpkg::Options::="--force-confnew" || true
sudo update-alternatives --set iptables /usr/sbin/iptables-legacy 2>/dev/null || true
sudo update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy 2>/dev/null || true
sudo mkdir -p /etc/docker
printf '%s\n' '{' '  "storage-driver": "fuse-overlayfs",' '  "iptables": true' '}' | sudo tee /etc/docker/daemon.json >/dev/null

# --- OpenSpec CLI ---
mkdir -p "$HOME/.local"
npm config set prefix "$HOME/.local"
npm install -g @fission-ai/openspec@1.14.0
sudo ln -sfn "$HOME/.local/bin/openspec" /usr/local/bin/openspec

# --- Env files (dev placeholders; override via Cursor secrets when needed) ---
if [ ! -f .env ]; then
  cp .env.example .env
  sed -i 's|^JWT_SECRET=.*|JWT_SECRET=cloud-agent-dev-jwt-secret-at-least-32-bytes-ok|' .env
fi
if [ -f frontend/.env.local.example ] && [ ! -f frontend/.env.local ]; then
  cp frontend/.env.local.example frontend/.env.local
fi

# --- Frontend deps + Playwright Chromium ---
(
  cd frontend
  if [ -f package-lock.json ]; then npm ci; else npm install; fi
  npx playwright install --with-deps chromium
)

# --- Maven reactor (shared + backend) ---
mvn -DskipTests clean install -pl notaire-shared,backend-api -am

# --- Pre-build Docker images while dockerd is available (best-effort) ---
if ! docker info >/dev/null 2>&1; then
  sudo pkill dockerd 2>/dev/null || true
  sudo dockerd >/tmp/dockerd-install.log 2>&1 &
  for i in $(seq 1 30); do
    docker info >/dev/null 2>&1 && break
    sleep 1
  done
  sudo chmod 666 /var/run/docker.sock 2>/dev/null || true
fi
if docker info >/dev/null 2>&1; then
  export COMPOSE_FILE=docker-compose.yml:docker-compose.cloud.yml
  docker compose --env-file .env build backend frontend || true
fi

echo "notaire cloud install complete"
