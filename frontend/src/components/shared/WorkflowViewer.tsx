"use client";
import { useMemo } from "react";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  type Node,
  type Edge,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import type { WorkflowNode, WorkflowTransition } from "@/types";

const NODE_COLORS: Record<string, { background: string; border: string; color: string }> = {
  INITIAL: { background: "#dcfce7", border: "#16a34a", color: "#15803d" },
  INTERMEDIATE: { background: "#dbeafe", border: "#2563eb", color: "#1d4ed8" },
  FINAL: { background: "#fee2e2", border: "#dc2626", color: "#b91c1c" },
};

function toFlowNodes(nodes: WorkflowNode[]): Node[] {
  return nodes.map((n) => {
    const colors = NODE_COLORS[n.tipo ?? "INTERMEDIATE"];
    return {
      id: String(n.id),
      position: { x: n.posicionX ?? 0, y: n.posicionY ?? 0 },
      data: { label: n.estadoGestionNombre ?? `Nodo ${n.id}` },
      style: {
        background: colors.background,
        border: `2px solid ${colors.border}`,
        color: colors.color,
        borderRadius: "8px",
        padding: "8px 16px",
        fontWeight: 600,
        fontSize: "13px",
        minWidth: "120px",
        textAlign: "center" as const,
      },
    };
  });
}

function toFlowEdges(transitions: WorkflowTransition[]): Edge[] {
  return transitions.map((t) => ({
    id: String(t.id),
    source: String(t.nodoOrigenId),
    target: String(t.nodoDestinoId),
    label: t.descripcion ?? undefined,
    animated: false,
    style: { stroke: "#94a3b8" },
  }));
}

interface WorkflowViewerProps {
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  "data-testid"?: string;
}

export function WorkflowViewer({ nodes, transitions, "data-testid": testId }: WorkflowViewerProps) {
  const flowNodes = useMemo(() => toFlowNodes(nodes), [nodes]);
  const flowEdges = useMemo(() => toFlowEdges(transitions), [transitions]);

  if (nodes.length === 0) {
    return (
      <div
        className="flex items-center justify-center h-64 rounded-xl border border-dashed border-neutral-300 text-neutral-400 text-sm"
        data-testid={testId}
      >
        Sin nodos. Agrega estados al workflow.
      </div>
    );
  }

  return (
    <div style={{ height: 440 }} className="rounded-xl border border-neutral-200 overflow-hidden" data-testid={testId}>
      <ReactFlow
        nodes={flowNodes}
        edges={flowEdges}
        fitView
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={false}
      >
        <Background />
        <Controls showInteractive={false} />
        <MiniMap nodeColor={(n) => {
          const tipo = nodes.find((wn) => String(wn.id) === n.id)?.tipo ?? "INTERMEDIATE";
          return NODE_COLORS[tipo]?.border ?? "#94a3b8";
        }} />
      </ReactFlow>
    </div>
  );
}
