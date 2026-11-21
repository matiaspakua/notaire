/**
 * Factory Dashboard — Server entry point.
 *
 * Fastify HTTP + WebSocket server that bridges the AI SDLC Python engine
 * to the React UI. Watches the evidence ledger for real-time events and
 * exposes the factory OpenAPI endpoints.
 */
import Fastify from "fastify";
import fastifyWs from "@fastify/websocket";
import fastifyStatic from "@fastify/static";
import { join, dirname } from "path";
import { fileURLToPath } from "url";
import { EventEmitter } from "events";
import { readFileSync, existsSync } from "fs";
import chokidar from "chokidar";
import { ConfigStore } from "./config-store.js";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = join(__dirname, "../..");

// ───────────────────── Event bus (pub/sub core) ─────────────────────

export const eventBus = new EventEmitter();
eventBus.setMaxListeners(50);

// ───────────────────── WebSocket clients ─────────────────────

const wsClients: Set<any> = new Set();

// ───────────────────── Evidence ledger watcher ─────────────────────

function startLedgerWatcher(aisdlcRoot: string) {
  const ledgerPath = join(aisdlcRoot, ".aisdlc", "evidence", "ledger.jsonl");
  const costsPath = join(aisdlcRoot, ".aisdlc", "evidence", "cost.jsonl");

  if (!existsSync(ledgerPath)) {
    console.log("[factory-dashboard] No evidence ledger found at " + ledgerPath + ", watching for creation");
  }

  const pathsToWatch = [ledgerPath, costsPath].filter(p => existsSync(p) || true);
  const watcher = chokidar.watch(pathsToWatch, {
    ignored: /\/\./,
    ignoreInitial: true,
    awaitWriteFinish: { stabilityThreshold: 100, pollInterval: 50 },
  });

  watcher.on("add", (path: string) => {
    console.log("[factory-dashboard] Evidence file created: " + path);
    eventBus.emit("evidence:file", { path });
  });

  watcher.on("change", (path: string) => {
    try {
      const lines = readFileSync(path, "utf-8").trim().split("\n").filter(Boolean);
      const lastLine = lines[lines.length - 1];
      if (lastLine) {
        const event = JSON.parse(lastLine);
        eventBus.emit("evidence:event", event);
        broadcastToWs({
          type: "event",
          timestamp: new Date().toISOString(),
          data: event,
        });
      }
    } catch (err) {
      console.error("[factory-dashboard] Error parsing evidence: " + err);
    }
  });

  console.log("[factory-dashboard] Watching evidence at " + aisdlcRoot + "/.aisdlc/evidence/");
  return watcher;
}

// ───────────────────── WebSocket broadcast ─────────────────────

function broadcastToWs(message: any) {
  const payload = JSON.stringify(message);
  for (const client of wsClients) {
    try {
      client.send(payload);
    } catch (err) {
      console.error(`[factory-dashboard] WebSocket send error: ${err}`);
      wsClients.delete(client);
    }
  }
}

// ───────────────────── AI SDLC bridge ─────────────────────

async function fetchFromAisdlc(path: string): Promise<any> {
  const port = process.env.AISDLC_SERVE_PORT || "8765";
  try {
    const res = await fetch(`http://127.0.0.1:${port}${path}`);
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

// ───────────────────── Server setup ─────────────────────

// Find the project root by walking up from the factory-dashboard directory
// until we find .claude/ or .aisdlc/
function findProjectRoot(start: string): string {
  let current = start;
  const maxDepth = 20;
  let depth = 0;
  while (depth < maxDepth) {
    if (existsSync(join(current, ".claude")) || existsSync(join(current, ".aisdlc"))) {
      return current;
    }
    const parent = dirname(current);
    if (parent === current) break; // reached filesystem root
    current = parent;
    depth++;
  }
  return start;
}

const aisdlcRoot = process.env.AISDLC_ROOT || findProjectRoot(__dirname);
const port = parseInt(process.env.FACTORY_DASHBOARD_PORT || "8766", 10);

// Initialize config store and discover all configuration
const configStore = new ConfigStore(aisdlcRoot);
configStore.discover();

const fastify = Fastify({
  logger: {
    level: process.env.NODE_ENV === "production" ? "info" : "debug",
  },
});

async function start() {
  // Register plugins
  await fastify.register(fastifyWs);
  await fastify.register(fastifyStatic, {
    root: join(projectRoot, "dist/ui"),
    prefix: "/",
  });

  // Start evidence watcher
  startLedgerWatcher(aisdlcRoot);

  // ── API: Capabilities ──
  fastify.get("/api/factory/capabilities", async () => {
    const caps = await fetchFromAisdlc("/factory/capabilities");
    return caps || {
      approvals_live: false,
      runner: false,
      forge_write: false,
      isolator: { sandbox: false, enforces_committer: false },
    };
  });

  // ── API: Runs ──
  fastify.get("/api/factory/runs", async () => {
    const runs = await fetchFromAisdlc("/factory/runs");
    return runs || { items: [], total: 0 };
  });

  fastify.get("/api/factory/runs/:id", async (request: any) => {
    const { id } = request.params;
    const run = await fetchFromAisdlc(`/factory/runs/${id}`);
    if (!run) {
      throw createError(404, `Run ${id} not found`);
    }
    return run;
  });

  fastify.get("/api/factory/runs/:id/events", async (request: any) => {
    const { id } = request.params;
    const events = await fetchFromAisdlc(`/factory/runs/${id}/events`);
    return events || { items: [] };
  });

  // ── API: Workflows ──
  fastify.get("/api/factory/workflows", async () => {
    const workflows = await fetchFromAisdlc("/factory/workflows");
    return workflows || { items: [] };
  });

  // ── API: Config ──
  fastify.get("/api/factory/config", async () => {
    const config = await fetchFromAisdlc("/factory/config");
    return config || {};
  });

  // ── API: Report ──
  fastify.get("/api/factory/report", async () => {
    const report = await fetchFromAisdlc("/factory/report");
    return report || {};
  });

  // ── API: Run Control (bidirectional commands) ──
  fastify.post("/api/factory/runs/:id/control", async (request: any) => {
    const { id } = request.params;
    const body: any = request.body;
    const command = body?.command;

    if (!command || !["play", "pause", "resume", "stop", "skip"].includes(command)) {
      throw createError(400, `Invalid command: ${command}. Must be play, pause, resume, stop, or skip.`);
    }

    const reason = body?.reason || "";
    const stepId = body?.step_id;

    // Forward to aisdlc serve if available
    let aisdlcResponse = null;
    try {
      const port = process.env.AISDLC_SERVE_PORT || "8765";
      const res = await fetch(`http://127.0.0.1:${port}/factory/runs/${id}/control`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ command, reason, step_id: stepId }),
      });
      if (res.ok) {
        aisdlcResponse = await res.json();
      }
    } catch {
      // aisdlc serve not available — handle locally for demo mode
    }

    // Process control command locally (state machine)
    const controlEvent = {
      kind: `run.${command}`,
      run_id: id,
      step: stepId || undefined,
      reason,
      timestamp: new Date().toISOString(),
    };

    // Emit to event bus for ledger watcher
    eventBus.emit("control:command", controlEvent);

    // Broadcast to all WebSocket clients
    const ack = {
      command,
      accepted: true,
      run_id: id,
      message: `Command "${command}" applied to run ${id}`,
      timestamp: new Date().toISOString(),
    };
    broadcastToWs({
      type: "control_ack",
      timestamp: new Date().toISOString(),
      data: ack,
    });

    // Also emit as an evidence event so the UI picks it up
    broadcastToWs({
      type: "event",
      timestamp: new Date().toISOString(),
      data: controlEvent,
    });

    console.log(`[factory-dashboard] Control: ${command} on run ${id}${stepId ? ` step ${stepId}` : ""}${reason ? ` (${reason})` : ""}`);

    return aisdlcResponse || ack;
  });

  // ═══════════════════════════════════════════════════
  //  API: Configuration — Agents
  // ═══════════════════════════════════════════════════

  fastify.get("/api/factory/config/agents", async () => {
    const items = configStore.getAgents();
    return { items, total: items.length };
  });

  fastify.get("/api/factory/config/agents/:id", async (request: any) => {
    const { id } = request.params;
    const agent = configStore.getAgent(id);
    if (!agent) throw createError(404, `Agent ${id} not found`);
    return agent;
  });

  fastify.post("/api/factory/config/agents", async (request: any) => {
    const body = request.body;
    const agent = configStore.createAgent(body);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "agent", action: "created", id: agent.id } });
    return agent;
  });

  fastify.put("/api/factory/config/agents/:id", async (request: any) => {
    const { id } = request.params;
    const body = request.body;
    const agent = configStore.updateAgent(id, body);
    if (!agent) throw createError(404, `Agent ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "agent", action: "updated", id } });
    return agent;
  });

  fastify.delete("/api/factory/config/agents/:id", async (request: any) => {
    const { id } = request.params;
    const deleted = configStore.deleteAgent(id);
    if (!deleted) throw createError(404, `Agent ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "agent", action: "deleted", id } });
    return { deleted: true };
  });

  // ═══════════════════════════════════════════════════
  //  API: Configuration — Rules
  // ═══════════════════════════════════════════════════

  fastify.get("/api/factory/config/rules", async () => {
    const items = configStore.getRules();
    return { items, total: items.length };
  });

  fastify.get("/api/factory/config/rules/:id", async (request: any) => {
    const { id } = request.params;
    const rule = configStore.getRule(id);
    if (!rule) throw createError(404, `Rule ${id} not found`);
    return rule;
  });

  fastify.post("/api/factory/config/rules", async (request: any) => {
    const body = request.body;
    const rule = configStore.createRule(body);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "rule", action: "created", id: rule.id } });
    return rule;
  });

  fastify.put("/api/factory/config/rules/:id", async (request: any) => {
    const { id } = request.params;
    const body = request.body;
    const rule = configStore.updateRule(id, body);
    if (!rule) throw createError(404, `Rule ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "rule", action: "updated", id } });
    return rule;
  });

  fastify.delete("/api/factory/config/rules/:id", async (request: any) => {
    const { id } = request.params;
    const deleted = configStore.deleteRule(id);
    if (!deleted) throw createError(404, `Rule ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "rule", action: "deleted", id } });
    return { deleted: true };
  });

  // ═══════════════════════════════════════════════════
  //  API: Configuration — MCP Servers
  // ═══════════════════════════════════════════════════

  fastify.get("/api/factory/config/mcp", async () => {
    const items = configStore.getMcpServers();
    return { items, total: items.length };
  });

  fastify.get("/api/factory/config/mcp/:id", async (request: any) => {
    const { id } = request.params;
    const server = configStore.getMcpServer(id);
    if (!server) throw createError(404, `MCP server ${id} not found`);
    return server;
  });

  fastify.post("/api/factory/config/mcp", async (request: any) => {
    const body = request.body;
    const server = configStore.createMcpServer(body);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "mcp", action: "created", id: server.id } });
    return server;
  });

  fastify.put("/api/factory/config/mcp/:id", async (request: any) => {
    const { id } = request.params;
    const body = request.body;
    const server = configStore.updateMcpServer(id, body);
    if (!server) throw createError(404, `MCP server ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "mcp", action: "updated", id } });
    return server;
  });

  fastify.delete("/api/factory/config/mcp/:id", async (request: any) => {
    const { id } = request.params;
    const deleted = configStore.deleteMcpServer(id);
    if (!deleted) throw createError(404, `MCP server ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "mcp", action: "deleted", id } });
    return { deleted: true };
  });

  fastify.post("/api/factory/config/mcp/:id/health", async (request: any) => {
    const { id } = request.params;
    const server = await configStore.checkMcpHealth(id);
    if (!server) throw createError(404, `MCP server ${id} not found`);
    return server;
  });

  // ═══════════════════════════════════════════════════
  //  API: Configuration — Skills
  // ═══════════════════════════════════════════════════

  fastify.get("/api/factory/config/skills", async () => {
    const items = configStore.getSkills();
    return { items, total: items.length };
  });

  fastify.get("/api/factory/config/skills/:id", async (request: any) => {
    const { id } = request.params;
    const skill = configStore.getSkill(id);
    if (!skill) throw createError(404, `Skill ${id} not found`);
    return skill;
  });

  fastify.post("/api/factory/config/skills", async (request: any) => {
    const body = request.body;
    const skill = configStore.createSkill(body);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "skill", action: "created", id: skill.id } });
    return skill;
  });

  fastify.put("/api/factory/config/skills/:id", async (request: any) => {
    const { id } = request.params;
    const body = request.body;
    const skill = configStore.updateSkill(id, body);
    if (!skill) throw createError(404, `Skill ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "skill", action: "updated", id } });
    return skill;
  });

  fastify.delete("/api/factory/config/skills/:id", async (request: any) => {
    const { id } = request.params;
    const deleted = configStore.deleteSkill(id);
    if (!deleted) throw createError(404, `Skill ${id} not found`);
    broadcastToWs({ type: "config_update", timestamp: new Date().toISOString(), data: { kind: "skill", action: "deleted", id } });
    return { deleted: true };
  });

  // ── API: Dashboard state (aggregated) ──
  fastify.get("/api/factory/dashboard", async () => {
    const [capabilities, runs, workflows] = await Promise.all([
      fetchFromAisdlc("/factory/capabilities"),
      fetchFromAisdlc("/factory/runs"),
      fetchFromAisdlc("/factory/workflows"),
    ]);

    return {
      capabilities: capabilities || {},
      activeRun: runs?.items?.[0] || null,
      workflows: workflows?.items || [],
    };
  });

  // ── WebSocket endpoint ──
  fastify.register(async (f: any) => {
    f.get("/ws", { websocket: true }, (socket: any, _req: any) => {
      wsClients.add(socket);
      console.log("[factory-dashboard] WebSocket client connected (" + wsClients.size + " total)");

      socket.on("message", (message: any) => {
        try {
          const parsed = JSON.parse(message.toString());
          eventBus.emit("ws:message", parsed);

          // Handle control commands sent via WebSocket
          if (parsed && parsed.command && ["play", "pause", "resume", "stop", "skip"].includes(parsed.command)) {
            const command = parsed.command;
            const runId = parsed.run_id || "run-demo-001";
            const stepId = parsed.step_id;
            const reason = parsed.reason || "";

            const controlEvent = {
              kind: `run.${command}`,
              run_id: runId,
              step: stepId || undefined,
              reason,
              timestamp: new Date().toISOString(),
            };

            eventBus.emit("control:command", controlEvent);

            const ack = {
              command,
              accepted: true,
              run_id: runId,
              message: `Command "${command}" applied to run ${runId}`,
              timestamp: new Date().toISOString(),
            };
            broadcastToWs({
              type: "control_ack",
              timestamp: new Date().toISOString(),
              data: ack,
            });
            broadcastToWs({
              type: "event",
              timestamp: new Date().toISOString(),
              data: controlEvent,
            });

            console.log(`[factory-dashboard] WS Control: ${command} on run ${runId}${stepId ? ` step ${stepId}` : ""}`);
          }
        } catch {
          // Ignore non-JSON messages
        }
      });

      socket.on("close", () => {
        wsClients.delete(socket);
        console.log("[factory-dashboard] WebSocket client disconnected (" + wsClients.size + " total)");
      });

      socket.on("error", (err: any) => {
        console.error("[factory-dashboard] WebSocket error: " + err.message);
        wsClients.delete(socket);
      });
    });
  });

  // ── Heartbeat ──
  const heartbeat = setInterval(() => {
    broadcastToWs({
      type: "heartbeat",
      timestamp: new Date().toISOString(),
      data: { clients: wsClients.size },
    });
  }, 30_000);

  // Unref so it doesn't keep the process alive
  heartbeat.unref();

  // ── Static UI fallback ──
  fastify.setNotFoundHandler(async (_request: any, reply: any) => {
    const uiIndex = join(projectRoot, "dist/ui/index.html");
    if (existsSync(uiIndex)) {
      return reply.type("text/html").sendFile("index.html");
    }
    reply.code(404).send({ error: "Not found" });
  });

  // ── Start listening ──
  try {
    await fastify.listen({ port, host: "127.0.0.1" });
    console.log("");
    console.log("╔═══════════════════════════════════════════════════════════╗");
    console.log("║  AI SDLC Factory Dashboard                                ║");
    console.log("╠═══════════════════════════════════════════════════════════╣");
    console.log("║  Dashboard:  http://127.0.0.1:" + String(port).padEnd(41) + "║");
    console.log("║  WebSocket:  ws://127.0.0.1:" + String(port).padEnd(43) + "║");
    console.log("║  API:        http://127.0.0.1:" + String(port).padEnd(41) + "║");
    console.log("║  AISDLC:     " + aisdlcRoot.padEnd(41) + "║");
    console.log("╚═══════════════════════════════════════════════════════════╝");
    console.log("");
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
}

function createError(status: number, message: string) {
  const error: any = new Error(message);
  error.statusCode = status;
  return error;
}

start().catch(console.error);
