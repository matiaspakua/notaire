import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");

const nextConfig: NextConfig = {
  output: "standalone",
  // Images from public/icons/ are small unoptimized PNGs — skip optimization
  images: {
    unoptimized: true,
  },
  // /api/v1/** is proxied at request time by the App Router Route Handler
  // (src/app/api/v1/[...path]/route.ts) using runtime BACKEND_URL — do not
  // bake destinations via rewrites() (standalone evaluates them at build).
  // Static security headers (issue #562). Content-Security-Policy is set
  // per-request in the edge proxy with a nonce (issue #1051) — do not set a
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
