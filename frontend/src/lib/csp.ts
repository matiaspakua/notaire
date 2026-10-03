/**
 * Content-Security-Policy builders for Notaire (issue #1051 / CU78).
 * Production forbids unsafe-eval and uses a per-request nonce for scripts.
 * Development may keep unsafe-eval for Next.js HMR.
 */

export interface CspOptions {
  nonce: string;
  isProduction: boolean;
}

export function buildContentSecurityPolicy({ nonce, isProduction }: CspOptions): string {
  const scriptSrc = isProduction
    ? `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'`
    : `script-src 'self' 'nonce-${nonce}' 'strict-dynamic' 'unsafe-eval'`;

  return [
    "default-src 'self'",
    scriptSrc,
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data:",
    "font-src 'self' data:",
    "connect-src 'self'",
    "frame-ancestors 'none'",
    "base-uri 'self'",
    "object-src 'none'",
  ].join("; ");
}

/** True when the CSP string meets production #1051 hardening rules. */
export function isHardenedProductionCsp(csp: string): boolean {
  const scriptSrc = csp
    .split(";")
    .map((d) => d.trim())
    .find((d) => d.startsWith("script-src"));
  if (!scriptSrc) {
    return false;
  }
  if (scriptSrc.includes("'unsafe-eval'")) {
    return false;
  }
  if (scriptSrc.includes("'unsafe-inline'") && !scriptSrc.includes("nonce-")) {
    return false;
  }
  return /nonce-[A-Za-z0-9+/=_-]+/.test(scriptSrc);
}
