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
  // internal Docker hostnames (e.g. "backend").
  async rewrites() {
    return [
      {
        source: "/api/v1/:path*",
        destination: `${BACKEND_URL}/:path*`,
      },
    ];
  },
  // Security response headers (issue #562) — defense-in-depth against
  // clickjacking, MIME-sniffing, and protocol downgrade.
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          {
            key: "Content-Security-Policy",
            value: [
              "default-src 'self'",
              "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
              "style-src 'self' 'unsafe-inline'",
              "img-src 'self' data:",
              "font-src 'self' data:",
              "connect-src 'self'",
              "frame-ancestors 'none'",
            ].join("; "),
          },
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
