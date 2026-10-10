import { Counter, Registry, collectDefaultMetrics } from "prom-client";

export const dynamic = "force-dynamic";

const registry = new Registry();
collectDefaultMetrics({ register: registry });

const scrapes = new Counter({
  name: "notaire_frontend_metrics_scrapes_total",
  help: "Times the metrics endpoint was scraped",
  registers: [registry],
});

export async function GET(): Promise<Response> {
  scrapes.inc();
  return new Response(await registry.metrics(), {
    headers: { "Content-Type": registry.contentType },
  });
}
