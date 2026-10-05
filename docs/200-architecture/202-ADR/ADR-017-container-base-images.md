# ADR-017: Container / Base-Image Strategy

## Status

Accepted (pin-to-minor-or-digest policy added — issue #1045 / CU78)

## Context

Both application containers (backend, frontend) build multi-stage Docker
images. The base-image choices affect image size, attack surface (Trivy
scan results), and startup speed — worth recording explicitly rather than
leaving as implicit Dockerfile detail.

Floating tags (`:latest`, major-only channels such as `postgres:16` or
`node:22-alpine`, unversioned `sonarqube:community`) make local/prod/CI
stacks non-reproducible. Issue #1045 requires every in-scope image to be
pinned to a **minor version or digest**, with Dependabot covering Dockerfile
bases.

## Decision

Use **Alpine-based, multi-stage builds** for both backend and frontend,
running as a **non-root user** in the final stage.

**Pin policy:** every compose `image:` and Dockerfile `FROM` (and related CI
service images such as postgres) MUST use a minor-version tag (preferred) or
digest (`@sha256:…`). Do not use `:latest`, major-only floats, or bare
channel tags. Compose/infra pins live in YAML and are bumped in-repo;
Dependabot **docker** ecosystems for `/backend-api` and `/frontend` open PRs
when Dockerfile bases change.

### Backend (`backend-api/Dockerfile`)

- **Build stage**: `maven:3.x-eclipse-temurin-26-alpine` (pinned minor;
  currently `3.10.0-eclipse-temurin-26-alpine`, JDK 26 since #1276) — compiles
  `backend-api` (`mvn package -pl backend-api -am
  -DskipTests`).
- **Runtime stage**: `eclipse-temurin:26.0.x_*-jre-alpine` (pinned;
  currently `26.0.2.1_1-jre-alpine`) — copies the built JAR, runs as
  `notary` (uid/gid 1000).
- JVM tuned for containers: `-XX:+UseContainerSupport
  -XX:MaxRAMPercentage=70.0 -XX:+UseG1GC`.
- `HEALTHCHECK` via `wget` against `/actuator/health`.
- **`backend-api/Dockerfile.slim`** variant: same runtime stage, but expects
  a pre-built JAR (`backend-api/target/*.jar`) copied in directly, skipping
  the Maven build stage entirely — used by CI/CD where the JAR is already
  built by an earlier pipeline step, avoiding a duplicate compile.

### Frontend (`frontend/Dockerfile`)

- **Build stage**: `node:22.x.x-alpine` (pinned; currently `22.23.3-alpine`)
  — `npm ci` + `npm run build` (Next.js standalone output).
- **Runtime stage**: same pinned Node Alpine tag — copies only
  `.next/standalone`, `.next/static`, and `public/`; runs as `nextjs`
  (uid/gid 1001).
- `NEXT_TELEMETRY_DISABLED=1` in both stages.

## Options Considered

- **Distroless images**: Rejected — smaller attack surface, but no shell
  makes the `wget`-based `HEALTHCHECK` and ad-hoc container debugging
  harder; Alpine's size savings already satisfy current needs.
- **Full `jdk`/`node` images for runtime**: Rejected — unnecessarily large
  and include build tooling not needed at runtime, widening the CVE surface
  Trivy scans against.
- **Digest-only pins everywhere**: Rejected for routine compose/Dockerfile
  pins — opaque diffs; minor tags stay reviewable and Dependabot-friendly.
  Digests remain acceptable where registries lack stable minor tags.

## Consequences

- **Pros**: Small final images (JRE-only, standalone Next.js output),
  non-root runtime reduces container-escape blast radius, multi-stage builds
  keep source/build tooling out of the shipped image; pinned tags make
  rebuilds reproducible; Dependabot docker PRs keep Dockerfile bases current.
- **Cons**: Alpine's `musl` libc occasionally surfaces native-dependency
  quirks not seen on `glibc`-based distros (none currently blocking); Trivy
  filesystem/image scans (see `.claude/rules/code-quality.md`) must still be
  run regularly since Alpine base images do accumulate CVEs over time;
  compose/infra `image:` lines are not auto-rewritten by Dependabot docker
  (bump via PR / checklist in `infra/README.md`).
- **Hygiene**: `scripts/test_image_pins_and_dependabot.py` rejects floating
  tags and asserts Dependabot npm + docker coverage.
