/**
 * File Viewer — Browse and open factory configuration files.
 *
 * Shows a categorized file tree on the left and a syntax-highlighted
 * preview on the right. Supports Markdown, JSON, YAML, TOML, and plain text.
 */
import { useState, useMemo, useCallback, useEffect } from "react";
import { FileViewerEntry, FileViewerContent, ENDPOINTS, FileViewerType } from "@/types";

// ───────────────────── Syntax highlighting helpers ─────────────────────

function escapeHtml(str: string): string {
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function highlightJson(text: string): string {
  let escaped = escapeHtml(text);
  escaped = escaped.replace(/"(?:[^"\\]|\\.)*"/g, '"$&"');
  escaped = escaped.replace(/\b(true|false|null)\b/g, '<span class="hl-keyword">$1</span>');
  escaped = escaped.replace(/\b(-?\d+\.?\d*)\b/g, '<span class="hl-number">$1</span>');
  return escaped;
}

function highlightYaml(text: string): string {
  let escaped = escapeHtml(text);
  escaped = escaped.replace(/^(\s*[\w-]+)(:)/gm, '<span class="hl-keyword">$1</span>$2');
  escaped = escaped.replace(/(:\s*)(".*?")/gm, '$1<span class="hl-string">$2</span>');
  escaped = escaped.replace(/(:\s*)(true|false|null)/gm, '$1<span class="hl-keyword">$2</span>');
  return escaped;
}

function highlightMarkdown(text: string): string {
  let escaped = escapeHtml(text);
  escaped = escaped.replace(/^(#{1,6}\s+.+)$/gm, '<span class="hl-heading">$1</span>');
  escaped = escaped.replace(/(`[^`]+`)/g, '<span class="hl-code">$1</span>');
  escaped = escaped.replace(/(\*\*[^*]+\*\*)/g, '<span class="hl-bold">$1</span>');
  escaped = escaped.replace(/(\*[^*]+\*)/g, '<span class="hl-italic">$1</span>');
  escaped = escaped.replace(/^(\s*[-*]\s)/gm, '<span class="hl-list">$1</span>');
  return escaped;
}

function highlightTOML(text: string): string {
  let escaped = escapeHtml(text);
  escaped = escaped.replace(/^(\s*[\w-]+)(\s*=)/gm, '<span class="hl-keyword">$1</span>$2');
  escaped = escaped.replace(/"(?:[^"\\]|\\.)*"/g, '<span class="hl-string">$&</span>');
  escaped = escaped.replace(/\[(.+?)\]/g, '<span class="hl-heading">[$1]</span>');
  return escaped;
}

function highlightCode(type: FileViewerType, text: string): string {
  switch (type) {
    case "json": return highlightJson(text);
    case "yaml": return highlightYaml(text);
    case "markdown": return highlightMarkdown(text);
    case "toml": return highlightTOML(text);
    default: return escapeHtml(text);
  }
}

// ───────────────────── Default file list ─────────────────────

const DEFAULT_FILES: FileViewerEntry[] = [
  // Factory config
  { path: ".aisdlc/aisdlc.toml", label: "aisdlc.toml", category: "Factory", type: "toml", description: "AI SDLC main configuration" },
  { path: "repolens.toml", label: "repolens.toml", category: "Factory", type: "toml", description: "RepoLens knowledge graph config" },
  { path: "AGENTS.md", label: "AGENTS.md", category: "Factory", type: "markdown", description: "Agent workflow guidelines" },
  { path: "CONSTITUTION.md", label: "CONSTITUTION.md", category: "Factory", type: "markdown", description: "Project constitution" },
  { path: "CONSTITUTION-AGENT-CARD.md", label: "AGENT-CARD.md", category: "Factory", type: "markdown", description: "Agent constitution digest" },
  { path: "DESIGN.md", label: "DESIGN.md", category: "Factory", type: "markdown", description: "Design principles" },
  { path: "PRODUCT.md", label: "PRODUCT.md", category: "Factory", type: "markdown", description: "Product vision" },

  // Agent configs
  { path: ".claude/agents/cloud-foreman.md", label: "cloud-foreman.md", category: "Agents", type: "markdown", description: "Foreman orchestrator agent" },
  { path: ".claude/agents/security-auditor.md", label: "security-auditor.md", category: "Agents", type: "markdown", description: "Security auditor agent" },
  { path: ".claude/agents/devops-engineer.md", label: "devops-engineer.md", category: "Agents", type: "markdown", description: "DevOps engineer agent" },

  // Rules
  { path: ".claude/rules/hexagonal-architecture.md", label: "hexagonal-architecture.md", category: "Rules", type: "markdown", description: "Hex arch boundary rules" },
  { path: ".claude/rules/programming.md", label: "programming.md", category: "Rules", type: "markdown", description: "Programming standards" },
  { path: ".claude/rules/code-quality.md", label: "code-quality.md", category: "Rules", type: "markdown", description: "Code quality rules" },
  { path: ".claude/rules/general.md", label: "general.md", category: "Rules", type: "markdown", description: "General hygiene" },

  // Skills
  { path: ".claude/skills/hexagonal-arch/SKILL.md", label: "hexagonal-arch/SKILL.md", category: "Skills", type: "markdown", description: "Hexagonal architecture skill" },
  { path: ".claude/skills/backend/SKILL.md", label: "backend/SKILL.md", category: "Skills", type: "markdown", description: "Backend development skill" },
  { path: ".claude/skills/java/SKILL.md", label: "java/SKILL.md", category: "Skills", type: "markdown", description: "Java skill" },

  // Framework
  { path: "framework/ai-sdlc/MODULE.md", label: "MODULE.md", category: "Framework", type: "markdown", description: "AI SDLC module reference" },
  { path: "framework/ai-sdlc/README.md", label: "README.md", category: "Framework", type: "markdown", description: "AI SDLC framework readme" },
  { path: "framework/ai-sdlc/factory-dashboard/package.json", label: "package.json", category: "Framework", type: "json", description: "Dashboard package config" },

  // Benchmarks
  { path: "framework/ai-sdlc/repolens/bench/tasks/notaire.yaml", label: "notaire.yaml", category: "Benchmarks", type: "yaml", description: "Benchmark task corpus" },
  { path: "framework/ai-sdlc/repolens/bench/results/factory-kpis.json", label: "factory-kpis.json", category: "Benchmarks", type: "json", description: "Factory KPIs" },

  // Docs
  { path: "docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md", label: "ADR-030.md", category: "Docs", type: "markdown", description: "Hex arch adoption ADR" },
  { path: "docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md", label: "FACTORY-UI-SPEC.md", category: "Docs", type: "markdown", description: "Factory UI specification" },
];

// ───────────────────── File tree ─────────────────────

function FileTree({
  files,
  selected,
  onSelect,
}: {
  files: FileViewerEntry[];
  selected: string | null;
  onSelect: (entry: FileViewerEntry) => void;
}) {
  const [search, setSearch] = useState("");
  const [expandedCategories, setExpandedCategories] = useState<Set<string>>(
    new Set(files.map((f) => f.category))
  );

  const filtered = useMemo(() => {
    if (!search) return files;
    const q = search.toLowerCase();
    return files.filter((f) => f.label.toLowerCase().includes(q) || f.category.toLowerCase().includes(q));
  }, [files, search]);

  const grouped = useMemo(() => {
    const map = new Map<string, FileViewerEntry[]>();
    for (const f of filtered) {
      const list = map.get(f.category) || [];
      list.push(f);
      map.set(f.category, list);
    }
    return map;
  }, [filtered]);

  const toggleCategory = (cat: string) => {
    setExpandedCategories((prev) => {
      const next = new Set(prev);
      if (next.has(cat)) next.delete(cat);
      else next.add(cat);
      return next;
    });
  };

  return (
    <div className="file-tree">
      <div className="file-tree-search">
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
          <circle cx="6" cy="6" r="4" />
          <path d="M13 13l-3-3" strokeLinecap="round" />
        </svg>
        <input
          type="text"
          placeholder="Search files…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>
      <div className="file-tree-list">
        {Array.from(grouped.entries()).map(([category, entries]) => (
          <div key={category} className="file-tree-group">
            <button
              className="file-tree-group-header"
              onClick={() => toggleCategory(category)}
            >
              <span className={`file-tree-chevron ${expandedCategories.has(category) ? "expanded" : ""}`}>
                ▸
              </span>
              {category}
              <span className="file-tree-count">{entries.length}</span>
            </button>
            {expandedCategories.has(category) && (
              <div className="file-tree-group-items">
                {entries.map((entry) => (
                  <button
                    key={entry.path}
                    className={`file-tree-item ${selected === entry.path ? "selected" : ""}`}
                    onClick={() => onSelect(entry)}
                  >
                    <span className={`file-tree-icon ${entry.type}`}>
                      {entry.type === "markdown" ? "M" : entry.type === "json" ? "J" : entry.type === "yaml" ? "Y" : entry.type === "toml" ? "T" : "T"}
                    </span>
                    <span className="file-tree-label">{entry.label}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

// ───────────────────── File preview ─────────────────────

function FilePreview({
  entry,
  onOpenExternal,
}: {
  entry: FileViewerEntry | null;
  onOpenExternal: (path: string) => void;
}) {
  const [content, setContent] = useState<FileViewerContent | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!entry) {
      setContent(null);
      return;
    }
    setLoading(true);
    setError(null);

    // Try to fetch from API first
    fetch(ENDPOINTS.fileViewerContent(entry.path))
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data: FileViewerContent) => {
        setContent(data);
        setLoading(false);
      })
      .catch(() => {
        // Fallback: show placeholder for demo mode
        setContent({
          path: entry.path,
          content: `[File content loaded from: ${entry.path}]\n\nThis file is available in the repository. In production mode, the full content would be displayed here with syntax highlighting.`,
          type: entry.type,
        });
        setLoading(false);
      });
  }, [entry]);

  if (!entry) {
    return (
      <div className="file-preview-empty">
        <svg width="48" height="48" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1">
          <path d="M6 2H3a1 1 0 00-1 1v10a1 1 0 001 1h10a1 1 0 001-1V7l-5-5z" />
          <path d="M11 2v5h4" />
          <path d="M8 11V8M6 8h4" strokeLinecap="round" />
        </svg>
        <p>Select a file to preview</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="file-preview-loading">
        <span className="loading-spinner" />
        Loading…
      </div>
    );
  }

  if (error) {
    return (
      <div className="file-preview-error">
        <p>Failed to load file</p>
        <p className="error-detail">{error}</p>
      </div>
    );
  }

  const highlighted = content ? highlightCode(content.type, content.content) : "";
  const lineCount = content ? content.content.split("\n").length : 0;

  return (
    <div className="file-preview">
      <div className="file-preview-header">
        <div className="file-preview-title">
          <span className={`file-preview-type-badge ${content?.type || entry.type}`}>
            {content?.type || entry.type}
          </span>
          <span className="file-preview-path">{entry.path}</span>
        </div>
        <div className="file-preview-meta">
          <span>{lineCount} lines</span>
          {content?.last_modified && <span>{content.last_modified}</span>}
          <button className="file-preview-open-btn" onClick={() => onOpenExternal(entry.path)}>
            <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M11 2H9a1 1 0 00-1 1v3a1 1 0 01-1 1H4a1 1 0 00-1 1v6a1 1 0 001 1h8a1 1 0 001-1v-4a1 1 0 011-1h3V6l-4-4z" />
              <path d="M11 2l4 4M11 2v4h4" strokeLinecap="round" />
            </svg>
            Open in editor
          </button>
        </div>
      </div>
      <div className="file-preview-body">
        <pre className="file-preview-code"><code dangerouslySetInnerHTML={{ __html: highlighted }} /></pre>
      </div>
    </div>
  );
}

// ───────────────────── Main view ─────────────────────

export function FileViewerView() {
  const [files] = useState<FileViewerEntry[]>(DEFAULT_FILES);
  const [selectedPath, setSelectedPath] = useState<string | null>(null);

  const selectedEntry = useMemo(
    () => files.find((f) => f.path === selectedPath) || null,
    [files, selectedPath]
  );

  const handleSelect = useCallback((entry: FileViewerEntry) => {
    setSelectedPath(entry.path);
  }, []);

  const handleOpenExternal = useCallback((path: string) => {
    // Open in default editor or browser
    const repoUrl = "https://github.com/matiaspakua/notaire";
    window.open(`${repoUrl}/blob/main/${path}`, "_blank");
  }, []);

  return (
    <div className="file-viewer-container">
      <FileTree files={files} selected={selectedPath} onSelect={handleSelect} />
      <FilePreview entry={selectedEntry} onOpenExternal={handleOpenExternal} />
    </div>
  );
}
