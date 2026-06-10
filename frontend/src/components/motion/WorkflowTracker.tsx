"use client";

import { motion } from "framer-motion";
import type { GestionWorkflowTrace, WorkflowNode, WorkflowTransition } from "@/types";
import { useMemo } from "react";

interface Props {
  trace: GestionWorkflowTrace;
}

const NODE_W = 160;
const NODE_H = 56;
const X_GAP = 80;
const Y_GAP = 100;
const PAD = 40;

const STATUS = {
  completed: { bg: "#10b981", border: "#059669", text: "#ffffff" },
  in_progress: { bg: "#3b82f6", border: "#2563eb", text: "#ffffff" },
  pending: { bg: "#f3f4f6", border: "#d1d5db", text: "#9ca3af" },
} as const;

/**
 * Renders the workflow trace as an animated SVG map.
 *
 * - Nodes are positioned via posicionX/posicionY when available,
 *   otherwise auto-laid-out top‑to‑bottom by layer.
 * - Transitions are drawn as arrows between node edges.
 * - Node fill color reflects computed status (completed / in_progress / pending).
 * - Nodes in "in_progress" pulse with a glow ring.
 */
export default function WorkflowTracker({ trace }: Props) {
  const { nodes, transitions, nodeStatuses, workflowDefinition } = trace;

  // ── Layout ───────────────────────────────────────────────────────────
  const layout = useMemo(() => {
    type LayedOut = WorkflowNode & { lx: number; ly: number };

    const hasPos = nodes.some((n) => n.posicionX != null && n.posicionY != null);
    if (hasPos) {
      const mx = Math.min(...nodes.map((n) => n.posicionX ?? 0));
      const my = Math.min(...nodes.map((n) => n.posicionY ?? 0));
      return nodes.map(
        (n): LayedOut => ({
          ...n,
          lx: (n.posicionX ?? 0) - mx + PAD,
          ly: (n.posicionY ?? 0) - my + PAD,
        }),
      );
    }

    // ── Auto-layout (BFS layered) ──
    const children = new Map<number, number[]>();
    for (const t of transitions) {
      const src = t.nodoOrigenId ?? -1;
      const dst = t.nodoDestinoId ?? -1;
      if (src >= 0 && dst >= 0) {
        children.set(src, [...(children.get(src) ?? []), dst]);
      }
    }

    const layerOf = new Map<number, number>();
    const q: Array<{ id: number; l: number }> = [];
    const initial = nodes.find((n) => n.tipo === "INITIAL");
    if (initial?.id != null) q.push({ id: initial.id, l: 0 });

    while (q.length) {
      const { id, l } = q.shift()!;
      if (layerOf.has(id)) continue;
      layerOf.set(id, l);
      for (const childId of children.get(id) ?? []) {
        if (!layerOf.has(childId)) q.push({ id: childId, l: l + 1 });
      }
    }
    // Isolated nodes
    for (const n of nodes) {
      if (n.id != null && !layerOf.has(n.id)) layerOf.set(n.id, 0);
    }

    // Count per layer
    const cnt = new Map<number, number>();
    const idx = new Map<number, number>();
    for (const [, l] of layerOf) cnt.set(l, (cnt.get(l) ?? 0) + 1);

    return nodes.map((n): LayedOut => {
      const l = n.id != null ? layerOf.get(n.id) ?? 0 : 0;
      const total = cnt.get(l) ?? 1;
      const i = idx.get(l) ?? 0;
      idx.set(l, i + 1);
      const rowW = total * (NODE_W + X_GAP) - X_GAP;
      return {
        ...n,
        lx: PAD + (rowW / 2 - NODE_W / 2) + i * (NODE_W + X_GAP),
        ly: PAD + l * Y_GAP,
      };
    });
  }, [nodes, transitions]);

  // ── SVG bounds ──
  const svgW = Math.max(
    ...layout.map((n) => n.lx + NODE_W),
    PAD + 80,
  );
  const svgH = Math.max(
    ...layout.map((n) => n.ly + NODE_H),
    PAD + 80,
  );

  // ── Node positions map ──
  const posMap = useMemo(
    () => new Map(layout.map((n) => [n.id!, { x: n.lx, y: n.ly }])),
    [layout],
  );

  // ── Arrows ──
  const arrows = useMemo(
    () =>
      transitions
        .map((t) => {
          const f = t.nodoOrigenId != null ? posMap.get(t.nodoOrigenId) : undefined;
          const tgt = t.nodoDestinoId != null ? posMap.get(t.nodoDestinoId) : undefined;
          if (!f || !tgt) return null;

          const cx1 = f.x + NODE_W / 2;
          const cy1 = f.y + NODE_H / 2;
          const cx2 = tgt.x + NODE_W / 2;
          const cy2 = tgt.y + NODE_H / 2;
          const dx = cx2 - cx1;
          const dy = cy2 - cy1;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < 1) return null;

          // Normalised direction
          const nx = dx / dist;
          const ny = dy / dist;

          return {
            id: t.id ?? -1,
            x1: cx1 + nx * (NODE_H / 2),
            y1: cy1 + ny * (NODE_H / 2),
            x2: cx2 - nx * (NODE_H / 2),
            y2: cy2 - ny * (NODE_H / 2),
          };
        })
        .filter(Boolean),
    [transitions, posMap],
  );

  // ── Render ──
  return (
    <svg
      viewBox={`0 0 ${svgW + PAD} ${svgH + PAD}`}
      className="w-full h-auto max-h-[600px] bg-white rounded-xl shadow-sm"
    >
      <defs>
        <marker id="aw" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
          <polygon points="0 0,10 3.5,0 7" fill="#6b7280" />
        </marker>
        <filter id="glow">
          <feGaussianBlur stdDeviation="3" result="b" />
          <feMerge>
            <feMergeNode in="b" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      {/* Title */}
      {workflowDefinition?.nombre && (
        <text
          x={PAD}
          y={16}
          fontSize={13}
          fontWeight={600}
          fill="#374151"
          fontFamily="system-ui, sans-serif"
        >
          {workflowDefinition.nombre}
        </text>
      )}

      {/* Arrows */}
      {arrows.map(
        (a) =>
          a && (
            <line
              key={a.id}
              x1={a.x1}
              y1={a.y1}
              x2={a.x2}
              y2={a.y2}
              stroke="#9ca3af"
              strokeWidth={2}
              markerEnd="url(#aw)"
            />
          ),
      )}

      {/* Nodes */}
      {layout.map((node) => {
        const status =
          node.id != null ? (nodeStatuses[node.id] as keyof typeof STATUS) : "pending";
        const c = STATUS[status] ?? STATUS.pending;
        const ip = status === "in_progress";
        return (
          <g key={node.id}>
            {/* Pulse glow for in_progress */}
            {ip && (
              <motion.ellipse
                cx={node.lx + NODE_W / 2}
                cy={node.ly + NODE_H / 2}
                rx={NODE_W / 2 + 10}
                ry={NODE_H / 2 + 10}
                fill="none"
                stroke={c.border}
                strokeWidth={3}
                initial={{ opacity: 0.5, scale: 1 }}
                animate={{ opacity: 0, scale: 1.15 }}
                transition={{ duration: 1.5, repeat: Infinity, ease: "easeOut" }}
              />
            )}
            <rect
              x={node.lx}
              y={node.ly}
              width={NODE_W}
              height={NODE_H}
              rx={8}
              fill={c.bg}
              stroke={c.border}
              strokeWidth={ip ? 2 : 1}
              filter={ip ? "url(#glow)" : undefined}
            />
            {/* Dot */}
            <circle
              cx={node.lx + 14}
              cy={node.ly + NODE_H / 2}
              r={5}
              fill={status === "completed" ? "#ffffff" : "currentColor"}
              opacity={0.7}
            />
            <text
              x={node.lx + NODE_W / 2}
              y={node.ly + NODE_H / 2}
              textAnchor="middle"
              dominantBaseline="middle"
              fill={c.text}
              fontSize={13}
              fontWeight={600}
              fontFamily="system-ui, sans-serif"
            >
              {node.estadoGestionNombre ?? `Nodo ${node.id}`}
            </text>
          </g>
        );
      })}
    </svg>
  );
}
