/**
 * Factory Dashboard — Persistent storage layer.
 *
 * Reads from `.aisdlc/` directories to provide historical data even without
 * an active intent. Supports single and multi-project setups.
 *
 * Storage layout:
 *   .aisdlc/evidence/ledger.jsonl  — stage run evidence
 *   .aisdlc/evidence/cost.jsonl    — cost entries
 *   .aisdlc/runs/                  — run requests
 *   .aisdlc/projects/              — project definitions
 *   .aisdlc/catalogs/              — agents, skills, MCP, workflows
 *   .aisdlc/aisdlc.toml            — profile config
 */

import {
  readFileSync,
  readdirSync,
  existsSync,
  statSync,
} from "fs";
import { join, dirname } from "path";

// ───────────────────── Types ─────────────────────

export interface EvidenceRecord {
  ts: string;
  /** The intent this evidence belongs to — primary tracker */
  intent_id?: string;
  stage: string;
  ok: boolean;
  commit?: string;
  base?: string;
  gates?: Array<{
    name: string;
    status: string;
    seconds: number;
    exit: number;
  }>;
  [key: string]: unknown;
}

export interface CostEntry {
  ts: string;
  /** The intent this cost belongs to — primary tracker */
  intent_id?: string;
  agent: string;
  model: string;
  input: number;
  output: number;
  usd: number | null;
  issue?: string;
  [key: string]: unknown;
}

export interface RunEntry {
  id: string;
  /** The intent this run is fulfilling — primary tracker */
  intent_id?: string;
  project: string;
  workflow: { id: string; version: number; digest: string };
  status: string;
  requested_by: string;
  requested_at: string;
  steps?: Array<{ id: string; status: string }>;
  [key: string]: unknown;
}

export interface ProjectEntry {
  id: string;
  profile_ref: string;
  repos: Array<{
    id: string;
    forge: string;
    url: string;
    default_branch: string;
    mode: string;
  }>;
  created_at: string;
  [key: string]: unknown;
}

export interface CatalogEntry {
  id: string;
  version: number;
  [key: string]: unknown;
}

export interface StorageSummary {
  aisdlc_root: string;
  evidence_count: number;
  cost_count: number;
  run_count: number;
  project_count: number;
  agent_count: number;
  skill_count: number;
  mcp_count: number;
  workflow_count: number;
  stages: Record<string, number>;
  costs_by_model: Record<string, { entries: number; usd: number }>;
  costs_by_agent: Record<string, number>;
  costs_by_intent: Record<string, { entries: number; usd: number }>;
  evidence_by_intent: Record<string, number>;
  total_usd: number;
}

// ───────────────────── Storage class ─────────────────────

export class PersistentStorage {
  private aisdlcRoot: string;

  constructor(projectRoot: string) {
    this.aisdlcRoot = join(projectRoot, ".aisdlc");
  }

  /** Check if the .aisdlc directory exists */
  exists(): boolean {
    return existsSync(this.aisdlcRoot);
  }

  /** Get the .aisdlc root path */
  getRoot(): string {
    return this.aisdlcRoot;
  }

  // ── Evidence ──

  /** Read all evidence records from ledger.jsonl */
  readEvidence(): EvidenceRecord[] {
    const path = join(this.aisdlcRoot, "evidence", "ledger.jsonl");
    if (!existsSync(path)) return [];

    const lines = readFileSync(path, "utf-8")
      .split("\n")
      .filter((l) => l.trim());

    const records: EvidenceRecord[] = [];
    for (const line of lines) {
      try {
        records.push(JSON.parse(line));
      } catch {
        // Skip malformed lines
      }
    }
    return records;
  }

  /** Read cost entries from cost.jsonl */
  readCosts(): CostEntry[] {
    const path = join(this.aisdlcRoot, "evidence", "cost.jsonl");
    if (!existsSync(path)) return [];

    const lines = readFileSync(path, "utf-8")
      .split("\n")
      .filter((l) => l.trim());

    const entries: CostEntry[] = [];
    for (const line of lines) {
      try {
        entries.push(JSON.parse(line));
      } catch {
        // Skip malformed lines
      }
    }
    return entries;
  }

  // ── Runs ──

  /** Read all run entries */
  readRuns(): RunEntry[] {
    const runsDir = join(this.aisdlcRoot, "runs");
    if (!existsSync(runsDir)) return [];

    const entries: RunEntry[] = [];
    try {
      const items = readdirSync(runsDir);
      for (const item of items) {
        const currentPath = join(runsDir, item, "current.json");
        if (existsSync(currentPath)) {
          try {
            const data = JSON.parse(readFileSync(currentPath, "utf-8"));
            if (data && data.id) {
              entries.push(data);
            }
          } catch {
            // Skip malformed run files
          }
        }
      }
    } catch {
      // Directory read error
    }
    return entries;
  }

  // ── Projects ──

  /** Read all project entries */
  readProjects(): ProjectEntry[] {
    const projectsDir = join(this.aisdlcRoot, "projects");
    if (!existsSync(projectsDir)) return [];

    const entries: ProjectEntry[] = [];
    try {
      const items = readdirSync(projectsDir);
      for (const item of items) {
        const currentPath = join(projectsDir, item, "current.json");
        if (existsSync(currentPath)) {
          try {
            const data = JSON.parse(readFileSync(currentPath, "utf-8"));
            if (data && data.id) {
              entries.push(data);
            }
          } catch {
            // Skip malformed project files
          }
        }
      }
    } catch {
      // Directory read error
    }
    return entries;
  }

  // ── Catalogs ──

  /** Read all catalog entries of a given type (agents, skills, mcp, workflows) */
  readCatalogs(type: "agents" | "skills" | "mcp" | "workflows"): CatalogEntry[] {
    const catalogDir = join(this.aisdlcRoot, "catalogs", type);
    if (!existsSync(catalogDir)) return [];

    const entries: CatalogEntry[] = [];
    try {
      const items = readdirSync(catalogDir);
      for (const item of items) {
        const currentPath = join(catalogDir, item, "current.json");
        if (existsSync(currentPath)) {
          try {
            const data = JSON.parse(readFileSync(currentPath, "utf-8"));
            if (data) {
              entries.push({ id: item, version: data.version || 1, ...data });
            }
          } catch {
            // Skip malformed catalog files
          }
        }
      }
    } catch {
      // Directory read error
    }
    return entries;
  }

  // ── Summary ──

  /** Compute a summary of all stored data */
  summary(): StorageSummary {
    const evidence = this.readEvidence();
    const costs = this.readCosts();
    const runs = this.readRuns();
    const projects = this.readProjects();
    const agents = this.readCatalogs("agents");
    const skills = this.readCatalogs("skills");
    const mcp = this.readCatalogs("mcp");
    const workflows = this.readCatalogs("workflows");

    // Stage counts
    const stages: Record<string, number> = {};
    for (const record of evidence) {
      const stage = record.stage || "unknown";
      stages[stage] = (stages[stage] || 0) + 1;
    }

    // Cost aggregation by model, agent, and intent
    const costs_by_model: Record<string, { entries: number; usd: number }> = {};
    const costs_by_agent: Record<string, number> = {};
    const costs_by_intent: Record<string, { entries: number; usd: number }> = {};
    let total_usd = 0;

    for (const entry of costs) {
      const model = entry.model || "unknown";
      if (!costs_by_model[model]) {
        costs_by_model[model] = { entries: 0, usd: 0 };
      }
      costs_by_model[model].entries++;
      costs_by_model[model].usd += entry.usd || 0;

      const agent = entry.agent || "unknown";
      costs_by_agent[agent] = (costs_by_agent[agent] || 0) + (entry.usd || 0);

      // Track by intent_id (primary tracker)
      const intentKey = entry.intent_id || entry.issue || "untracked";
      if (!costs_by_intent[intentKey]) {
        costs_by_intent[intentKey] = { entries: 0, usd: 0 };
      }
      costs_by_intent[intentKey].entries++;
      costs_by_intent[intentKey].usd += entry.usd || 0;

      total_usd += entry.usd || 0;
    }

    // Evidence grouped by intent
    const evidence_by_intent: Record<string, number> = {};
    for (const record of evidence) {
      const intentKey = record.intent_id || "untracked";
      evidence_by_intent[intentKey] = (evidence_by_intent[intentKey] || 0) + 1;
    }

    return {
      aisdlc_root: this.aisdlcRoot,
      evidence_count: evidence.length,
      cost_count: costs.length,
      run_count: runs.length,
      project_count: projects.length,
      agent_count: agents.length,
      skill_count: skills.length,
      mcp_count: mcp.length,
      workflow_count: workflows.length,
      stages,
      costs_by_model,
      costs_by_agent,
      costs_by_intent,
      evidence_by_intent,
      total_usd: Math.round(total_usd * 1000000) / 1000000,
    };
  }

  /** Get evidence records grouped by stage */
  evidenceByStage(): Record<string, EvidenceRecord[]> {
    const evidence = this.readEvidence();
    const grouped: Record<string, EvidenceRecord[]> = {};

    for (const record of evidence) {
      const stage = record.stage || "unknown";
      if (!grouped[stage]) grouped[stage] = [];
      grouped[stage].push(record);
    }

    return grouped;
  }

  /** Get the latest N evidence records */
  recentEvidence(limit: number = 50): EvidenceRecord[] {
    const evidence = this.readEvidence();
    return evidence.slice(-limit);
  }

  /** Get cost entries grouped by issue */
  costsByIssue(): Record<string, CostEntry[]> {
    const costs = this.readCosts();
    const grouped: Record<string, CostEntry[]> = {};

    for (const entry of costs) {
      const issue = entry.issue || "untracked";
      if (!grouped[issue]) grouped[issue] = [];
      grouped[issue].push(entry);
    }

    return grouped;
  }

  /** Get cost entries grouped by intent_id (primary tracker) */
  costsByIntent(): Record<string, CostEntry[]> {
    const costs = this.readCosts();
    const grouped: Record<string, CostEntry[]> = {};

    for (const entry of costs) {
      const intentKey = entry.intent_id || entry.issue || "untracked";
      if (!grouped[intentKey]) grouped[intentKey] = [];
      grouped[intentKey].push(entry);
    }

    return grouped;
  }

  /** Get evidence records grouped by intent_id (primary tracker) */
  evidenceByIntent(): Record<string, EvidenceRecord[]> {
    const evidence = this.readEvidence();
    const grouped: Record<string, EvidenceRecord[]> = {};

    for (const record of evidence) {
      const intentKey = record.intent_id || "untracked";
      if (!grouped[intentKey]) grouped[intentKey] = [];
      grouped[intentKey].push(record);
    }

    return grouped;
  }

  /** Get all runs grouped by intent_id */
  runsByIntent(): Record<string, RunEntry[]> {
    const runs = this.readRuns();
    const grouped: Record<string, RunEntry[]> = {};

    for (const run of runs) {
      const intentKey = run.intent_id || "untracked";
      if (!grouped[intentKey]) grouped[intentKey] = [];
      grouped[intentKey].push(run);
    }

    return grouped;
  }

  /** Get costs for a specific intent */
  costsForIntent(intentId: string): CostEntry[] {
    const costs = this.readCosts();
    return costs.filter(
      (entry) => entry.intent_id === intentId || entry.issue === intentId
    );
  }

  /** Get evidence for a specific intent */
  evidenceForIntent(intentId: string): EvidenceRecord[] {
    const evidence = this.readEvidence();
    return evidence.filter((record) => record.intent_id === intentId);
  }

  /** Get runs for a specific intent */
  runsForIntent(intentId: string): RunEntry[] {
    const runs = this.readRuns();
    return runs.filter((run) => run.intent_id === intentId);
  }
}
