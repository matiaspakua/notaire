import { handleClientErrorBeacon } from "@/lib/client-error-beacon";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function POST(request: Request): Promise<Response> {
  return handleClientErrorBeacon(request);
}
