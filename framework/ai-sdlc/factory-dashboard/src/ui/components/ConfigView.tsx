/**
 * Factory Dashboard — Configuration view.
 *
 * Manages the full lifecycle of factory configuration: agents, rules,
 * MCP integrations, and skills. Tabbed sub-views with CRUD operations,
 * enable/disable toggles, and association management.
 */
import { useState, useEffect, useCallback } from "react";
import {
  AgentConfig, RuleConfig, McpServerConfig, SkillConfig,
  ConfigCollection, CONFIG_ENDPOINTS,
} from "@/types";

// ───────────────────── Sub-tabs ─────────────────────

type ConfigTab = "agents" | "rules" | "mcp" | "skills";

const TAB_LABELS: Record<ConfigTab, string> = {
  agents: "Agents",
  rules: "Rules",
  mcp: "MCP Integrations",
  skills: "Skills",
};

// ───────────────────── Fetch helpers ─────────────────────

async function fetchCollection<T>(url: string): Promise<ConfigCollection<T>> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to fetch ${url}`);
  return res.json();
}

async function fetchItem<T>(url: string): Promise<T> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to fetch ${url}`);
  return res.json();
}

async function putItem<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(`Failed to update ${url}`);
  return res.json();
}

async function deleteItem(url: string): Promise<void> {
  const res = await fetch(url, { method: "DELETE" });
  if (!res.ok) throw new Error(`Failed to delete ${url}`);
}

async function postItem<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(`Failed to create ${url}`);
  return res.json();
}

// ───────────────────── Enable/Disable Toggle ─────────────────────

function EnableToggle({ enabled, onChange, label }: {
  enabled: boolean;
  onChange: (enabled: boolean) => void;
  label: string;
}) {
  return (
    <button
      className={`toggle-switch ${enabled ? "enabled" : "disabled"}`}
      onClick={() => onChange(!enabled)}
      title={`${label}: ${enabled ? "enabled" : "disabled"}`}
      aria-label={`Toggle ${label} ${enabled ? "off" : "on"}`}
    >
      <span className="toggle-knob" />
    </button>
  );
}

// ───────────────────── Agents View ─────────────────────

function AgentsView() {
  const [agents, setAgents] = useState<AgentConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    try {
      const { items } = await fetchCollection<AgentConfig>(CONFIG_ENDPOINTS.agents);
      setAgents(items);
    } catch (err) {
      console.error("Failed to load agents:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleToggle = async (id: string, enabled: boolean) => {
    try {
      const updated = await putItem<AgentConfig>(CONFIG_ENDPOINTS.agentById(id), { enabled });
      setAgents(prev => prev.map(a => a.id === id ? updated : a));
    } catch (err) {
      console.error("Failed to toggle agent:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this agent?")) return;
    try {
      await deleteItem(CONFIG_ENDPOINTS.agentById(id));
      setAgents(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      console.error("Failed to delete agent:", err);
    }
  };

  const filtered = agents.filter(a =>
    a.name.toLowerCase().includes(search.toLowerCase()) ||
    a.role.toLowerCase().includes(search.toLowerCase()) ||
    a.provider.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="config-loading">Loading agents…</div>;

  return (
    <div className="config-view">
      <div className="config-toolbar">
        <input
          className="config-search"
          type="text"
          placeholder="Search agents…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <span className="config-count">{filtered.length} agent{filtered.length !== 1 ? "s" : ""}</span>
      </div>

      <div className="config-list">
        {filtered.map(agent => (
          <div key={agent.id} className={`config-card ${editing === agent.id ? "editing" : ""}`}>
            <div className="config-card-header">
              <div className="config-card-title">
                <span className="config-card-name">{agent.name}</span>
                <span className="config-provider-badge">{agent.provider}</span>
              </div>
              <div className="config-card-actions">
                <EnableToggle
                  enabled={agent.enabled}
                  onChange={enabled => handleToggle(agent.id, enabled)}
                  label={agent.name}
                />
                <button
                  className="config-btn config-btn-edit"
                  onClick={() => setEditing(editing === agent.id ? null : agent.id)}
                  title="Edit agent"
                >
                  {editing === agent.id ? "Cancel" : "Edit"}
                </button>
                <button
                  className="config-btn config-btn-delete"
                  onClick={() => handleDelete(agent.id)}
                  title="Delete agent"
                >
                  Delete
                </button>
              </div>
            </div>

            <div className="config-card-body">
              <div className="config-field">
                <span className="config-field-label">Role</span>
                <span className="config-field-value">{agent.role}</span>
              </div>
              <div className="config-field">
                <span className="config-field-label">Description</span>
                <span className="config-field-value">{agent.description}</span>
              </div>
              {agent.model && (
                <div className="config-field">
                  <span className="config-field-label">Model</span>
                  <span className="config-field-value">{agent.model}</span>
                </div>
              )}
              {agent.associated_stations?.length > 0 && (
                <div className="config-field">
                  <span className="config-field-label">Stations</span>
                  <div className="config-tags">
                    {agent.associated_stations.map(s => (
                      <span key={s} className="config-tag">{s}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {editing === agent.id && (
              <div className="config-edit-form">
                <div className="config-edit-field">
                  <label>Model</label>
                  <input
                    type="text"
                    defaultValue={agent.model || ""}
                    placeholder="e.g., claude-sonnet-4-20250514"
                  />
                </div>
                <div className="config-edit-field">
                  <label>Max Tokens</label>
                  <input
                    type="number"
                    defaultValue={agent.max_tokens || 4096}
                  />
                </div>
                <div className="config-edit-field">
                  <label>Temperature</label>
                  <input
                    type="number"
                    step="0.1"
                    min="0"
                    max="2"
                    defaultValue={agent.temperature ?? 0.7}
                  />
                </div>
                <div className="config-edit-field">
                  <label>System Prompt</label>
                  <textarea
                    defaultValue={agent.system_prompt || ""}
                    rows={3}
                    placeholder="System prompt for this agent…"
                  />
                </div>
                <button
                  className="config-btn config-btn-save"
                  onClick={async () => {
                    const updated = await putItem<AgentConfig>(CONFIG_ENDPOINTS.agentById(agent.id), {
                      ...agent,
                      model: (document.activeElement as HTMLInputElement)?.value || agent.model,
                    });
                    setAgents(prev => prev.map(a => a.id === agent.id ? updated : a));
                    setEditing(null);
                  }}
                >
                  Save Changes
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

// ───────────────────── Rules View ─────────────────────

function RulesView() {
  const [rules, setRules] = useState<RuleConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<string>("all");

  const load = useCallback(async () => {
    try {
      const { items } = await fetchCollection<RuleConfig>(CONFIG_ENDPOINTS.rules);
      setRules(items);
    } catch (err) {
      console.error("Failed to load rules:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleToggle = async (id: string, enabled: boolean) => {
    try {
      const updated = await putItem<RuleConfig>(CONFIG_ENDPOINTS.ruleById(id), { enabled });
      setRules(prev => prev.map(r => r.id === id ? updated : r));
    } catch (err) {
      console.error("Failed to toggle rule:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this rule?")) return;
    try {
      await deleteItem(CONFIG_ENDPOINTS.ruleById(id));
      setRules(prev => prev.filter(r => r.id !== id));
    } catch (err) {
      console.error("Failed to delete rule:", err);
    }
  };

  const categories = Array.from(new Set(rules.map(r => r.category)));

  const filtered = rules.filter(r => {
    const matchesSearch = r.name.toLowerCase().includes(search.toLowerCase()) ||
      r.description.toLowerCase().includes(search.toLowerCase());
    const matchesCategory = categoryFilter === "all" || r.category === categoryFilter;
    return matchesSearch && matchesCategory;
  });

  if (loading) return <div className="config-loading">Loading rules…</div>;

  return (
    <div className="config-view">
      <div className="config-toolbar">
        <input
          className="config-search"
          type="text"
          placeholder="Search rules…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select
          className="config-filter"
          value={categoryFilter}
          onChange={e => setCategoryFilter(e.target.value)}
        >
          <option value="all">All Categories</option>
          {categories.map(cat => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </select>
        <span className="config-count">{filtered.length} rule{filtered.length !== 1 ? "s" : ""}</span>
      </div>

      <div className="config-list">
        {filtered.map(rule => (
          <div key={rule.id} className="config-card">
            <div className="config-card-header">
              <div className="config-card-title">
                <span className="config-card-name">{rule.name}</span>
                <span className={`config-category-badge config-category-${rule.category}`}>{rule.category}</span>
                <span className={`config-severity-badge config-severity-${rule.severity}`}>{rule.severity}</span>
              </div>
              <div className="config-card-actions">
                <EnableToggle
                  enabled={rule.enabled}
                  onChange={enabled => handleToggle(rule.id, enabled)}
                  label={rule.name}
                />
                <button
                  className="config-btn config-btn-delete"
                  onClick={() => handleDelete(rule.id)}
                  title="Delete rule"
                >
                  Delete
                </button>
              </div>
            </div>

            <div className="config-card-body">
              <div className="config-field">
                <span className="config-field-label">Description</span>
                <span className="config-field-value">{rule.description}</span>
              </div>
              <div className="config-field">
                <span className="config-field-label">File</span>
                <code className="config-file-path">{rule.file_path}</code>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ───────────────────── MCP Integrations View ─────────────────────

function McpView() {
  const [servers, setServers] = useState<McpServerConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    try {
      const { items } = await fetchCollection<McpServerConfig>(CONFIG_ENDPOINTS.mcpServers);
      setServers(items);
    } catch (err) {
      console.error("Failed to load MCP servers:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleToggle = async (id: string, enabled: boolean) => {
    try {
      const updated = await putItem<McpServerConfig>(CONFIG_ENDPOINTS.mcpServerById(id), { enabled });
      setServers(prev => prev.map(s => s.id === id ? updated : s));
    } catch (err) {
      console.error("Failed to toggle MCP server:", err);
    }
  };

  const handleHealthCheck = async (id: string) => {
    try {
      const res = await fetch(CONFIG_ENDPOINTS.mcpHealth(id), { method: "POST" });
      if (res.ok) {
        const updated = await res.json();
        setServers(prev => prev.map(s => s.id === id ? updated : s));
      }
    } catch (err) {
      console.error("Health check failed:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this MCP integration?")) return;
    try {
      await deleteItem(CONFIG_ENDPOINTS.mcpServerById(id));
      setServers(prev => prev.filter(s => s.id !== id));
    } catch (err) {
      console.error("Failed to delete MCP server:", err);
    }
  };

  const filtered = servers.filter(s =>
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.description.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="config-loading">Loading MCP integrations…</div>;

  return (
    <div className="config-view">
      <div className="config-toolbar">
        <input
          className="config-search"
          type="text"
          placeholder="Search MCP integrations…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <span className="config-count">{filtered.length} integration{filtered.length !== 1 ? "s" : ""}</span>
      </div>

      <div className="config-list">
        {filtered.map(server => (
          <div key={server.id} className="config-card">
            <div className="config-card-header">
              <div className="config-card-title">
                <span className="config-card-name">{server.name}</span>
                <span className="config-transport-badge">{server.transport}</span>
                <span className={`config-health-badge config-health-${server.health_status}`}>
                  {server.health_status || "unknown"}
                </span>
              </div>
              <div className="config-card-actions">
                <EnableToggle
                  enabled={server.enabled}
                  onChange={enabled => handleToggle(server.id, enabled)}
                  label={server.name}
                />
                <button
                  className="config-btn config-btn-health"
                  onClick={() => handleHealthCheck(server.id)}
                  title="Check health"
                >
                  Health
                </button>
                <button
                  className="config-btn config-btn-delete"
                  onClick={() => handleDelete(server.id)}
                  title="Delete integration"
                >
                  Delete
                </button>
              </div>
            </div>

            <div className="config-card-body">
              <div className="config-field">
                <span className="config-field-label">Description</span>
                <span className="config-field-value">{server.description}</span>
              </div>
              {server.command && (
                <div className="config-field">
                  <span className="config-field-label">Command</span>
                  <code className="config-file-path">{server.command} {server.args?.join(" ")}</code>
                </div>
              )}
              {server.url && (
                <div className="config-field">
                  <span className="config-field-label">URL</span>
                  <code className="config-file-path">{server.url}</code>
                </div>
              )}
              {server.tools?.length > 0 && (
                <div className="config-field">
                  <span className="config-field-label">Tools</span>
                  <div className="config-tags">
                    {server.tools.map(t => (
                      <span key={t} className="config-tag">{t}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ───────────────────── Skills View ─────────────────────

function SkillsView() {
  const [skills, setSkills] = useState<SkillConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<string>("all");

  const load = useCallback(async () => {
    try {
      const { items } = await fetchCollection<SkillConfig>(CONFIG_ENDPOINTS.skills);
      setSkills(items);
    } catch (err) {
      console.error("Failed to load skills:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleToggle = async (id: string, enabled: boolean) => {
    try {
      const updated = await putItem<SkillConfig>(CONFIG_ENDPOINTS.skillById(id), { enabled });
      setSkills(prev => prev.map(s => s.id === id ? updated : s));
    } catch (err) {
      console.error("Failed to toggle skill:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this skill?")) return;
    try {
      await deleteItem(CONFIG_ENDPOINTS.skillById(id));
      setSkills(prev => prev.filter(s => s.id !== id));
    } catch (err) {
      console.error("Failed to delete skill:", err);
    }
  };

  const categories = Array.from(new Set(skills.map(s => s.category || "General")));

  const filtered = skills.filter(s => {
    const matchesSearch = s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.description.toLowerCase().includes(search.toLowerCase());
    const matchesCategory = categoryFilter === "all" || (s.category || "General") === categoryFilter;
    return matchesSearch && matchesCategory;
  });

  if (loading) return <div className="config-loading">Loading skills…</div>;

  return (
    <div className="config-view">
      <div className="config-toolbar">
        <input
          className="config-search"
          type="text"
          placeholder="Search skills…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select
          className="config-filter"
          value={categoryFilter}
          onChange={e => setCategoryFilter(e.target.value)}
        >
          <option value="all">All Categories</option>
          {categories.map(cat => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </select>
        <span className="config-count">{filtered.length} skill{filtered.length !== 1 ? "s" : ""}</span>
      </div>

      <div className="config-list">
        {filtered.map(skill => (
          <div key={skill.id} className="config-card">
            <div className="config-card-header">
              <div className="config-card-title">
                <span className="config-card-name">{skill.name}</span>
                {skill.is_generic && <span className="config-generic-badge">Generic</span>}
                {skill.category && <span className={`config-category-badge config-category-${skill.category?.toLowerCase()}`}>{skill.category}</span>}
              </div>
              <div className="config-card-actions">
                <EnableToggle
                  enabled={skill.enabled}
                  onChange={enabled => handleToggle(skill.id, enabled)}
                  label={skill.name}
                />
                <button
                  className="config-btn config-btn-delete"
                  onClick={() => handleDelete(skill.id)}
                  title="Delete skill"
                >
                  Delete
                </button>
              </div>
            </div>

            <div className="config-card-body">
              <div className="config-field">
                <span className="config-field-label">Description</span>
                <span className="config-field-value">{skill.description}</span>
              </div>
              <div className="config-field">
                <span className="config-field-label">File</span>
                <code className="config-file-path">{skill.file_path}</code>
              </div>
              {skill.tags?.length > 0 && (
                <div className="config-field">
                  <span className="config-field-label">Tags</span>
                  <div className="config-tags">
                    {skill.tags.map(t => (
                      <span key={t} className="config-tag">{t}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ───────────────────── Main Configuration View ─────────────────────

export function ConfigView() {
  const [activeTab, setActiveTab] = useState<ConfigTab>("agents");

  return (
    <div className="config-container">
      <div className="config-header">
        <h2 className="config-title">Factory Configuration</h2>
        <p className="config-subtitle">Manage agents, rules, integrations, and skills</p>
      </div>

      <div className="config-tabs" role="tablist">
        {(Object.keys(TAB_LABELS) as ConfigTab[]).map(tab => (
          <button
            key={tab}
            role="tab"
            className={`config-tab ${activeTab === tab ? "active" : ""}`}
            onClick={() => setActiveTab(tab)}
            aria-selected={activeTab === tab}
          >
            {TAB_LABELS[tab]}
          </button>
        ))}
      </div>

      <div className="config-content">
        {activeTab === "agents" && <AgentsView />}
        {activeTab === "rules" && <RulesView />}
        {activeTab === "mcp" && <McpView />}
        {activeTab === "skills" && <SkillsView />}
      </div>
    </div>
  );
}
