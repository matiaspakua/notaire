# Stage 1: Builder
FROM node:26.10.0-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ENV NEXT_TELEMETRY_DISABLED=1
# Do not bake Docker-internal hosts into NEXT_PUBLIC_* — browser uses same-origin
# /api/v1; the runner stage reads server-only BACKEND_URL at request time (#1055).

RUN npm run build

# Stage 2: Runner
FROM node:26.10.0-alpine AS runner

WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
# Default for local `docker run` without compose; override per environment.
ENV BACKEND_URL=http://localhost:8080/api/v1

RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

USER nextjs

EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["node", "server.js"]
