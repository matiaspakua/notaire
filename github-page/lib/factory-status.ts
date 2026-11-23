/**
 * What the AI SDLC kit on this checkout can do (#2201).
 * Plugin names and planned paths are read from the profile. Prose does not invent a sandbox.
 */
import { readFileSync } from "node:fs";
import path from "node:path";

export type InventoryRead = {
  plannedSources: string[];
  counts: { migrated: number; planned: number; stay: number };
};

export function readPluginAllow(toml: string): string[] {
  const match = toml.match(/\[plugins\][^\[]*?allow\s*=\s*\[([^\]]+)\]/);
  if (!match) return [];
  return [...match[1].matchAll(/"([^"]+)"/g)].map((found) => found[1]);
}

export function readInventory(toml: string): InventoryRead {
  const counts = { migrated: 0, planned: 0, stay: 0 };
  const plannedSources: string[] = [];
  for (const block of toml.split(/\n\[\[rows\]\]/).slice(1)) {
    const status = block.match(/status\s*=\s*"([^"]+)"/)?.[1] ?? "";
    const source = block.match(/source\s*=\s*"([^"]+)"/)?.[1] ?? "";
    if (status === "migrated" || status === "planned" || status === "stay") counts[status] += 1;
    if (status === "planned" && source) plannedSources.push(source);
  }
  return { plannedSources, counts };
}

const ROOT = path.resolve(process.cwd(), "..");

export function loadFactoryNow() {
  const profile = readFileSync(path.join(ROOT, ".aisdlc/aisdlc.toml"), "utf8");
  const inventory = readFileSync(path.join(ROOT, ".aisdlc/migration-inventory.toml"), "utf8");
  const parsed = readInventory(inventory);
  return {
    loadedPlugins: readPluginAllow(profile),
    plannedSources: parsed.plannedSources,
    counts: parsed.counts,
    commands: ["install", "config", "plugin", "grant", "isolator", "foreman", "metrics", "line", "status", "intent", "run", "harness", "workflow"],
    en: {
      title: "What is on main today",
      lead: "These commands exist in this repository. Planned work is labeled planned. A missing tool is not described as if it were running.",
      loaded: "Loaded plugins",
      loadedNote: "Read from .aisdlc/aisdlc.toml [plugins].allow. plugin plan prints an argv list and does not run it. Spec Kit and AWS AI-DLC are in the tree and are not loaded.",
      shippedTitle: "Shipped on main",
      plannedTitle: "Still planned",
      isolator:
        "isolator status, probe and run. sandbox is true only after a successful bubblewrap probe. If bubblewrap is missing, the command reports sandbox false. A Grant expire or write-set check is policy, not a sandbox.",
      grant: "grant list, show and check compare changed paths with a write set. An expiry fails closed (#2192, #2194, #2198).",
      foreman: "foreman plan prints a dispatch plan from the stage knobs and does not run agents or MCP (#2196).",
      leadTime: "metrics lead-time reads a fixture or, read-only, GitHub through gh. It does not write to the forge (#2184).",
      config: "config validate and config show --redacted, plus the migration inventory check (#2121, merged in #2127). Routing for hexagonal work stays on the cloud fleet (#2086, merged in #2092).",
      approval: "ApprovalPort is a design. doctor reports no-evaluable and approvals are not live. Planned until a real custody runtime exists.",
      line:
        "line, status and the AI SDLC Factory dashboard draw the production line from the evidence ledger: a station per stage with its kind, gates with their last result, the correction loop, throughput and cost (#2407, #2411). Each station lists what the run's steps received and produced, with repository links (#2435).",
      runs:
        "intent submit, run start --intent and harness report record an intent, attach runs to it and append each step to the ledger; workflow edit gives every step its station (#2395, #2410, #2426).",
      gate:
        "The pre-push gate runs only the suite groups a branch touches and remembers a commit that already passed: a docs-only push takes under a minute instead of about 35 (#2413).",
      counts: "Inventory rows",
    },
    es: {
      title: "Qué hay en main hoy",
      lead: "Estos comandos existen en este repositorio. Lo que no está hecho se marca como planificado. Si una herramienta no está, no se describe como si estuviera corriendo.",
      loaded: "Plugins cargados",
      loadedNote: "Leído de .aisdlc/aisdlc.toml [plugins].allow. plugin plan imprime un argv y no lo ejecuta. Spec Kit y AWS AI-DLC están en el árbol y no están cargados.",
      shippedTitle: "En main",
      plannedTitle: "Todavía planificado",
      isolator:
        "isolator status, probe y run. sandbox es true solo después de un probe exitoso de bubblewrap. Si bubblewrap no está, el comando informa sandbox false. Un Grant que vence o revisa el write set es política, no un sandbox.",
      grant: "grant list, show y check comparan las rutas cambiadas con un write set. Si el Grant venció, falla cerrado (#2192, #2194, #2198).",
      foreman: "foreman plan imprime un plan de despacho a partir de los knobs del stage y no ejecuta agentes ni MCP (#2196).",
      leadTime: "metrics lead-time lee un fixture o, en solo lectura, GitHub mediante gh. No escribe en el forge (#2184).",
      config: "config validate y config show --redacted, más el chequeo del inventario de migración (#2121, fusionado en #2127). El trabajo hexagonal sigue enrutado a la flota cloud (#2086, fusionado en #2092).",
      approval: "ApprovalPort es un diseño. doctor informa no-evaluable y las aprobaciones no están vivas. Planificado hasta que exista un runtime de custodia real.",
      line:
        "line, status y el panel AI SDLC Factory dibujan la línea de producción desde el registro de evidencia: una estación por etapa con su tipo, los gates con su último resultado, el bucle de corrección, el rendimiento y el costo (#2407, #2411). Cada estación muestra qué recibieron y qué produjeron los pasos de la corrida, con enlaces al repositorio (#2435).",
      runs:
        "intent submit, run start --intent y harness report registran una intención, le asocian corridas y agregan cada paso al registro; workflow edit le da a cada paso su estación (#2395, #2410, #2426).",
      gate:
        "El gate de pre-push corre solo los grupos de suites que toca la rama y recuerda un commit que ya pasó: un push solo de documentación tarda menos de un minuto en lugar de unos 35 (#2413).",
      counts: "Filas del inventario",
    },
  };
}

export type FactoryNow = ReturnType<typeof loadFactoryNow>;
