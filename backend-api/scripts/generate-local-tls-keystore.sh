#!/usr/bin/env bash
# Self-signed PKCS#12 keystore for local HTTPS prove-out (#254). Not for production.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${ROOT}/target/local-dev-keystore.p12"
mkdir -p "${ROOT}/target"
PASSWORD="${SSL_KEY_STORE_PASSWORD:-changeit}"
ALIAS="${SSL_KEY_ALIAS:-notaire-local}"

keytool -genkeypair \
  -alias "${ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 825 \
  -storetype PKCS12 \
  -keystore "${OUT}" \
  -storepass "${PASSWORD}" \
  -keypass "${PASSWORD}" \
  -dname "CN=localhost, OU=Notaire Local Dev, O=Licensis, L=Local, ST=Local, C=AR"

echo "Wrote ${OUT} (alias ${ALIAS}). Start with: SPRING_PROFILES_ACTIVE=https mvn spring-boot:run -pl backend-api"
