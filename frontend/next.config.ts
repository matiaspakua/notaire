import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");

// BACKEND_URL is the internal server-side URL (never exposed to browser).
// In Docker it resolves via internal DNS: http://backend:8080/api/v1
// Locally it points to the running backend: http://localhost:8080/api/v1
const BACKEND_URL =
  process.env.BACKEND_URL ??
  process.env.NEXT_PUBLIC_API_URL ??
  "http://localhost:8080/api/v1";

const nextConfig: NextConfig = {
  output: "standalone",
  // Images from public/icons/ are small unoptimized PNGs — skip optimization
  images: {
    unoptimized: true,
  },
  // Proxy all /api/v1 calls server-side so the browser never needs to reach
  // internal Docker hostnames (e.g. "backend"). Cookie / Set-Cookie are
  // forwarded for HttpOnly JWT sessions (issue #1051).
  async rewrites() {
    return [
      {
        source: "/api/v1/:path*",
        destination: `${BACKEND_URL}/:path*`,
      },
    ];
  },
  // Static security headers (issue #562). Content-Security-Policy is set
  // per-request in middleware with a nonce (issue #1051) — do not set a
  // competing CSP here.
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          {
            key: "X-Frame-Options",
            value: "DENY",
          },
          {
            key: "X-Content-Type-Options",
            value: "nosniff",
          },
          {
            key: "Strict-Transport-Security",
            value: "max-age=63072000; includeSubDomains",
          },
        ],
      },
    ];
  },
};

export default withNextIntl(nextConfig);
