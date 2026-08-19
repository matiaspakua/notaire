# ADR-017: Container / Base-Image Strategy

## Status
Accepted

## Context
Both application containers (backend, frontend) build multi-stage Docker
images. The base-image choices affect image size, attack surface (Trivy
scan results), and startup speed — worth recording explicitly rather than
leaving as implicit Dockerfile detail.

## Decision
Use **Alpine-based, multi-stage builds** for both backend and frontend,
running as a **non-root user** in the final stage.

### Backend (`backend-api/Dockerfile`)
- **Build stage**: `maven:3.9-eclipse-temurin-21-alpine` — compiles
  `notaire-shared` + `backend-api` (`mvn package -pl backend-api -am
  -DskipTests`).
- **Runtime stage**: `eclipse-temurin:21-jre-alpine` (JRE only, not JDK) —
  copies the built JAR, runs as `notary` (uid/gid 1000).
- JVM tuned for containers: `-XX:+UseContainerSupport
  -XX:MaxRAMPercentage=70.0 -XX:+UseG1GC`.
- `HEALTHCHECK` via `wget` against `/actuator/health`.
- **`backend-api/Dockerfile.slim`** variant: same runtime stage, but expects
  a pre-built JAR (`backend-api/target/*.jar`) copied in directly, skipping
  the Maven build stage entirely — used by CI/CD where the JAR is already
  built by an earlier pipeline step, avoiding a duplicate compile.

### Frontend (`frontend/Dockerfile`)
- **Build stage**: `node:22-alpine` — `npm ci` + `npm run build` (Next.js
  standalone output).
- **Runtime stage**: `node:22-alpine` — copies only `.next/standalone`,
  `.next/static`, and `public/`; runs as `nextjs` (uid/gid 1001).
- `NEXT_TELEMETRY_DISABLED=1` in both stages.

## Options Considered
- **Distroless images**: Rejected — smaller attack surface, but no shell
  makes the `wget`-based `HEALTHCHECK` and ad-hoc container debugging
  harder; Alpine's size savings already satisfy current needs.
- **Full `jdk`/`node` images for runtime**: Rejected — unnecessarily large
  and include build tooling not needed at runtime, widening the CVE surface
  Trivy scans against.

## Consequences
- **Pros**: Small final images (JRE-only, standalone Next.js output),
  non-root runtime reduces container-escape blast radius, multi-stage builds
  keep source/build tooling out of the shipped image.
- **Cons**: Alpine's `musl` libc occasionally surfaces native-dependency
  quirks not seen on `glibc`-based distros (none currently blocking); Trivy
  filesystem/image scans (see `.claude/rules/code-quality.md`) must still be
  run regularly since Alpine base images do accumulate CVEs over time.
