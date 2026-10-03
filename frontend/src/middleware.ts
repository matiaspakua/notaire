import { NextRequest, NextResponse } from "next/server";
import {
  AUTH_ROLE_COOKIE,
  AUTH_STATUS_COOKIE,
  forbiddenDashboardPath,
  shouldDenyAdminRoute,
} from "@/lib/admin-access";
import { buildContentSecurityPolicy } from "@/lib/csp";

const PUBLIC_PATHS = ["/login"];

function withCsp(request: NextRequest, response: NextResponse): NextResponse {
  const nonce = Buffer.from(crypto.randomUUID()).toString("base64");
  const isProduction = process.env.NODE_ENV === "production";
  const csp = buildContentSecurityPolicy({ nonce, isProduction });
  response.headers.set("Content-Security-Policy", csp);
  // Expose nonce on the request for RSC / Next script tagging when continuing.
  response.headers.set("x-nonce", nonce);
  return response;
}

function continueWithNonce(request: NextRequest): NextResponse {
  const nonce = Buffer.from(crypto.randomUUID()).toString("base64");
  const isProduction = process.env.NODE_ENV === "production";
  const csp = buildContentSecurityPolicy({ nonce, isProduction });

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);

  const response = NextResponse.next({
    request: { headers: requestHeaders },
  });
  response.headers.set("Content-Security-Policy", csp);
  return response;
}

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  // Skip Next.js internals, static files, and API proxy routes
  if (
    pathname.startsWith("/_next") ||
    pathname.startsWith("/favicon") ||
    pathname.startsWith("/api/") ||
    pathname.includes(".")
  ) {
    return NextResponse.next();
  }

  const isPublic = PUBLIC_PATHS.some((p) => pathname.startsWith(p));

  // Zustand persists to localStorage — we detect auth via cookies set on login
  const authCookie = req.cookies.get(AUTH_STATUS_COOKIE);
  const roleRaw = req.cookies.get(AUTH_ROLE_COOKIE)?.value;
  let roleValue: string | undefined;
  if (roleRaw) {
    try {
      roleValue = decodeURIComponent(roleRaw);
    } catch {
      roleValue = roleRaw;
    }
  }

  if (!authCookie && !isPublic) {
    return withCsp(req, NextResponse.redirect(new URL("/login", req.url)));
  }

  if (authCookie && pathname === "/login") {
    return withCsp(req, NextResponse.redirect(new URL("/dashboard", req.url)));
  }

  // Frontend half of CU78 admin access control (issue #1052). Role cookie is a
  // non-credential UI marker; backend RBAC remains the real control (#559).
  if (authCookie && shouldDenyAdminRoute(pathname, authCookie.value, roleValue)) {
    return withCsp(
      req,
      NextResponse.redirect(new URL(forbiddenDashboardPath(), req.url)),
    );
  }

  return continueWithNonce(req);
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
