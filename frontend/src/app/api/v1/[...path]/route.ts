/**
 * Catch-all Route Handler BFF for /api/v1/** (issue #1055).
 * Node runtime so standalone containers read BACKEND_URL at request time.
 * Distinct from the edge auth interceptor in src/proxy.ts (#1056).
 */
import { proxyApiRequest } from "@/lib/backend-proxy";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

type RouteContext = {
  params: Promise<{ path: string[] }>;
};

async function handle(request: Request, context: RouteContext): Promise<Response> {
  const { path } = await context.params;
  return proxyApiRequest(request, path ?? []);
}

export const GET = handle;
export const POST = handle;
export const PUT = handle;
export const PATCH = handle;
export const DELETE = handle;
export const HEAD = handle;
export const OPTIONS = handle;
