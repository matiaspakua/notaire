#!/usr/bin/env bash
# Cursor Cloud Agent — start Notaire stack on every boot.
# Nested Docker: host networking via docker-compose.cloud.yml (bridge CNI fails).
set -euo pipefail
cd /workspace

sudo update-alternatives --set iptables /usr/sbin/iptables-legacy 2>/dev/null || true
sudo update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy 2>/dev/null || true
sudo mkdir -p /etc/docker
printf '%s\n' '{' '  "storage-driver": "fuse-overlayfs",' '  "iptables": true' '}' | sudo tee /etc/docker/daemon.json >/dev/null

if ! docker info >/dev/null 2>&1; then
  sudo pkill dockerd 2>/dev/null || true
  sleep 1
  sudo rm -f /var/run/docker.pid
  sudo dockerd >/tmp/dockerd.log 2>&1 &
  for i in $(seq 1 40); do
    if docker info >/dev/null 2>&1; then break; fi
    sleep 1
  done
fi
sudo chmod 666 /var/run/docker.sock 2>/dev/null || true

if [ ! -f .env ]; then
  cp .env.example .env
  sed -i 's|^JWT_SECRET=.*|JWT_SECRET=cloud-agent-dev-jwt-secret-at-least-32-bytes-ok|' .env
fi

export COMPOSE_FILE=docker-compose.yml:docker-compose.cloud.yml
docker compose --env-file .env up -d postgres backend frontend

# Wait for readiness (do not fail the boot script hard — agent can still work)
for i in $(seq 1 90); do
  if curl -sf http://127.0.0.1:8080/actuator/health/liveness >/dev/null 2>&1 \
     && curl -sf -o /dev/null http://127.0.0.1:3000/login; then
    echo "notaire stack ready"
    exit 0
  fi
  sleep 2
done
echo "notaire stack start timed out; check docker ps / logs" >&2
docker ps -a || true
exit 0
