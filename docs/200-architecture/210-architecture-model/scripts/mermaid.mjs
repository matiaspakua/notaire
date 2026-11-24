// Converts a LikeC4 computed view to Mermaid (#2483, ADR-033). Pure: no LikeC4 import, so it is unit-tested
// with fixtures. `likec4 gen mermaid` exists, but it drops step numbers, the sequence variant, merged-edge
// labels and the finding styles that make these diagrams worth reading.

const CLASS_DEFS = [
  "classDef c_primary fill:#dbeafe,stroke:#1d4ed8,color:#0f172a",
  "classDef c_secondary fill:#ede9fe,stroke:#6d28d9,color:#0f172a",
  "classDef c_sky fill:#e0f2fe,stroke:#0369a1,color:#0f172a",
  "classDef c_indigo fill:#e0e7ff,stroke:#4338ca,color:#0f172a",
  "classDef c_green fill:#dcfce7,stroke:#15803d,color:#0f172a",
  "classDef c_amber fill:#fef3c7,stroke:#b45309,color:#0f172a",
  "classDef c_red fill:#fee2e2,stroke:#b91c1c,color:#0f172a",
  "classDef c_slate fill:#e2e8f0,stroke:#334155,color:#0f172a",
  "classDef c_gray fill:#f1f5f9,stroke:#475569,color:#0f172a",
  "classDef c_muted fill:#f8fafc,stroke:#64748b,color:#334155",
  "classDef finding fill:#fee2e2,stroke:#b91c1c,stroke-width:2px,stroke-dasharray:6 3,color:#0f172a",
  "classDef debt fill:#fef3c7,stroke:#b45309,stroke-width:2px,stroke-dasharray:6 3,color:#0f172a",
  "classDef planned fill:#f8fafc,stroke:#64748b,stroke-dasharray:2 3,color:#334155",
];

const FINDING_LINK = "stroke:#b91c1c,stroke-width:2px,color:#b91c1c";

export function mermaidId(fqn) {
  return fqn.replace(/[^A-Za-z0-9]/g, "_");
}

function clean(text) {
  return String(text ?? "")
    .replace(/\s+/g, " ")
    .replace(/;/g, ",")
    .replace(/"/g, "'")
    .replace(/#/g, "No.")
    .trim();
}

function nodeLabel(node) {
  const title = clean(node.title);
  return node.technology ? `${title}<br/><i>${clean(node.technology)}</i>` : title;
}

function shape(node) {
  const label = `"${nodeLabel(node)}"`;
  switch (node.shape) {
    case "person":
      return `([${label}])`;
    case "storage":
    case "cylinder":
      return `[(${label})]`;
    case "queue":
      return `[[${label}]]`;
    default:
      return `[${label}]`;
  }
}

function nodeClass(node) {
  if (node.tags?.includes("finding")) return "finding";
  if (node.tags?.includes("debt")) return "debt";
  if (node.tags?.includes("planned")) return "planned";
  return `c_${node.color ?? "primary"}`;
}

function edgeLabel(edge, relationTitle) {
  const merged = !edge.label || edge.label === "[...]";
  if (!merged) return clean(edge.label);
  const titles = (edge.relations ?? []).map((id) => relationTitle?.(id)).filter(Boolean);
  if (titles.length === 0) return "";
  return titles.length === 1 ? clean(titles[0]) : `${clean(titles[0])} (+${titles.length - 1})`;
}

function arrow(edge, label) {
  const from = mermaidId(edge.source);
  const to = mermaidId(edge.target);
  // LikeC4 draws every relation dashed by default, so only an explicit `dotted` style draws dotted.
  const dotted = edge.line === "dotted";
  if (!label) return dotted ? `${from} -.-> ${to}` : `${from} --> ${to}`;
  return dotted ? `${from} -. "${label}" .-> ${to}` : `${from} -- "${label}" --> ${to}`;
}

function frontMatter(view) {
  return `---\ntitle: "${clean(view.title ?? view.id)}"\n---\n`;
}

function renderNodes(view) {
  const byId = new Map(view.nodes.map((n) => [n.id, n]));
  const lines = [];
  const walk = (node, depth) => {
    const pad = "  ".repeat(depth);
    const children = (node.children ?? []).map((id) => byId.get(id)).filter(Boolean);
    if (children.length === 0) {
      lines.push(`${pad}${mermaidId(node.id)}${shape(node)}`);
      return;
    }
    lines.push(`${pad}subgraph ${mermaidId(node.id)}["${nodeLabel(node)}"]`);
    children.forEach((child) => walk(child, depth + 1));
    lines.push(`${pad}end`);
  };
  view.nodes.filter((n) => !n.parent || !byId.has(n.parent)).forEach((n) => walk(n, 1));
  return lines;
}

function classLines(view) {
  const leaves = view.nodes.filter((n) => (n.children ?? []).length === 0);
  const groups = new Map();
  for (const node of leaves) {
    const cls = nodeClass(node);
    groups.set(cls, [...(groups.get(cls) ?? []), mermaidId(node.id)]);
  }
  const used = new Set(groups.keys());
  const defs = CLASS_DEFS.filter((d) => used.has(d.split(" ")[1])).map((d) => `  ${d}`);
  const uses = [...groups.entries()].map(([cls, ids]) => `  class ${ids.join(",")} ${cls}`);
  return [...defs, ...uses];
}

function flowchart(view, options, direction, numbered) {
  const lines = [`flowchart ${direction}`, ...renderNodes(view)];
  const findings = [];
  view.edges.forEach((edge, index) => {
    let label = edgeLabel(edge, options.relationTitle);
    if (numbered) label = `${index + 1}. ${label}`;
    lines.push(`  ${arrow(edge, label)}`);
    if (edge.tags?.includes("finding")) findings.push(index);
  });
  lines.push(...classLines(view));
  findings.forEach((index) => lines.push(`  linkStyle ${index} ${FINDING_LINK}`));
  return frontMatter(view) + lines.join("\n") + "\n";
}

function sequence(view) {
  const lines = ["sequenceDiagram", "  autonumber"];
  const byId = new Map(view.nodes.map((n) => [n.id, n]));
  const order = [];
  for (const edge of view.edges) {
    const [a, b] = edge.dir === "back" ? [edge.target, edge.source] : [edge.source, edge.target];
    for (const id of [a, b]) if (!order.includes(id)) order.push(id);
  }
  for (const id of order) {
    const node = byId.get(id);
    const keyword = node?.shape === "person" ? "actor" : "participant";
    lines.push(`  ${keyword} ${mermaidId(id)} as ${clean(node?.title ?? id)}`);
  }
  for (const edge of view.edges) {
    const back = edge.dir === "back";
    lines.push(`  ${mermaidId(edge.source)}${back ? "-->>" : "->>"}${mermaidId(edge.target)}: ${clean(edge.label)}`);
  }
  return frontMatter(view) + lines.join("\n") + "\n";
}

/**
 * @param view a LikeC4 computed view (`$view` of the computed model)
 * @param options.relationTitle resolves a relation id to its title, for edges that merge several relations
 */
export function toMermaid(view, options = {}) {
  if (view._type === "dynamic") {
    return view.variant === "sequence" ? sequence(view) : flowchart(view, options, "LR", true);
  }
  return flowchart(view, options, "TB", false);
}
