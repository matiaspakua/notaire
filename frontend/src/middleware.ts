import { NextRequest, NextResponse } from "next/server";
import {
  AUTH_ROLE_COOKIE,
  AUTH_STATUS_COOKIE,
  forbiddenDashboardPath,
  shouldDenyAdminRoute,
} from "@/lib/admin-access";

const PUBLIC_PATHS = ["/login"];

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
    return NextResponse.redirect(new URL("/login", req.url));
  }

  if (authCookie && pathname === "/login") {
    return NextResponse.redirect(new URL("/dashboard", req.url));
  }

  // Frontend half of CU78 admin access control (issue #1052). Role cookie is a
  // non-credential UI marker; backend RBAC remains the real control (#559).
  if (authCookie && shouldDenyAdminRoute(pathname, authCookie.value, roleValue)) {
    return NextResponse.redirect(new URL(forbiddenDashboardPath(), req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
