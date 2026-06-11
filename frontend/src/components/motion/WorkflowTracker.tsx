"use client";

import { motion, AnimatePresence, useReducedMotion } from "motion/react";
import { useTranslations } from "next-intl";
import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";
import { theme } from "@/theme/tokens";
import { easeApple } from "@/components/motion";
import type {
  GestionWorkflowTrace,
  HistorialEntry,
  WorkflowNode,
  WorkflowTransition,
} from "@/types";

interface Props {
  trace: GestionWorkflowTrace;
}

type NodeStatus = "completed" | "in_progress" | "pending";

interface LaidOutNode extends WorkflowNode {
  lx: number;
  ly: number;
  layer: number;
}

interface Edge {
  id: number;
  from: number;
  to: number;
  path: string;
  active: boolean;
}

const NODE_W = 190;
const NODE_H = 64;
const X_GAP = 64;
const Y_GAP = 130;
const PAD = 32;

const STATUS_STYLE: Record<NodeStatus, { fill: string; border: string; text: string; badge: string }> = {
  completed: {
    fill: theme.colors.neutral[0],
    border: theme.colors.success[500],
    text: theme.colors.neutral[900],
    badge: theme.colors.success[500],
  },
  in_progress: {
    fill: theme.colors.primary[50],
    border: theme.colors.primary[600],
    text: theme.colors.primary[900],
    badge: theme.colors.primary[600],
  },
  pending: {
    fill: theme.colors.neutral[100],
    border: theme.colors.neutral[400],
    text: theme.colors.neutral[600],
    badge: theme.colors.neutral[500],
  },
};

function nodeStatus(node: WorkflowNode, statuses: Record<number, string>): NodeStatus {
  const raw = node.id != null ? statuses[node.id] : undefined;
  return raw === "completed" || raw === "in_progress" ? raw : "pending";
}

/**
 * Longest-path layering: each node sits one layer below its deepest
 * predecessor, so forks that re-converge always land below every branch.
 */
function computeLayers(nodes: WorkflowNode[], transitions: WorkflowTransition[]): Map<number, number> {
  const layers = new Map<number, number>();
  for (const n of nodes) {
    if (n.id != null) {
      layers.set(n.id, n.tipo === "INITIAL" ? 0 : 0);
    }
  }
  for (let i = 0; i < nodes.length; i++) {
    let changed = false;
    for (const t of transitions) {
      if (t.nodoOrigenId == null || t.nodoDestinoId == null) {
        continue;
      }
      const candidate = (layers.get(t.nodoOrigenId) ?? 0) + 1;
      if (candidate > (layers.get(t.nodoDestinoId) ?? 0)) {
        layers.set(t.nodoDestinoId, candidate);
        changed = true;
      }
    }
    if (!changed) {
      break;
    }
  }
  return layers;
}

function layoutNodes(nodes: WorkflowNode[], transitions: WorkflowTransition[]): LaidOutNode[] {
  const layers = computeLayers(nodes, transitions);
  const byLayer = new Map<number, WorkflowNode[]>();
  for (const n of nodes) {
    const layer = n.id != null ? layers.get(n.id) ?? 0 : 0;
    byLayer.set(layer, [...(byLayer.get(layer) ?? []), n]);
  }
  const maxRowW = Math.max(
    ...Array.from(byLayer.values()).map((row) => row.length * (NODE_W + X_GAP) - X_GAP),
  );
  const result: LaidOutNode[] = [];
  for (const [layer, row] of byLayer) {
    const rowW = row.length * (NODE_W + X_GAP) - X_GAP;
    row.forEach((n, i) => {
      result.push({
        ...n,
        layer,
        lx: PAD + (maxRowW - rowW) / 2 + i * (NODE_W + X_GAP),
        ly: PAD + layer * Y_GAP,
      });
    });
  }
  return result;
}

function buildEdges(
  transitions: WorkflowTransition[],
  positions: Map<number, LaidOutNode>,
  statuses: Record<number, string>,
): Edge[] {
  const edges: Edge[] = [];
  for (const t of transitions) {
    if (t.nodoOrigenId == null || t.nodoDestinoId == null) {
      continue;
    }
    const from = positions.get(t.nodoOrigenId);
    const to = positions.get(t.nodoDestinoId);
    if (!from || !to) {
      continue;
    }
    const x1 = from.lx + NODE_W / 2;
    const y1 = from.ly + NODE_H;
    const x2 = to.lx + NODE_W / 2;
    const y2 = to.ly;
    const bend = Math.max((y2 - y1) / 2, 24);
    const fromStatus = statuses[t.nodoOrigenId];
    edges.push({
      id: t.id ?? edges.length,
      from: t.nodoOrigenId,
      to: t.nodoDestinoId,
      path: `M ${x1} ${y1} C ${x1} ${y1 + bend}, ${x2} ${y2 - bend}, ${x2} ${y2}`,
      active: fromStatus === "completed" || fromStatus === "in_progress",
    });
  }
  return edges;
}

function StatusBadge({ status }: { status: NodeStatus }) {
  const tw = useTranslations("dashboard.workflow");
  const style = STATUS_STYLE[status];
  return (
    <span
      data-testid="workflow-modal-status"
      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold text-white"
      style={{ backgroundColor: style.badge }}
    >
      {tw(`status.${status}`)}
    </span>
  );
}

interface NodeModalProps {
  node: LaidOutNode;
  status: NodeStatus;
  trace: GestionWorkflowTrace;
  onClose: () => void;
}

function NodeModal({ node, status, trace, onClose }: NodeModalProps) {
  const tw = useTranslations("dashboard.workflow");
  const nodeName = (id: number | undefined) =>
    trace.nodes.find((n) => n.id === id)?.estadoGestionNombre ?? `#${id}`;
  const history: HistorialEntry[] = trace.historial.filter(
    (h) => h.estadoGestionId === node.estadoGestionId,
  );
  const incoming = trace.transitions.filter((t) => t.nodoDestinoId === node.id);
  const outgoing = trace.transitions.filter((t) => t.nodoOrigenId === node.id);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <motion.div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ backgroundColor: "rgba(0, 0, 0, 0.4)" }}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.2, ease: easeApple }}
      onClick={onClose}
      data-testid="workflow-node-modal"
    >
      <motion.div
        role="dialog"
        aria-modal="true"
        aria-label={node.estadoGestionNombre}
        className="bg-white rounded-[28px] shadow-2xl w-full max-w-md p-8 max-h-[80vh] overflow-y-auto"
        initial={{ scale: 0.92, opacity: 0, y: 12 }}
        animate={{ scale: 1, opacity: 1, y: 0 }}
        exit={{ scale: 0.95, opacity: 0 }}
        transition={{ type: "spring", stiffness: 260, damping: 24 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-4">
          <h3 className="text-xl font-semibold tracking-tight" style={{ color: theme.colors.neutral[900] }}>
            {node.estadoGestionNombre ?? `Nodo ${node.id}`}
          </h3>
          <button
            type="button"
            aria-label={tw("modal.close")}
            onClick={onClose}
            className="p-2 rounded-full hover:bg-neutral-100 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-300"
          >
            <X className="h-5 w-5" style={{ color: theme.colors.neutral[600] }} />
          </button>
        </div>

        <div className="mt-4 flex items-center gap-3">
          <StatusBadge status={status} />
          {node.tipo && (
            <span
              className="text-xs font-semibold uppercase tracking-wider"
              style={{ color: theme.colors.neutral[600] }}
            >
              {tw(`type.${node.tipo}`)}
            </span>
          )}
        </div>

        <div className="mt-6 space-y-5 text-sm">
          {incoming.length > 0 && (
            <div>
              <p className="font-semibold mb-1.5" style={{ color: theme.colors.neutral[900] }}>
                {tw("modal.incoming")}
              </p>
              <ul className="space-y-1" style={{ color: theme.colors.neutral[600] }}>
                {incoming.map((t) => (
                  <li key={t.id}>← {nodeName(t.nodoOrigenId)}{t.condicion ? ` · ${t.condicion}` : ""}</li>
                ))}
              </ul>
            </div>
          )}
          {outgoing.length > 0 && (
            <div>
              <p className="font-semibold mb-1.5" style={{ color: theme.colors.neutral[900] }}>
                {tw("modal.outgoing")}
              </p>
              <ul className="space-y-1" style={{ color: theme.colors.neutral[600] }}>
                {outgoing.map((t) => (
                  <li key={t.id}>→ {nodeName(t.nodoDestinoId)}{t.condicion ? ` · ${t.condicion}` : ""}</li>
                ))}
              </ul>
            </div>
          )}
          <div>
            <p className="font-semibold mb-1.5" style={{ color: theme.colors.neutral[900] }}>
              {tw("modal.history")}
            </p>
            {history.length === 0 ? (
              <p style={{ color: theme.colors.neutral[500] }}>{tw("modal.noHistory")}</p>
            ) : (
              <ul className="space-y-2">
                {history.map((h) => (
                  <li
                    key={h.idHistorial}
                    className="rounded-xl px-4 py-2.5"
                    style={{ backgroundColor: theme.colors.neutral[100] }}
                  >
                    <p className="font-medium" style={{ color: theme.colors.neutral[900] }}>
                      {h.fecha ? new Date(h.fecha).toLocaleDateString() : "—"}
                    </p>
                    {h.observaciones && (
                      <p className="mt-0.5" style={{ color: theme.colors.neutral[600] }}>
                        {h.observaciones}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
}

/**
 * Animated, interactive map of a gestión's workflow trace.
 *
 * Top-to-bottom DAG: forks fan out and re-converge; each node card reflects
 * its computed status (completed / in progress / pending), the in-progress
 * step pulses, and traversed edges carry traveling "data flow" dots. Nodes
 * are keyboard-focusable and open a detail modal. All motion respects
 * `prefers-reduced-motion`.
 */
export default function WorkflowTracker({ trace }: Props) {
  const tw = useTranslations("dashboard.workflow");
  const reduceMotion = useReducedMotion();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [focusedId, setFocusedId] = useState<number | null>(null);

  const layout = useMemo(
    () => layoutNodes(trace.nodes, trace.transitions),
    [trace.nodes, trace.transitions],
  );
  const positions = useMemo(
    () => new Map(layout.filter((n) => n.id != null).map((n) => [n.id as number, n])),
    [layout],
  );
  const edges = useMemo(
    () => buildEdges(trace.transitions, positions, trace.nodeStatuses),
    [trace.transitions, positions, trace.nodeStatuses],
  );

  if (layout.length === 0) {
    return (
      <p className="text-sm py-8 text-center" style={{ color: theme.colors.neutral[500] }}>
        {tw("empty")}
      </p>
    );
  }

  const svgW = Math.max(...layout.map((n) => n.lx + NODE_W)) + PAD;
  const svgH = Math.max(...layout.map((n) => n.ly + NODE_H)) + PAD;
  const selected = selectedId != null ? positions.get(selectedId) : undefined;

  return (
    <div data-testid="workflow-tracker">
      <div
        className="rounded-[28px] overflow-x-auto"
        style={{
          backgroundColor: theme.colors.neutral[50],
          border: `1px solid ${theme.colors.neutral[200]}`,
        }}
      >
        <svg
          viewBox={`0 0 ${svgW} ${svgH}`}
          className="w-full h-auto max-h-[640px] mx-auto"
          style={{ minWidth: Math.min(svgW, 720) }}
          role="img"
          aria-label={trace.workflowDefinition?.nombre}
        >
          <defs>
            <marker id="wf-arrow-active" markerWidth="10" markerHeight="8" refX="8" refY="4" orient="auto">
              <polygon points="0 0,10 4,0 8" fill={theme.colors.primary[500]} />
            </marker>
            <marker id="wf-arrow" markerWidth="10" markerHeight="8" refX="8" refY="4" orient="auto">
              <polygon points="0 0,10 4,0 8" fill={theme.colors.neutral[400]} />
            </marker>
          </defs>

          {edges.map((edge) => (
            <g key={edge.id}>
              <motion.path
                d={edge.path}
                fill="none"
                stroke={edge.active ? theme.colors.primary[500] : theme.colors.neutral[400]}
                strokeWidth={edge.active ? 2.5 : 1.5}
                strokeDasharray={edge.active ? undefined : "6 5"}
                markerEnd={edge.active ? "url(#wf-arrow-active)" : "url(#wf-arrow)"}
                initial={reduceMotion ? false : { pathLength: 0 }}
                animate={{ pathLength: 1 }}
                transition={{ duration: 0.6, ease: easeApple, delay: 0.2 }}
              />
              {edge.active && !reduceMotion && (
                <circle r={4} fill={theme.colors.primary[500]} fillOpacity={0.9}>
                  <animateMotion dur="2.4s" repeatCount="indefinite" path={edge.path} />
                </circle>
              )}
            </g>
          ))}

          {layout.map((node) => {
            const status = nodeStatus(node, trace.nodeStatuses);
            const style = STATUS_STYLE[status];
            const inProgress = status === "in_progress";
            const isFinal = node.tipo === "FINAL";
            const radius = node.tipo === "INITIAL" ? NODE_H / 2 : 14;
            const cx = node.lx + NODE_W / 2;
            const cy = node.ly + NODE_H / 2;
            return (
              <motion.g
                key={node.id}
                data-testid={`workflow-node-${node.id}`}
                role="button"
                tabIndex={0}
                aria-label={`${node.estadoGestionNombre ?? node.id} — ${tw(`status.${status}`)}`}
                style={{ cursor: "pointer", outline: "none" }}
                initial={reduceMotion ? false : { opacity: 0, y: -14 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, ease: easeApple, delay: node.layer * 0.1 }}
                onClick={() => node.id != null && setSelectedId(node.id)}
                onKeyDown={(e) => {
                  if ((e.key === "Enter" || e.key === " ") && node.id != null) {
                    e.preventDefault();
                    setSelectedId(node.id);
                  }
                }}
                onFocus={() => setFocusedId(node.id ?? null)}
                onBlur={() => setFocusedId(null)}
              >
                {inProgress && !reduceMotion && (
                  <motion.rect
                    x={node.lx - 6}
                    y={node.ly - 6}
                    width={NODE_W + 12}
                    height={NODE_H + 12}
                    rx={radius + 6}
                    fill="none"
                    stroke={theme.colors.primary[500]}
                    strokeWidth={3}
                    initial={{ strokeOpacity: 0.55, scale: 1 }}
                    animate={{ strokeOpacity: 0, scale: 1.06 }}
                    transition={{ duration: 1.6, repeat: Infinity, ease: "easeOut" }}
                    style={{ transformOrigin: `${cx}px ${cy}px` }}
                  />
                )}
                {isFinal && (
                  <rect
                    x={node.lx - 5}
                    y={node.ly - 5}
                    width={NODE_W + 10}
                    height={NODE_H + 10}
                    rx={radius + 5}
                    fill="none"
                    stroke={style.border}
                    strokeWidth={1.5}
                    strokeOpacity={0.5}
                  />
                )}
                {focusedId === node.id && (
                  <rect
                    x={node.lx - 4}
                    y={node.ly - 4}
                    width={NODE_W + 8}
                    height={NODE_H + 8}
                    rx={radius + 4}
                    fill="none"
                    stroke={theme.colors.primary[300]}
                    strokeWidth={3}
                  />
                )}
                <rect
                  x={node.lx}
                  y={node.ly}
                  width={NODE_W}
                  height={NODE_H}
                  rx={radius}
                  fill={style.fill}
                  stroke={style.border}
                  strokeWidth={inProgress ? 2 : 1.5}
                />
                {status === "completed" ? (
                  <g>
                    <circle cx={node.lx + 22} cy={cy} r={9} fill={style.badge} />
                    <path
                      d={`M ${node.lx + 17.5} ${cy} l 3 3.5 l 6 -7`}
                      fill="none"
                      stroke={theme.colors.neutral[0]}
                      strokeWidth={2}
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </g>
                ) : (
                  <circle
                    cx={node.lx + 22}
                    cy={cy}
                    r={6}
                    fill={inProgress ? style.badge : "none"}
                    stroke={style.badge}
                    strokeWidth={2}
                  />
                )}
                <text
                  x={node.lx + 40}
                  y={cy - (inProgress ? 6 : 0)}
                  dominantBaseline="middle"
                  fill={style.text}
                  fontSize={13.5}
                  fontWeight={600}
                  fontFamily={theme.typography.fontFamily.body}
                >
                  {node.estadoGestionNombre ?? `Nodo ${node.id}`}
                </text>
                {inProgress && (
                  <text
                    x={node.lx + 40}
                    y={cy + 12}
                    dominantBaseline="middle"
                    fill={theme.colors.primary[600]}
                    fontSize={10.5}
                    fontWeight={500}
                    fontFamily={theme.typography.fontFamily.body}
                  >
                    {tw("status.in_progress")}
                  </text>
                )}
              </motion.g>
            );
          })}
        </svg>
      </div>

      <div className="flex items-center justify-center gap-6 mt-4 flex-wrap" data-testid="workflow-legend">
        {(["completed", "in_progress", "pending"] as const).map((status) => (
          <span key={status} className="inline-flex items-center gap-2 text-xs font-medium"
            style={{ color: theme.colors.neutral[600] }}>
            <span
              className="inline-block w-3 h-3 rounded-full"
              style={{ backgroundColor: STATUS_STYLE[status].badge }}
            />
            {tw(`status.${status}`)}
          </span>
        ))}
      </div>

      <AnimatePresence>
        {selected && (
          <NodeModal
            node={selected}
            status={nodeStatus(selected, trace.nodeStatuses)}
            trace={trace}
            onClose={() => setSelectedId(null)}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
