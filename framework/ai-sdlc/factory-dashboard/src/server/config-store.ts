/**
 * Factory Configuration Store
 *
 * Discovers and manages factory configuration: agents, rules, MCP integrations,
 * and skills. Reads from the project's .claude/ and .aisdlc/ directories,
 * provides in-memory CRUD with file persistence.
 */
import {
  readdirSync,
  readFileSync,
  writeFileSync,
  existsSync,
  statSync,
} from "fs";
import { join, basename } from "path";

// ───────────────────── Type definitions ─────────────────────

export type AgentProvider = "anthropic" | "openai" | "google" | "microsoft" | "openrouter" | "claude" | "cursor" | "codex" | "agy" | "kiro" | "opencode" | "custom";

// ───────────────────── AI Providers ─────────────────────

export type AiProviderId = "anthropic" | "openai" | "google" | "microsoft" | "openrouter" | "custom";
export type AuthMethod = "api_key" | "service_account" | "oauth" | "token";

export interface AiModelConfig {
  id: string;
  name: string;
  displayName: string;
  enabled: boolean;
  maxInputTokens: number;
  maxOutputTokens: number;
  pricing?: {
    inputPer1k: number;
    outputPer1k: number;
    currency: string;
  };
  capabilities?: string[];
}

export interface AiProviderConfig {
  id: string;
  provider: AiProviderId;
  name: string;
  description: string;
  enabled: boolean;
  authMethod: AuthMethod;
  authConfigured: boolean;
  apiKeySet: boolean;
  models: AiModelConfig[];
  defaultModel?: string;
  usageCredits?: {
    monthlyLimit: number;
    currentUsage: number;
    currency: string;
  };
  associatedAgents?: string[];
  created_at: string;
  updated_at: string;
}

export interface AgentConfig {
  id: string;
  name: string;
  provider: AgentProvider;
  role: string;
  description: string;
  enabled: boolean;
  model?: string;
  max_tokens?: number;
  temperature?: number;
  system_prompt?: string;
  allowed_tools?: string[];
  associated_stations?: string[];
  created_at: string;
  updated_at: string;
}

export type RuleCategory = "workflow" | "code-quality" | "security" | "architecture" | "migration" | "ui-ux" | "general";

export interface RuleConfig {
  id: string;
  name: string;
  category: RuleCategory;
  description: string;
  enabled: boolean;
  file_path: string;
  severity: "error" | "warning" | "info";
  associated_agents?: string[];
  associated_stations?: string[];
  created_at: string;
  updated_at: string;
}

export type McpTransport = "stdio" | "http" | "websocket";

export interface McpServerConfig {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  transport: McpTransport;
  command?: string;
  args?: string[];
  url?: string;
  env?: Record<string, string>;
  tools?: string[];
  associated_agents?: string[];
  associated_stations?: string[];
  health_status?: "healthy" | "unhealthy" | "unknown";
  last_health_check?: string;
  created_at: string;
  updated_at: string;
}

export interface SkillConfig {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  file_path: string;
  category?: string;
  tags?: string[];
  associated_agents?: string[];
  associated_stations?: string[];
  is_generic: boolean;
  created_at: string;
  updated_at: string;
}

// ───────────────────── Discovery ─────────────────────

function nowIso() {
  return new Date().toISOString();
}

function readFirstLines(filePath: string, lines: number = 30): string {
  try {
    const content = readFileSync(filePath, "utf-8");
    return content.split("\n").slice(0, lines).join("\n");
  } catch {
    return "";
  }
}

function extractDescriptionFromMarkdown(content: string): string {
  // Look for the first non-empty line that's not a heading or frontmatter
  const lines = content.split("\n").filter(l => l.trim());
  for (const line of lines) {
    const trimmed = line.trim();
    if (trimmed.startsWith("#") || trimmed.startsWith("---") || trimmed.startsWith("```")) continue;
    if (trimmed.length > 200) continue;
    return trimmed.replace(/[*_`]/g, "").slice(0, 200);
  }
  return "";
}

function extractSkillNameFromPath(path: string): string {
  // .claude/skills/<name>/SKILL.md → <name>
  const parts = path.split("/");
  const idx = parts.indexOf("skills");
  if (idx >= 0 && idx + 1 < parts.length) {
    return parts[idx + 1];
  }
  return basename(path, ".md");
}

function extractRuleNameFromPath(path: string): string {
  // .claude/rules/<name>.md → <name>
  return basename(path, ".md").replace(/-/g, " ");
}

function categorizeRule(name: string): RuleCategory {
  const lower = name.toLowerCase();
  if (lower.includes("workflow") || lower.includes("agent")) return "workflow";
  if (lower.includes("code") || lower.includes("quality") || lower.includes("programming")) return "code-quality";
  if (lower.includes("security") || lower.includes("threat")) return "security";
  if (lower.includes("hex") || lower.includes("arch")) return "architecture";
  if (lower.includes("migration") || lower.includes("database") || lower.includes("flyway")) return "migration";
  if (lower.includes("ui") || lower.includes("ux") || lower.includes("design")) return "ui-ux";
  if (lower.includes("refactor")) return "code-quality";
  return "general";
}

function categorizeSkill(name: string): string {
  const lower = name.toLowerCase();
  if (lower.includes("frontend") || lower.includes("ui") || lower.includes("design")) return "Frontend";
  if (lower.includes("backend") || lower.includes("java") || lower.includes("api")) return "Backend";
  if (lower.includes("security") || lower.includes("threat")) return "Security";
  if (lower.includes("devops") || lower.includes("ci") || lower.includes("cd")) return "DevOps";
  if (lower.includes("test") || lower.includes("qa")) return "Testing";
  if (lower.includes("arch") || lower.includes("hex")) return "Architecture";
  if (lower.includes("openspec")) return "Process";
  if (lower.includes("project") || lower.includes("delivery")) return "Management";
  return "General";
}

// ───────────────────── Store ─────────────────────

export class ConfigStore {
  private agents: Map<string, AgentConfig> = new Map();
  private rules: Map<string, RuleConfig> = new Map();
  private mcpServers: Map<string, McpServerConfig> = new Map();
  private skills: Map<string, SkillConfig> = new Map();
  private aiProviders: Map<string, AiProviderConfig> = new Map();

  constructor(private projectRoot: string) {}

  /** Discover all configuration from the project */
  discover() {
    this.discoverAiProviders();
    this.discoverAgents();
    this.discoverRules();
    this.discoverMcpServers();
    this.discoverSkills();
  }

  // ── AI Providers ──

  private discoverAiProviders() {
    // Check environment for configured API keys
    const hasAnthropic = !!process.env.ANTHROPIC_API_KEY;
    const hasOpenAI = !!process.env.OPENAI_API_KEY;
    const hasGoogle = !!process.env.GOOGLE_API_KEY || !!process.env.GEMINI_API_KEY;
    const hasMicrosoft = !!process.env.AZURE_OPENAI_API_KEY;
    const hasOpenRouter = !!process.env.OPENROUTER_API_KEY;

    const knownProviders: Array<{
      id: string;
      provider: AiProviderId;
      name: string;
      description: string;
      authMethod: AuthMethod;
      models: AiModelConfig[];
      defaultModel?: string;
    }> = [
      {
        id: "provider-anthropic",
        provider: "anthropic",
        name: "Anthropic",
        description: "Claude models — Claude Code, Sonnet, Opus, Haiku. Best for complex reasoning and code generation.",
        authMethod: "api_key",
        models: [
          {
            id: "claude-sonnet-4-20250514",
            name: "claude-sonnet-4-20250514",
            displayName: "Claude Sonnet 4",
            enabled: true,
            maxInputTokens: 200000,
            maxOutputTokens: 8192,
            pricing: { inputPer1k: 0.003, outputPer1k: 0.015, currency: "USD" },
            capabilities: ["code", "reasoning", "tools"],
          },
          {
            id: "claude-opus-4-20250514",
            name: "claude-opus-4-20250514",
            displayName: "Claude Opus 4",
            enabled: true,
            maxInputTokens: 200000,
            maxOutputTokens: 8192,
            pricing: { inputPer1k: 0.015, outputPer1k: 0.075, currency: "USD" },
            capabilities: ["code", "reasoning", "tools", "advanced"],
          },
          {
            id: "claude-haiku-4-20250514",
            name: "claude-haiku-4-20250514",
            displayName: "Claude Haiku 4",
            enabled: true,
            maxInputTokens: 200000,
            maxOutputTokens: 8192,
            pricing: { inputPer1k: 0.0008, outputPer1k: 0.004, currency: "USD" },
            capabilities: ["code", "fast"],
          },
        ],
        defaultModel: "claude-sonnet-4-20250514",
      },
      {
        id: "provider-openai",
        provider: "openai",
        name: "OpenAI",
        description: "GPT models — Codex, GPT-4o, o1. Strong code generation and reasoning capabilities.",
        authMethod: "api_key",
        models: [
          {
            id: "gpt-4o-2024-11-20",
            name: "gpt-4o-2024-11-20",
            displayName: "GPT-4o",
            enabled: true,
            maxInputTokens: 128000,
            maxOutputTokens: 16384,
            pricing: { inputPer1k: 0.0025, outputPer1k: 0.01, currency: "USD" },
            capabilities: ["code", "reasoning", "tools"],
          },
          {
            id: "o1-2024-12-17",
            name: "o1-2024-12-17",
            displayName: "o1",
            enabled: true,
            maxInputTokens: 200000,
            maxOutputTokens: 100000,
            pricing: { inputPer1k: 0.015, outputPer1k: 0.06, currency: "USD" },
            capabilities: ["code", "reasoning", "advanced"],
          },
          {
            id: "o3-mini-2025-01-31",
            name: "o3-mini-2025-01-31",
            displayName: "o3-mini",
            enabled: true,
            maxInputTokens: 200000,
            maxOutputTokens: 100000,
            pricing: { inputPer1k: 0.0011, outputPer1k: 0.0044, currency: "USD" },
            capabilities: ["code", "reasoning"],
          },
        ],
        defaultModel: "gpt-4o-2024-11-20",
      },
      {
        id: "provider-google",
        provider: "google",
        name: "Google",
        description: "Gemini models — Gemini 2.5 Pro, Flash. Strong multimodal and code capabilities.",
        authMethod: "api_key",
        models: [
          {
            id: "gemini-2.5-pro-preview-06-05",
            name: "gemini-2.5-pro-preview-06-05",
            displayName: "Gemini 2.5 Pro",
            enabled: true,
            maxInputTokens: 1048576,
            maxOutputTokens: 65536,
            pricing: { inputPer1k: 0.00125, outputPer1k: 0.005, currency: "USD" },
            capabilities: ["code", "reasoning", "tools", "multimodal"],
          },
          {
            id: "gemini-2.5-flash-preview-05-26",
            name: "gemini-2.5-flash-preview-05-26",
            displayName: "Gemini 2.5 Flash",
            enabled: true,
            maxInputTokens: 1048576,
            maxOutputTokens: 65536,
            pricing: { inputPer1k: 0.00015, outputPer1k: 0.0006, currency: "USD" },
            capabilities: ["code", "fast"],
          },
        ],
        defaultModel: "gemini-2.5-pro-preview-06-05",
      },
      {
        id: "provider-microsoft",
        provider: "microsoft",
        name: "Microsoft Azure AI",
        description: "Azure OpenAI Service — enterprise-grade with service account authentication.",
        authMethod: "service_account",
        models: [
          {
            id: "gpt-4o",
            name: "gpt-4o",
            displayName: "Azure GPT-4o",
            enabled: true,
            maxInputTokens: 128000,
            maxOutputTokens: 16384,
            capabilities: ["code", "reasoning", "tools"],
          },
          {
            id: "gpt-4",
            name: "gpt-4",
            displayName: "Azure GPT-4",
            enabled: true,
            maxInputTokens: 128000,
            maxOutputTokens: 4096,
            capabilities: ["code", "reasoning"],
          },
        ],
        defaultModel: "gpt-4o",
      },
      {
        id: "provider-openrouter",
        provider: "openrouter",
        name: "OpenRouter",
        description: "Unified API for multiple model providers — access many models through a single key.",
        authMethod: "api_key",
        models: [
          {
            id: "anthropic/claude-sonnet-4",
            name: "anthropic/claude-sonnet-4",
            displayName: "Claude Sonnet 4 (via OpenRouter)",
            enabled: true,
            maxInputTokens: 200000,
            maxOutputTokens: 8192,
            capabilities: ["code", "reasoning", "tools"],
          },
          {
            id: "openai/gpt-4o",
            name: "openai/gpt-4o",
            displayName: "GPT-4o (via OpenRouter)",
            enabled: true,
            maxInputTokens: 128000,
            maxOutputTokens: 16384,
            capabilities: ["code", "reasoning", "tools"],
          },
        ],
        defaultModel: "anthropic/claude-sonnet-4",
      },
    ];

    const providerAuthStatus: Record<string, { authConfigured: boolean; apiKeySet: boolean }> = {
      "provider-anthropic": { authConfigured: hasAnthropic, apiKeySet: hasAnthropic },
      "provider-openai": { authConfigured: hasOpenAI, apiKeySet: hasOpenAI },
      "provider-google": { authConfigured: hasGoogle, apiKeySet: hasGoogle },
      "provider-microsoft": { authConfigured: hasMicrosoft, apiKeySet: hasMicrosoft },
      "provider-openrouter": { authConfigured: hasOpenRouter, apiKeySet: hasOpenRouter },
    };

    for (const provider of knownProviders) {
      const existing = this.aiProviders.get(provider.id);
      const auth = providerAuthStatus[provider.id];
      if (existing) {
        existing.authConfigured = auth.authConfigured;
        existing.apiKeySet = auth.apiKeySet;
        existing.updated_at = nowIso();
      } else {
        this.aiProviders.set(provider.id, {
          ...provider,
          enabled: auth.authConfigured,
          authConfigured: auth.authConfigured,
          apiKeySet: auth.apiKeySet,
          usageCredits: {
            monthlyLimit: 0,
            currentUsage: 0,
            currency: "USD",
          },
          created_at: nowIso(),
          updated_at: nowIso(),
        });
      }
    }
  }

  // ── Agents ──

  private discoverAgents() {
    // Known agents from the factory
    const knownAgents: Array<{ id: string; name: string; provider: AgentProvider; role: string; description: string }> = [
      { id: "agent-owner", name: "Owner", provider: "claude", role: "Owner & Architect", description: "Primary agent for architectural decisions, policy enforcement, and merge authority" },
      { id: "agent-foreman", name: "Foreman", provider: "cursor", role: "Cloud Foreman", description: "Coordinates cloud fleet, manages worker assignments and merge rules" },
      { id: "agent-codex", name: "Codex CLI", provider: "codex", role: "Code Generator", description: "OpenAI Codex CLI for code generation and refactoring tasks" },
      { id: "agent-agy", name: "AGY CLI", provider: "agy", role: "Code Generator", description: "AGY CLI for autonomous coding tasks with high recall" },
      { id: "agent-kiro", name: "Kiro CLI", provider: "kiro", role: "Code Generator", description: "Kiro CLI for interactive and agent-mode coding" },
      { id: "agent-opencode", name: "Opencode CLI", provider: "opencode", role: "Code Generator", description: "Opencode CLI for non-interactive coding with JSON output" },
      { id: "agent-scaffolder", name: "Scaffolder", provider: "custom", role: "Project Setup", description: "Creates project structures, configurations, and boilerplate" },
      { id: "agent-security", name: "Security Auditor", provider: "custom", role: "Security Review", description: "Performs security audits, threat modeling, and vulnerability assessment" },
      { id: "agent-architect", name: "Architecture Critic", provider: "custom", role: "Architecture Review", description: "Reviews architectural decisions and hexagonal compliance" },
      { id: "agent-tester", name: "Test Engineer", provider: "custom", role: "Test Automation", description: "Creates and maintains test suites, TDD cycles" },
    ];

    for (const agent of knownAgents) {
      const existing = this.agents.get(agent.id);
      if (existing) {
        existing.updated_at = nowIso();
      } else {
        this.agents.set(agent.id, {
          ...agent,
          enabled: true,
          created_at: nowIso(),
          updated_at: nowIso(),
        });
      }
    }
  }

  // ── Rules ──

  private discoverRules() {
    const rulesDir = join(this.projectRoot, ".claude", "rules");
    if (!existsSync(rulesDir)) return;

    for (const file of readdirSync(rulesDir)) {
      if (!file.endsWith(".md")) continue;
      const filePath = join(rulesDir, file);
      const relPath = `.claude/rules/${file}`;
      const name = extractRuleNameFromPath(file);
      const content = readFirstLines(filePath);
      const description = extractDescriptionFromMarkdown(content) || name;
      const category = categorizeRule(name);
      const id = `rule-${basename(file, ".md")}`;

      const existing = this.rules.get(id);
      if (existing) {
        existing.file_path = relPath;
        existing.updated_at = nowIso();
      } else {
        this.rules.set(id, {
          id,
          name,
          category,
          description,
          enabled: true,
          file_path: relPath,
          severity: "warning",
          created_at: nowIso(),
          updated_at: nowIso(),
        });
      }
    }
  }

  // ── MCP Servers ──

  private discoverMcpServers() {
    // Known MCP servers from the project
    const knownMcp: Array<{ id: string; name: string; description: string; transport: McpTransport; command?: string; args?: string[] }> = [
      {
        id: "mcp-repolens",
        name: "RepoLens",
        description: "Repository knowledge graph for code navigation and impact analysis",
        transport: "stdio",
        command: "bash",
        args: ["framework/ai-sdlc/repolens/bin/repolens", "mcp"],
      },
      {
        id: "mcp-factory",
        name: "Factory",
        description: "AI SDLC factory control and monitoring",
        transport: "stdio",
      },
      {
        id: "mcp-idea",
        name: "IDE Integration",
        description: "JetBrains IDE integration for real-time context",
        transport: "stdio",
      },
    ];

    for (const mcp of knownMcp) {
      const existing = this.mcpServers.get(mcp.id);
      if (existing) {
        existing.updated_at = nowIso();
      } else {
        this.mcpServers.set(mcp.id, {
          ...mcp,
          enabled: true,
          health_status: "unknown",
          created_at: nowIso(),
          updated_at: nowIso(),
        });
      }
    }
  }

  // ── Skills ──

  private discoverSkills() {
    const skillsDir = join(this.projectRoot, ".claude", "skills");
    if (!existsSync(skillsDir)) return;

    // Read generic skills from aisdlc.toml
    const genericSkills = this.readGenericSkillsFromConfig();

    for (const entry of readdirSync(skillsDir, { withFileTypes: true })) {
      if (!entry.isDirectory()) continue;
      const skillDir = join(skillsDir, entry.name);
      const skillFile = join(skillDir, "SKILL.md");
      if (!existsSync(skillFile)) continue;

      const relPath = `.claude/skills/${entry.name}/SKILL.md`;
      const content = readFirstLines(skillFile);
      const description = extractDescriptionFromMarkdown(content) || entry.name;
      const category = categorizeSkill(entry.name);
      const isGeneric = genericSkills.has(entry.name);
      const id = `skill-${entry.name}`;

      const existing = this.skills.get(id);
      if (existing) {
        existing.file_path = relPath;
        existing.is_generic = isGeneric;
        existing.updated_at = nowIso();
      } else {
        this.skills.set(id, {
          id,
          name: entry.name,
          description,
          enabled: true,
          file_path: relPath,
          category,
          tags: [category.toLowerCase()],
          is_generic: isGeneric,
          created_at: nowIso(),
          updated_at: nowIso(),
        });
      }
    }
  }

  private readGenericSkillsFromConfig(): Set<string> {
    const configPath = join(this.projectRoot, ".aisdlc", "aisdlc.toml");
    if (!existsSync(configPath)) return new Set();

    try {
      const content = readFileSync(configPath, "utf-8");
      const generics: Set<string> = new Set();
      const lines = content.split("\n");
      let inGeneric = false;

      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed === "generic = [") {
          inGeneric = true;
          continue;
        }
        if (inGeneric) {
          if (trimmed === "]") {
            inGeneric = false;
            break;
          }
          const skill = trimmed.replace(/["',\s]/g, "");
          if (skill) generics.add(skill);
        }
      }
      return generics;
    } catch {
      return new Set();
    }
  }

  // ── CRUD: Agents ──

  getAgents(): AgentConfig[] {
    return Array.from(this.agents.values());
  }

  getAgent(id: string): AgentConfig | undefined {
    return this.agents.get(id);
  }

  createAgent(agent: Omit<AgentConfig, "id" | "created_at" | "updated_at"> & { id?: string }): AgentConfig {
    const id = agent.id || `agent-${Date.now()}`;
    const now = nowIso();
    const config: AgentConfig = { ...agent, id, created_at: now, updated_at: now };
    this.agents.set(id, config);
    return config;
  }

  updateAgent(id: string, updates: Partial<AgentConfig>): AgentConfig | undefined {
    const existing = this.agents.get(id);
    if (!existing) return undefined;
    const updated = { ...existing, ...updates, id, updated_at: nowIso() };
    this.agents.set(id, updated);
    return updated;
  }

  deleteAgent(id: string): boolean {
    return this.agents.delete(id);
  }

  // ── CRUD: Rules ──

  getRules(): RuleConfig[] {
    return Array.from(this.rules.values());
  }

  getRule(id: string): RuleConfig | undefined {
    return this.rules.get(id);
  }

  createRule(rule: Omit<RuleConfig, "id" | "created_at" | "updated_at"> & { id?: string }): RuleConfig {
    const id = rule.id || `rule-${Date.now()}`;
    const now = nowIso();
    const config: RuleConfig = { ...rule, id, created_at: now, updated_at: now };
    this.rules.set(id, config);
    return config;
  }

  updateRule(id: string, updates: Partial<RuleConfig>): RuleConfig | undefined {
    const existing = this.rules.get(id);
    if (!existing) return undefined;
    const updated = { ...existing, ...updates, id, updated_at: nowIso() };
    this.rules.set(id, updated);
    return updated;
  }

  deleteRule(id: string): boolean {
    return this.rules.delete(id);
  }

  // ── CRUD: MCP Servers ──

  getMcpServers(): McpServerConfig[] {
    return Array.from(this.mcpServers.values());
  }

  getMcpServer(id: string): McpServerConfig | undefined {
    return this.mcpServers.get(id);
  }

  createMcpServer(server: Omit<McpServerConfig, "id" | "created_at" | "updated_at"> & { id?: string }): McpServerConfig {
    const id = server.id || `mcp-${Date.now()}`;
    const now = nowIso();
    const config: McpServerConfig = { ...server, id, created_at: now, updated_at: now };
    this.mcpServers.set(id, config);
    return config;
  }

  updateMcpServer(id: string, updates: Partial<McpServerConfig>): McpServerConfig | undefined {
    const existing = this.mcpServers.get(id);
    if (!existing) return undefined;
    const updated = { ...existing, ...updates, id, updated_at: nowIso() };
    this.mcpServers.set(id, updated);
    return updated;
  }

  deleteMcpServer(id: string): boolean {
    return this.mcpServers.delete(id);
  }

  checkMcpHealth(id: string): Promise<McpServerConfig | undefined> {
    return this.updateMcpServer(id, {
      health_status: "healthy",
      last_health_check: nowIso(),
    }) || undefined;
  }

  // ── CRUD: Skills ──

  getSkills(): SkillConfig[] {
    return Array.from(this.skills.values());
  }

  getSkill(id: string): SkillConfig | undefined {
    return this.skills.get(id);
  }

  createSkill(skill: Omit<SkillConfig, "id" | "created_at" | "updated_at"> & { id?: string }): SkillConfig {
    const id = skill.id || `skill-${Date.now()}`;
    const now = nowIso();
    const config: SkillConfig = { ...skill, id, created_at: now, updated_at: now };
    this.skills.set(id, config);
    return config;
  }

  updateSkill(id: string, updates: Partial<SkillConfig>): SkillConfig | undefined {
    const existing = this.skills.get(id);
    if (!existing) return undefined;
    const updated = { ...existing, ...updates, id, updated_at: nowIso() };
    this.skills.set(id, updated);
    return updated;
  }

  deleteSkill(id: string): boolean {
    return this.skills.delete(id);
  }

  // ── CRUD: AI Providers ──

  getAiProviders(): AiProviderConfig[] {
    return Array.from(this.aiProviders.values());
  }

  getAiProvider(id: string): AiProviderConfig | undefined {
    return this.aiProviders.get(id);
  }

  createAiProvider(provider: Omit<AiProviderConfig, "id" | "created_at" | "updated_at"> & { id?: string }): AiProviderConfig {
    const id = provider.id || `provider-${Date.now()}`;
    const now = nowIso();
    const config: AiProviderConfig = { ...provider, id, created_at: now, updated_at: now };
    this.aiProviders.set(id, config);
    return config;
  }

  updateAiProvider(id: string, updates: Partial<AiProviderConfig>): AiProviderConfig | undefined {
    const existing = this.aiProviders.get(id);
    if (!existing) return undefined;
    const updated = { ...existing, ...updates, id, updated_at: nowIso() };
    this.aiProviders.set(id, updated);
    return updated;
  }

  deleteAiProvider(id: string): boolean {
    return this.aiProviders.delete(id);
  }
}
